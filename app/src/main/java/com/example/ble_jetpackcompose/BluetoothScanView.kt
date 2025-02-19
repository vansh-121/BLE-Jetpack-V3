package com.example.ble_jetpackcompose

import android.Manifest.*
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BluetoothScanViewModel(private val context: Context) : ViewModel() {
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices.asStateFlow()

    private var scanCallback: ScanCallback? = null

    // Region: Data Classes and Sealed Classes
    sealed class SensorData {
        abstract val deviceId: String

        data class SHT40Data(
            override val deviceId: String,
            val temperature: String,
            val humidity: String
        ) : SensorData()

        data class LuxData(
            override val deviceId: String,
            val calculatedLux: Float
        ) : SensorData()

        data class LIS2DHData(
            override val deviceId: String,
            val x: String,
            val y: String,
            val z: String
        ) : SensorData()

        data class SoilSensorData(
            override val deviceId: String,
            val nitrogen: String,
            val phosphorus: String,
            val potassium: String,
            val moisture: String,
            val temperature: String,
            val ec: String,
            val pH: String
        ) : SensorData()

        data class SDTData(
            override val deviceId: String,
            val speed: String,
            val distance: String
        ) : SensorData()

        data class ObjectDetectorData(
            override val deviceId: String,
            val detection: Boolean
        ) : SensorData()
    }

    data class BluetoothDevice(
        val name: String,
        val address: String,
        val deviceId: String,
        val sensorData: SensorData? = null
    )

    // Region: Scanner Management
    @SuppressLint("MissingPermission")
    fun startScan(activity: Activity) {
        getBluetoothScanner()?.let { scanner ->
            val scanSettings = createScanSettings()
            scanCallback = createScanCallback()
            scanner.startScan(null, scanSettings, scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        getBluetoothScanner()?.let { scanner ->
            scanCallback?.let { callback ->
                scanner.stopScan(callback)
                scanCallback = null
            }
        }
    }

    private fun getBluetoothScanner(): BluetoothLeScanner? =
        BluetoothAdapter.getDefaultAdapter()?.bluetoothLeScanner

    private fun createScanSettings(): ScanSettings =
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setLegacy(false)
            .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
            .build()

    // Region: Scan Callback
    private fun createScanCallback(): ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.device?.let { device ->
                if (device.address.isNullOrEmpty() || device.name.isNullOrEmpty()) return

                val deviceType = determineDeviceType(device.name)
                val sensorData = parseAdvertisingData(result, deviceType)

                val bluetoothDevice = BluetoothDevice(
                    name = device.name ?: "Unknown",
                    address = device.address,
                    deviceId = sensorData?.deviceId ?: "Unknown",
                    sensorData = sensorData
                )

                updateDevice(bluetoothDevice)
            }
        }
    }

    // Region: Data Parsing
    fun parseAdvertisingData(result: ScanResult, deviceType: String?): SensorData? {
        val manufacturerData = result.scanRecord?.manufacturerSpecificData ?: return null
        if (manufacturerData.size() == 0) return null

        val data = manufacturerData.valueAt(0) ?: return null
        return when (deviceType) {
            "SHT40" -> parseSHT40Data(data)
            "Lux Sensor" -> parseLuxSensorData(data)
            "LIS2DH" -> parseLIS2DHData(data)
            "Soil Sensor" -> parseSoilSensorData(data)
            "SPEED_DISTANCE" -> parseSDTData(data)
            "Metal Detector" -> parseMetalDetectorData(data)
            else -> null
        }
    }

    private fun parseSHT40Data(data: ByteArray): SensorData? {
        if (data.size < 5) return null
        return SensorData.SHT40Data(
            deviceId = data[0].toUByte().toString(),
            temperature = "${data[1].toUByte()}.${data[2].toUByte()}",
            humidity = "${data[3].toUByte()}.${data[4].toUByte()}"
        )
    }

    private fun parseLuxSensorData(data: ByteArray): SensorData? {
        if (data.size < 5) return null
        val deviceId = data[0].toUByte().toString()
        val temperature = "${data[1].toUByte()}.${data[2].toUByte()}".toFloat()
        return SensorData.LuxData(
            deviceId = deviceId,
            calculatedLux = temperature * 60
        )
    }

    private fun parseLIS2DHData(data: ByteArray): SensorData? {
        if (data.size < 7) return null
        return SensorData.LIS2DHData(
            deviceId = data[0].toUByte().toString(),
            x = "${data[1].toInt()}.${data[2].toUByte()}",
            y = "${data[3].toInt()}.${data[4].toUByte()}",
            z = "${data[5].toInt()}.${data[6].toUByte()}"
        )
    }

    private fun parseSoilSensorData(data: ByteArray): SensorData? {
        if (data.size < 11) return null
        return SensorData.SoilSensorData(
            deviceId = data[0].toUByte().toString(),
            nitrogen = data[1].toUByte().toString(),
            phosphorus = data[2].toUByte().toString(),
            potassium = data[3].toUByte().toString(),
            moisture = data[4].toUByte().toString(),
            temperature = "${data[5].toUByte()}.${data[6].toUByte()}",
            ec = "${data[7].toUByte()}.${data[8].toUByte()}",
            pH = "${data[9].toUByte()}.${data[10].toUByte()}"
        )
    }

    private fun parseSDTData(data: ByteArray): SensorData? {
        if (data.size < 6) return null
        return SensorData.SDTData(
            deviceId = data[0].toUByte().toString(),
            speed = "${data[1].toUByte()}.${data[2].toUByte()}",
            distance = "${data[4].toUByte()}.${data[5].toUByte()}"
        )
    }

    private fun parseMetalDetectorData(data: ByteArray): SensorData? {
        if (data.size < 2) return null
        return SensorData.ObjectDetectorData(
            deviceId = data[0].toUByte().toString(),
            detection = data[1].toInt() != 0
        )
    }

    // Region: Utility Functions
    private fun determineDeviceType(name: String?): String? = when {
        name?.contains("SHT", ignoreCase = true) == true -> "SHT40"
        name?.contains("LUX", ignoreCase = true) == true -> "Lux Sensor"
        name?.contains("Activity", ignoreCase = true) == true -> "LIS2DH"
        name?.contains("SOIL", ignoreCase = true) == true -> "Soil Sensor"
        name?.contains("Speed", ignoreCase = true) == true -> "SPEED_DISTANCE"
        name?.contains("METAL", ignoreCase = true) == true -> "Metal Detector"
        else -> null
    }

    private fun updateDevice(newDevice: BluetoothDevice) {
        _devices.update { devices ->
            val existingDeviceIndex = devices.indexOfFirst { it.address == newDevice.address }
            if (existingDeviceIndex >= 0) {
                devices.toMutableList().apply {
                    this[existingDeviceIndex] = newDevice
                }
            } else {
                devices + newDevice
            }
        }
    }

    fun clearDevices() {
        _devices.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        clearDevices()
    }
}