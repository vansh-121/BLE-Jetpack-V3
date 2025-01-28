package com.example.ble_jetpackcompose

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity

class BluetoothAdapterWrapper(private val activity: androidx.activity.ComponentActivity) {

    // Lazy initialization for Bluetooth Adapter
    val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            val bluetoothManager =
                activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        } catch (e: SecurityException) {
            null
        }
    }




    fun checkAndEnableBluetooth() {
        if (!hasPermissions()) {
            requestPermissions(PERMISSION_REQUEST_CODE)
            return
        }

        try {
            if (bluetoothAdapter == null) {
                Toast.makeText(
                    activity,
                    "Bluetooth is not supported on this device",
                    Toast.LENGTH_SHORT
                ).show()
                activity.finish()
            } else if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivity(enableBtIntent)
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                activity,
                "Bluetooth permissions not granted",
                Toast.LENGTH_SHORT
            ).show()
            requestPermissions(PERMISSION_REQUEST_CODE)
        }
    }





    // Function to check and request the required Bluetooth permissions based on Android version
    fun getRequiredPermissions(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Function to check if all required permissions are granted
    fun hasPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request permissions if not granted
    fun requestPermissions(requestCode: Int) {
        val permissionsToRequest = getRequiredPermissions().filter {
            ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                requestCode
            )
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}