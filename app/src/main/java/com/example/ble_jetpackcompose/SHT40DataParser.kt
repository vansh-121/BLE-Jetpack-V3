//package com.example.ble_jetpackcompose
//
//class SHT40DataParser {
//    data class SHT40Reading(
//        val deviceId: String,
//        val temperature: Float,
//        val humidity: Float
//    )
//
//    fun parseData(data: ByteArray): SHT40Reading? {
//        return try {
//            if (data.size >= 5) {
//                val deviceId = data[0].toUByte().toString()
//                val temperatureBeforeDecimal = data[1].toUByte().toString()
//                val temperatureAfterDecimal = data[2].toUByte().toString()
//                val humidityBeforeDecimal = data[3].toUByte().toString()
//                val humidityAfterDecimal = data[4].toUByte().toString()
//
//                val temperature = "$temperatureBeforeDecimal.$temperatureAfterDecimal".toFloat()
//                val humidity = "$humidityBeforeDecimal.$humidityAfterDecimal".toFloat()
//
//                SHT40Reading(deviceId, temperature, humidity)
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//}