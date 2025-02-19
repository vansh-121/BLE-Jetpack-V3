package com.example.ble_jetpackcompose

sealed class SensorData {
        data class MetalDetectorData(
            val isMetalDetected: Boolean
        ) : SensorData()

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

        data class WeatherData(
            val temperature: String,
            val humidity: String,
            val pressure: String
        ) : SensorData()

        data class SpeedDistanceData(
            val speed: String,
            val distance: String
        ) : SensorData()
    }