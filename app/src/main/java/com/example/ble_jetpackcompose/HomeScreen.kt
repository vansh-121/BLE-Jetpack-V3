package com.example.ble_jetpackcompose

import BluetoothScanViewModel
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    viewModel: AuthViewModel,
    onSignOut: () -> Unit,
    navController: NavHostController,
    bluetoothViewModel: BluetoothScanViewModel,

//    goToSettings : () -> Unit
) {
    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val isPermissionGranted = remember { mutableStateOf(false) }
    val isScanning = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val hasPermissions = checkBluetoothPermissions(context)
        isPermissionGranted.value = hasPermissions

        if (hasPermissions) {
            // Start initial scan
            bluetoothViewModel.startScan()
            isScanning.value = true
        } else {
            println("Permissions not granted!")
        }
    }

    // Periodic scanning
    LaunchedEffect(isPermissionGranted.value) {
        while (isPermissionGranted.value) {
            if (!isScanning.value) {
                bluetoothViewModel.startScan()
                isScanning.value = true
            }
            delay(10000) // Scan every 30 seconds
        }
    }

    // Handle scan timeout
    LaunchedEffect(isScanning.value) {
        if (isScanning.value) {
            delay(3000) // 10 seconds scan timeout
            bluetoothViewModel.stopScan()
            isScanning.value = false
        }
    }

    BluetoothPermissionHandler(
        onPermissionsGranted = {
            isPermissionGranted.value = true
            bluetoothViewModel.startScan()
            isScanning.value = true
        })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Your existing TopAppBar code...

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nearby devices (${bluetoothDevices.size})",
                                    style = MaterialTheme.typography.h6
                                )
                                IconButton(
                                    onClick = {
                                        if (isPermissionGranted.value && !isScanning.value) {
                                            bluetoothViewModel.startScan()
                                            isScanning.value = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!isPermissionGranted.value) {
                                Text(
                                    "Bluetooth permissions required",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else if (bluetoothDevices.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isScanning.value) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Scanning for devices...")
                                    } else {
                                        Text("No devices found")
                                    }
                                }
                            } else {
                                bluetoothDevices.forEach { device ->
                                    BluetoothDeviceItem(device)
                                    Divider()
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Game Devices Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Game Devices",
                                    style = MaterialTheme.typography.h6
                                )
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    modifier = Modifier.clickable { /* More options logic */ }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(3) {
                                    GameSignalItem()
                                }
                            }
                        }
                    }
                }
            }
            Box {
                // Bottom Navigation
                CustomBottomNavigation(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    navController = navController
                )
            }
        }
    }
}


private fun checkBluetoothPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        checkPermission(context, Manifest.permission.BLUETOOTH_SCAN) &&
                checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT) &&
                checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        checkPermission(context, Manifest.permission.BLUETOOTH) &&
                checkPermission(context, Manifest.permission.BLUETOOTH_ADMIN) &&
                checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}


@Composable
fun BluetoothDeviceItem(device: BLEDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = device.name.toString(),
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "Address: ${device.address}",
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "Signal Strength: ${device.rssi} dBm",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
private fun DeviceItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun GameSignalItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun CustomBottomNavigation(modifier: Modifier = Modifier, navController: NavHostController) {
    BottomNavigation(
        modifier = modifier,
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.bluetooth),
                    contentDescription = "Bluetooth",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = true,
            onClick = { /* Navigate to Bluetooth */ }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.gamepad),
                    contentDescription = "Gameplay",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = {
                navController.navigate("game_loading")
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = { navController.navigate("settings_screen") }
        )
    }
}
