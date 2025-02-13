package com.example.ble_jetpackcompose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BluetoothScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothScanViewModel::class.java)) {
            return BluetoothScanViewModel<Any?>(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}