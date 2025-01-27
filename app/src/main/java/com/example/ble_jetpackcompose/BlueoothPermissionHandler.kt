package com.example.ble_jetpackcompose
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun BluetoothPermissionHandler(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val showRationaleDialog = remember { mutableStateOf(false) }

    // Determine required permissions based on Android version
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            showRationaleDialog.value = true
        }
    }

    // Check permissions on first composition
    LaunchedEffect(Unit) {
        checkAndRequestPermissions(context, requiredPermissions, permissionLauncher)
    }

    // Rationale dialog if permissions are denied
    if (showRationaleDialog.value) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog.value = false },
            title = { Text("Bluetooth Permissions Required") },
            text = { Text("Bluetooth and location permissions are needed to scan and connect to devices.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionLauncher.launch(requiredPermissions)
                        showRationaleDialog.value = false
                    }
                ) {
                    Text("Request Permissions")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRationaleDialog.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper function to check and request permissions
private fun checkAndRequestPermissions(
    context: Context,
    permissions: Array<String>,
    launcher: ActivityResultLauncher<Array<String>>
) {
    val notGrantedPermissions = permissions.filter { permission ->
        ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
    }

    if (notGrantedPermissions.isNotEmpty()) {
        launcher.launch(notGrantedPermissions.toTypedArray())
    }
}