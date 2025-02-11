package com.example.ble_jetpackcompose


data class BLEDevice(
    val name: String,
    val address: String,
    val rssi: String,
    val deviceId: String
)