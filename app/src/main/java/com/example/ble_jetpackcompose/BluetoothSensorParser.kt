//package com.example.ble_jetpackcompose
//
//import android.util.Log
//
//class BluetoothSensorParser {
//    sealed class SensorData
//
//    data class MetalDetectorData(
//        val isMetalDetected: Boolean
//    ) : SensorData()
//
//    data class SHT40Data(
//        val temperature: String,
//        val humidity: String
//    ) : SensorData()
//
//    data class LIS2DHData(
//        val x: String,
//        val y: String,
//        val z: String
//    ) : SensorData()
//
//    data class SoilSensorData(
//        val nitrogen: String,
//        val phosphorus: String,
//        val potassium: String,
//        val moisture: String,
//        val temperature: String,
//        val ec: String,
//        val pH: String
//    ) : SensorData()
//
//    data class LuxData(
//        val calculatedLux: Float
//    ) : SensorData()
//
//    data class WeatherData(
//        val temperature: String,
//        val humidity: String,
//        val pressure: String
//    ) : SensorData()
//
//    data class SpeedDistanceData(
//        val speed: String,
//        val distance: String
//    ) : SensorData()
//
//    // Main parser that delegates to specific sensor parsers
//    fun parseManufacturerData(data: ByteArray, deviceType: String?): SensorData? {
//        return try {
//            when (deviceType) {
//                "SHT40" -> parseSHT40Data(data)
//                "LIS2DH" -> parseLIS2DHData(data)
//                "Soil Sensor" -> parseSoilSensorData(data)
//                "Weather" -> parseWeatherData(data)
//                "SPEED_DISTANCE" -> parseSpeedDistanceData(data)
//                "Metal Detector" -> parseMetalDetectorData(data)
//                "LUX" -> parseLuxData(data)
//                else -> null
//            }
//        } catch (e: Exception) {
//            Log.e("BluetoothSensorParser", "Error parsing data for $deviceType: ${e.message}")
//            null
//        }
//    }
//
//    // Individual parser functions for each sensor type
//    private fun parseMetalDetectorData(data: ByteArray): MetalDetectorData? {
//        if (data.size < 2) return null
//
//        val isMetalDetected = data[1].toInt() != 0
//        return MetalDetectorData(isMetalDetected = isMetalDetected)
//    }
//
//    private fun parseSHT40Data(data: ByteArray): SHT40Data? {
//        if (data.size < 5) return null
//
//        return SHT40Data(
//            temperature = "${data[1].toUByte()}.${data[2].toUByte()}",
//            humidity = "${data[3].toUByte()}.${data[4].toUByte()}"
//        )
//    }
//
//    private fun parseLIS2DHData(data: ByteArray): LIS2DHData? {
//        if (data.size < 7) return null
//
//        return LIS2DHData(
//            x = "${data[1].toUByte()}.${data[2].toUByte()}",
//            y = "${data[3].toUByte()}.${data[4].toUByte()}",
//            z = "${data[5].toUByte()}.${data[6].toUByte()}"
//        )
//    }
//
//    private fun parseSoilSensorData(data: ByteArray): SoilSensorData? {
//        if (data.size < 11) return null
//
//        return SoilSensorData(
//            nitrogen = data[1].toUByte().toString(),
//            phosphorus = data[2].toUByte().toString(),
//            potassium = data[3].toUByte().toString(),
//            moisture = data[4].toUByte().toString(),
//            temperature = "${data[5].toUByte()}.${data[6].toUByte()}",
//            ec = "${data[7].toUByte()}.${data[8].toUByte()}",
//            pH = "${data[9].toUByte()}.${data[10].toUByte()}"
//        )
//    }
//
//    private fun parseWeatherData(data: ByteArray): WeatherData? {
//        if (data.size < 7) return null
//
//        return WeatherData(
//            temperature = "${data[1].toUByte()}.${data[2].toUByte()}",
//            humidity = "${data[3].toUByte()}.${data[4].toUByte()}",
//            pressure = "${data[5].toUByte()}.${data[6].toUByte()}"
//        )
//    }
//
//    private fun parseSpeedDistanceData(data: ByteArray): SpeedDistanceData? {
//        if (data.size < 5) return null
//
//        return SpeedDistanceData(
//            speed = "${data[1].toUByte()}.${data[2].toUByte()}",
//            distance = "${data[3].toUByte()}.${data[4].toUByte()}"
//        )
//    }
//
//    private fun parseLuxData(data: ByteArray): LuxData? {
//        if (data.size < 3) return null
//
//        val temp = "${data[1].toUByte()}.${data[2].toUByte()}".toFloat()
//        return LuxData(calculatedLux = temp * 60)
//    }
//
//    // Helper function to log raw data for debugging
//    private fun logRawData(data: ByteArray, deviceType: String) {
//        val hexString = data.joinToString(" ") { byte -> String.format("%02X", byte) }
//        Log.d("BluetoothSensorParser", "Raw data for $deviceType: $hexString")
//    }
//}