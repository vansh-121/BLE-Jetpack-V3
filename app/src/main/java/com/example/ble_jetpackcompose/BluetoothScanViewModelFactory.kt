package com.example.ble_jetpackcompose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BluetoothScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BluetoothScanViewModel<Any>(context) as T  // ✅ Correct instantiation
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}