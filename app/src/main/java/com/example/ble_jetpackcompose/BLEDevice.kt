package com.example.ble_jetpackcompose


data class BLEDevice(
    var name: String?,
    var address: String?,
    var rssi: String = 0.toString(),
    var lastSeen: Long = System.currentTimeMillis(),
    var isFound: Boolean = false,
)