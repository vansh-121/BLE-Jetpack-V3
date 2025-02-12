package com.example.ble_jetpackcompose

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_jetpackcompose.BLEDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch






class BluetoothScanViewModel<T>(private val context: Context) : ViewModel() {
    private val _devices = MutableStateFlow<List<BLEDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.bluetoothLeScanner
    }
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()


    init {
        // Initialize _devices with any previously stored devices
        _devices.value = persistentDevices
    }


    private fun checkAndEnableBluetoothAndLocation(activity: Activity) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Check Bluetooth availability
        if (bluetoothAdapter == null) {
            return
        }

        // Check and request to enable Bluetooth if disabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.ACCESS_FINE_LOCATION
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
        checkAndEnableBluetoothAndLocation(activity)

        // Check Bluetooth permissions
        if (!hasBluetoothPermissions()) {
            return
        }

        viewModelScope.launch {
            try {
                val scanFilters = listOf(
                    ScanFilter.Builder()
                        .build()
                )

                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setReportDelay(0L)
                    .setLegacy(false)
                    .build()



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
                    manufacturerData?.size() ?: 0 > 0 -> {
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
            } catch (e: SecurityException) {
            } catch (e: Exception) {
            }
        }
    }

    private fun determineDeviceType(manufacturerData: ByteArray?): String {
        return when {
            (manufacturerData?.size ?: 0) >= 11 -> "Soil Sensor"
            (manufacturerData?.size ?: 0) >= 7 -> "LIS2DH"
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
        val currentDevices = _devices.value
        val existingDeviceIndex = currentDevices.indexOfFirst { it.address == newDevice.address }

        val updatedDevices = if (existingDeviceIndex != -1) {
            currentDevices.toMutableList().apply {
                this[existingDeviceIndex] = newDevice
            }
        } else {
            currentDevices + newDevice
        }

        _devices.value = updatedDevices
        persistentDevices = updatedDevices
    }

    fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
        }
    }

    fun clearDevices() {
        _devices.value = emptyList()
        persistentDevices = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_LOCATION = 2
        private var persistentDevices = listOf<BLEDevice>()
    }
}