package com.example.ble_jetpackcompose

import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import java.util.logging.Handler

class AdvertisingDataActivity : AppCompatActivity() {
    // Modify the parsing functions to handle real data




    private fun parseSHT40Data(data: ByteArray): String {
        val builder = StringBuilder()
        if (data.size >= 5) {
            val deviceId = data[0].toUByte().toString()
            val temperature = "${data[1].toUByte()}.${data[2].toUByte()}"
            val humidity = "${data[3].toUByte()}.${data[4].toUByte()}"

            // Store actual reading
//            addReading(
//                SHT40Reading(
//                    timestamp = System.currentTimeMillis(),
//                    deviceId = deviceId,
//                    temperature = temperature,
//                    humidity = humidity
//                )
//            )

            builder.append("Device ID: $deviceId\n")
            builder.append("Temperature: $temperature°C\n")
            builder.append("Humidity: $humidity%\n")
        } else {
            builder.append("Invalid SHT40 data format\n")
        }
        return builder.toString()
    }

    private fun parseLuxSensorData(data: ByteArray): String {
        val builder = StringBuilder()
        if (data.size >= 3) {
            val deviceId = data[0].toUByte().toString()
            // Combine two bytes to get actual lux value
            val luxValue = (data[1].toUByte().toInt() shl 8) or data[2].toUByte().toInt()

//            addReading(
//                LuxSensorReading(
//                    timestamp = System.currentTimeMillis(),
//                    deviceId = deviceId,
//                    lux = luxValue.toFloat()
//                )
//            )

            // Update UI based on actual lux value
            android.os.Handler(Looper.getMainLooper()).post {
//                luxViewBulb.visibility = View.VISIBLE
//                val bulbResource = when {
//                    luxValue < 1700 -> R.drawable.bulb_off
//                    luxValue in 1700..2000 -> R.drawable.bulb_25
//                    luxValue in 2001..2500 -> R.drawable.bulb_50
//                    luxValue in 2501..3000 -> R.drawable.bulb_75
//                    else -> R.drawable.bulb_glow
//                }
//                luxViewBulb.setImageResource(bulbResource)
            }

            builder.append("Device ID: $deviceId\n")
            builder.append("Light Intensity: $luxValue lux\n")
        } else {
            builder.append("Invalid Lux sensor data format\n")
        }
        return builder.toString()
    }

    // Add similar modifications for other sensor parsing functions...
}