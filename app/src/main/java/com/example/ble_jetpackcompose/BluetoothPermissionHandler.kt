package com.example.ble_jetpackcompose

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class BluetoothManager(private val activity: ComponentActivity) {
    val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            val bluetoothManager =
                activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        } catch (e: SecurityException) {
            null
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun enableBluetooth() {
        try {
            if (!isBluetoothEnabled()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                activity,
                "Bluetooth permissions not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun enableLocation() {
        if (!isLocationEnabled()) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(intent, REQUEST_ENABLE_LOCATION)
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_ENABLE_LOCATION = 2
    }
}


@Composable
fun BluetoothPermissionHandler(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val showRationaleDialog = remember { mutableStateOf(false) }
    val showEnableDialog = remember { mutableStateOf(false) }
    val btManager = remember { BluetoothManager(context as ComponentActivity) }
    var checkingServices by remember { mutableStateOf(true) }

    // Check both services initially and after resuming
    LaunchedEffect(checkingServices) {
        if (!btManager.isBluetoothEnabled() || !btManager.isLocationEnabled()) {
            showEnableDialog.value = true
        }
    }

    // Activity result handler for Bluetooth
    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkingServices = !checkingServices // Trigger recheck
    }

    // Activity result handler for Location
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkingServices = !checkingServices // Trigger recheck
    }

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            if (!btManager.isBluetoothEnabled() || !btManager.isLocationEnabled()) {
                showEnableDialog.value = true
            } else {
                onPermissionsGranted()
            }
        } else {
            showRationaleDialog.value = true
        }
    }

    LaunchedEffect(Unit) {
        checkAndRequestPermissions(context, requiredPermissions, permissionLauncher)
    }

    if (showRationaleDialog.value) {
        RationaleDialog(
            onConfirm = {
                permissionLauncher.launch(requiredPermissions)
                showRationaleDialog.value = false
            },
            onDismiss = { showRationaleDialog.value = false }
        )
    }

    if (showEnableDialog.value) {
        AlertDialog(
            onDismissRequest = { showEnableDialog.value = false },
            title = { Text("Enable Required Services") },
            text = { Text("Both Bluetooth and Location services need to be enabled to scan for devices.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!btManager.isBluetoothEnabled()) {
                            bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }
                        if (!btManager.isLocationEnabled()) {
                            locationLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                        showEnableDialog.value = false
                    }
                ) {
                    Text("Enable Services")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnableDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }


}

@Composable
private fun RationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bluetooth Permissions Required") },
        text = { Text("Bluetooth and location permissions are needed to scan and connect to devices.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Request Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun checkAndRequestPermissions(
    context: Context,
    permissions: Array<String>,
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val notGrantedPermissions = permissions.filter { permission ->
        ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
    }

    if (notGrantedPermissions.isNotEmpty()) {
        launcher.launch(notGrantedPermissions.toTypedArray())
    }
}