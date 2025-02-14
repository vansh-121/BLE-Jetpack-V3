package com.example.ble_jetpackcompose

import android.Manifest.*
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch






class BluetoothScanViewModel(private val context: Context) : ViewModel() {
    private val _devices = MutableStateFlow<List<BLEDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.bluetoothLeScanner
    }
    private val _isScanning = MutableStateFlow(false)


    init {
        // Initialize _devices with any previously stored devices
        _devices.value = persistentDevices
    }

    private val scanSettings by lazy {
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setReportDelay(0L)
            .setLegacy(false)
            .build()
    }

    private val scanFilters by lazy {
        listOf(ScanFilter.Builder().build())
    }

    // Use a job to manage scanning lifecycle
    private var scanJob: Job? = null


    private fun checkAndEnableBluetoothAndLocation(activity: Activity) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter ?: return

        // Check Bluetooth availability

        // Check and request to enable Bluetooth if disabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Check if location services are enabled
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isLocationEnabled) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION)
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                permission.BLUETOOTH_SCAN,
                permission.BLUETOOTH_CONNECT,
                permission.ACCESS_FINE_LOCATION
            )
        } else {
            TODO("VERSION.SDK_INT < S")
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }
    private val scanDuration = 20000L // 10 seconds



    fun startScan(activity: Activity) {
        if (_isScanning.value) return  // Prevent multiple scan sessions

        checkAndEnableBluetoothAndLocation(activity)
        if (!hasBluetoothPermissions()) return

        scanJob?.cancel() // Cancel any existing scan
        scanJob = viewModelScope.launch {
            try {
                bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
                _isScanning.value = true

                delay(scanDuration)
                stopScan()
            } catch (e: SecurityException) {
                _isScanning.value = false
            }
        }
    }


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            try {
                val device = result.device ?: return
                val scanRecord = result.scanRecord ?: return
                val manufacturerData = scanRecord.manufacturerSpecificData

                // Extract manufacturer data and device ID
                val manufacturerDataBytes: ByteArray?
                val deviceId: String

                when {
                    // Check manufacturer specific data
                    (manufacturerData?.size() ?: 0) > 0 -> {
                        val key = manufacturerData.keyAt(0)
                        manufacturerDataBytes = manufacturerData.get(key)
                        deviceId = if (manufacturerDataBytes?.isNotEmpty() == true) {
                            manufacturerDataBytes[0].toUByte().toString()
                        } else {
                            "Unknown ID"
                        }
                    }
                    // Fallback to scan record bytes if no manufacturer data
                    scanRecord.bytes?.isNotEmpty() == true -> {
                        manufacturerDataBytes = scanRecord.bytes
                        deviceId = manufacturerDataBytes[0].toUByte().toString()
                    }
                    else -> {
                        manufacturerDataBytes = null
                        deviceId = "Unknown ID"
                    }
                }

                // Filter out devices with empty names and addresses
                if (device.address.isNullOrEmpty() || device.name.isNullOrEmpty()) {
                    return
                }

                // Determine device type based on name or other characteristics
                val deviceType = determineDeviceType(manufacturerDataBytes)
                // Parse sensor-specific data
                val sensorData = manufacturerDataBytes?.let { data ->
                    parseDeviceData(data, deviceType)
                }

                val bleDevice = BLEDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    rssi = result.rssi.toString(),
                    deviceId = deviceId,
                    sensorData = sensorData,
                    deviceType = deviceType
                )

                viewModelScope.launch {
                    updateDevicesList(bleDevice)
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
    }

    private fun determineDeviceType(manufacturerData: ByteArray?): String {
        return when {
            (manufacturerData?.size ?: 0) >= 11 -> "Soil Sensor"
            (manufacturerData?.size ?: 0) >= 7 -> "LIS2DH"
            (manufacturerData?.size ?: 0) >= 6 -> "Speed Distance"  // Moved before SHT40
            (manufacturerData?.size ?: 0) >= 5 -> "SHT40"
            (manufacturerData?.size ?: 0) >= 3 -> "LUX"
            else -> "Unknown"
        }
    }



    private fun parseDeviceData(data: ByteArray, deviceType: String): SensorData? {
        return try {
            when (deviceType) {
                "SHT40" -> {
                    if (data.size >= 5) {
                        SHT40Data(
                            temperature = "${data[1].toUByte()}.${data[2].toUByte()}",
                            humidity = "${data[3].toUByte()}.${data[4].toUByte()}"
                        )
                    } else null
                }
                "LIS2DH" -> {
                    if (data.size >= 7) {
                        LIS2DHData(
                            x = "${data[1].toUByte()}.${data[2].toUByte()}",
                            y = "${data[3].toUByte()}.${data[4].toUByte()}",
                            z = "${data[5].toUByte()}.${data[6].toUByte()}"
                        )
                    } else null
                }
                "Soil Sensor" -> {
                    if (data.size >= 11) {
                        SoilSensorData(
                            nitrogen = data[1].toUByte().toString(),
                            phosphorus = data[2].toUByte().toString(),
                            potassium = data[3].toUByte().toString(),
                            moisture = data[4].toUByte().toString(),
                            temperature = "${data[5].toUByte()}.${data[6].toUByte()}",
                            ec = "${data[7].toUByte()}.${data[8].toUByte()}",
                            pH = "${data[9].toUByte()}.${data[10].toUByte()}"
                        )
                    } else null
                }
                "LUX" -> {
                    if (data.size >= 3) {
                        val temp = "${data[1].toUByte()}.${data[2].toUByte()}".toFloat()
                        LuxData(calculatedLux = temp * 60)
                    } else null
                }
                "Speed Distance" -> {
                    if (data.size >= 5) {
                        SDTData(
                            speed = "${data[1].toUByte()}.${data[2].toUByte()}",
                            distance = "${data[3].toUByte()}.${data[4].toUByte()}"
                        )
                    } else null

                }



                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Data classes for different sensor types
    sealed class SensorData

    data class SHT40Data(
        val temperature: String,
        val humidity: String
    ) : SensorData()

    data class LIS2DHData(
        val x: String,
        val y: String,
        val z: String
    ) : SensorData()

    data class SoilSensorData(
        val nitrogen: String,
        val phosphorus: String,
        val potassium: String,
        val moisture: String,
        val temperature: String,
        val ec: String,
        val pH: String
    ) : SensorData()

    data class LuxData(
        val calculatedLux: Float
    ) : SensorData()

    data class SDTData(
        val speed: String,
        val distance: String
    ) : SensorData()



    // Update BLEDevice class to include sensor data
    data class BLEDevice(
        val name: String,
        val address: String,
        val rssi: String,
        val deviceId: String,
        val sensorData: SensorData? = null,
        val deviceType: String
    )

    private fun updateDevicesList(newDevice: BLEDevice) {
        viewModelScope.launch {
            val currentDevices = _devices.value
            val updatedDevices = currentDevices.toMutableList()

            val existingIndex = updatedDevices.indexOfFirst { it.address == newDevice.address }
            if (existingIndex != -1) {
                updatedDevices[existingIndex] = newDevice
            } else {
                updatedDevices.add(newDevice)
            }

            _devices.emit(updatedDevices)
            persistentDevices = updatedDevices
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Handle exception
        } finally {
            _isScanning.value = false
        }
    }
    fun clearDevices() {
        viewModelScope.launch {
            _devices.emit(emptyList())
            persistentDevices = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        clearDevices()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_LOCATION = 2
        private var persistentDevices = listOf<BLEDevice>()
    }
}