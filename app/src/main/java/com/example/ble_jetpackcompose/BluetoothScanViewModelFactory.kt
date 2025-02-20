package com.example.ble_jetpackcompose

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BluetoothScanViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BluetoothScanViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
