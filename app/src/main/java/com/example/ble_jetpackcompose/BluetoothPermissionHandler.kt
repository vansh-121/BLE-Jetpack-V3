package com.example.ble_jetpackcompose

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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

    fun enableBluetooth() {
        try {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivity(enableBtIntent)
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                activity,
                "Bluetooth permissions not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun BluetoothPermissionHandler(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val showRationaleDialog = remember { mutableStateOf(false) }
    val btManager = remember { BluetoothManager(context as ComponentActivity) }

    // Handle no Bluetooth support
    LaunchedEffect(btManager.bluetoothAdapter) {
        if (btManager.bluetoothAdapter == null) {
            Toast.makeText(
                context,
                "Bluetooth is not supported on this device",
                Toast.LENGTH_LONG
            ).show()
            (context as? ComponentActivity)?.finish()
        }
    }

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            btManager.enableBluetooth()
            onPermissionsGranted()
        } else {
            showRationaleDialog.value = true
        }
    }

    // Check permissions on composition and when permissions change
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