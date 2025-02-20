package com.example.ble_jetpackcompose

import android.Manifest
import android.app.Activity
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    navController: NavHostController,
    bluetoothViewModel: BluetoothScanViewModel,


//    goToSettings : () -> Unit
) {
    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val activity = context as Activity
    val isPermissionGranted = remember { mutableStateOf(false) }
    val isScanning = remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val sensorTypes = listOf("SHT40", "LIS2DH", "Soil Sensor", "Weather", "LUX", "Speed Distance", "Metal Detector")
    var selectedSensor by remember { mutableStateOf(sensorTypes[0]) }
    var showAllDevices by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        isPermissionGranted.value = checkBluetoothPermissions(context)
        if (isPermissionGranted.value) {
            bluetoothViewModel.startScan(activity)
        }
    }

// Remove the periodic scanning LaunchedEffect as it's causing the "no devices found" issue
// Instead, add this single scanning block:
    LaunchedEffect(isPermissionGranted.value) {
        if (isPermissionGranted.value) {
            bluetoothViewModel.startScan(activity)
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
            bluetoothViewModel.startScan(activity)
            isScanning.value = true
        })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Your existing TopAppBar code...
            androidx.compose.material.TopAppBar(
                backgroundColor = Color.White,
                elevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BLE Sense ",
                        color = Color.Black,
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPermissionGranted.value && !isScanning.value) {
                                                bluetoothViewModel.startScan(activity)
                                                isScanning.value = true
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh"
                                        )
                                    }
                                    // 3 Dots Menu Icon
                                    Box {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "More Options"
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            sensorTypes.forEach { sensor ->
                                                DropdownMenuItem(
                                                    text = { Text(sensor) },
                                                    onClick = {
                                                        selectedSensor = sensor
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
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
//                                bluetoothDevices.forEach { device ->
//                                    BluetoothDeviceItem(device)
//                                    Divider()
                                // Show only first 4 devices initially
                                val devicesToShow =
                                    if (showAllDevices) bluetoothDevices else bluetoothDevices.take(
                                        4
                                    )
                                devicesToShow.forEach { device ->
                                    BluetoothDeviceItem(device, navController, selectedSensor)
                                    Divider()
                                }

                                // Show "Show More" button if there are more than 4 devices
                                // Toggle between "Show More" and "Show Less" if there are more than 4 devices
                                if (bluetoothDevices.size > 4) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable { showAllDevices = !showAllDevices }
                                            .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (showAllDevices) "Show Less" else "Show More",
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Define the allowed game devices
                    val allowedGameDevices = listOf(
                        "Scarlet Witch", "Black Widow", "Captain Marvel", "Wasp", "Hela",
                        "Hulk", "Thor", "Iron_Man", "Spider Man", "Captain America"
                    )

                    // Filter Bluetooth devices to show only the allowed game devices
                    val gameDevices = bluetoothDevices.filter { it.name in allowedGameDevices }

                    // Show only first 4 devices initially
                    val devicesToShow = if (showAllDevices) gameDevices else gameDevices.take(4)

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
                                    text = "Game Devices (${gameDevices.size})",
                                    style = MaterialTheme.typography.h6
                                )
                                IconButton(
                                    onClick = {
                                        if (isPermissionGranted.value && !isScanning.value) {
                                            bluetoothViewModel.startScan(activity)
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
                            } else if (gameDevices.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isScanning.value) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Scanning for game devices...")
                                    } else {
                                        Text("No game devices found")
                                    }
                                }
                            } else {
                                devicesToShow.forEach { device ->
                                    BluetoothDeviceItem(device, navController, selectedSensor)
                                    Divider()
                                }

                                // Show "Show More" button if there are more than 4 devices
                                if (gameDevices.size > 4) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable { showAllDevices = !showAllDevices }
                                            .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (showAllDevices) "Show Less" else "Show More",
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
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
    } else
        checkPermission(context, Manifest.permission.BLUETOOTH) &&
                checkPermission(context, Manifest.permission.BLUETOOTH_ADMIN) &&
                checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
}

private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}
@Composable
fun BluetoothDeviceItem(
    device: BluetoothScanViewModel.BluetoothDevice,
    navController: NavHostController,
    selectedSensor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate(
                    "advertising/${device.name.replace("/", "-")}/${device.address}/${selectedSensor.ifEmpty { "SHT40" }}/${device.deviceId}"
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bluetooth),
                contentDescription = "Bluetooth Device",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF2196F3)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = device.name,
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

            // Display sensor data based on selected sensor type
            device.sensorData?.let { sensorData ->
                Text(
                    text = when {
                        // If no sensor is selected, show data based on sensor data type
                        selectedSensor.isEmpty() -> when {
                            sensorData is BluetoothScanViewModel.SensorData.SHT40Data ->
                                "Temp: ${sensorData.temperature}°C, Humidity: ${sensorData.humidity}%"
                            sensorData is BluetoothScanViewModel.SensorData.LIS2DHData ->
                                "X: ${sensorData.x}, Y: ${sensorData.y}, Z: ${sensorData.z}"
                            sensorData is BluetoothScanViewModel.SensorData.SoilSensorData ->
                                "Temp: ${sensorData.temperature}°C, Moisture: ${sensorData.moisture}%"
                            sensorData is BluetoothScanViewModel.SensorData.LuxData ->
                                "Light: ${sensorData.calculatedLux} LUX"
                            sensorData is BluetoothScanViewModel.SensorData.ObjectDetectorData ->
                                "Metal Detected: ${if (sensorData.detection) "Yes" else "No"}"
                            sensorData is BluetoothScanViewModel.SensorData.SDTData ->
                                "Speed: ${sensorData.speed}m/s, Distance: ${sensorData.distance}m"

                            else -> "No data"
                        }

                        // Show specific sensor data based on selection
                        selectedSensor == "SHT40" && sensorData is BluetoothScanViewModel.SensorData.SHT40Data ->
                            "Temp: ${sensorData.temperature}°C, Humidity: ${sensorData.humidity}%"
                        selectedSensor == "Metal Detector" && sensorData is BluetoothScanViewModel.SensorData.ObjectDetectorData ->
                            "Metal Detected: ${if (sensorData.detection) "Yes" else "No"}"
                        selectedSensor == "LIS2DH" && sensorData is BluetoothScanViewModel.SensorData.LIS2DHData ->
                            "X: ${sensorData.x}, Y: ${sensorData.y}, Z: ${sensorData.z}"
                        selectedSensor == "Soil Sensor" && sensorData is BluetoothScanViewModel.SensorData.SoilSensorData ->
                            "N: ${sensorData.nitrogen}, P: ${sensorData.phosphorus}, K: ${sensorData.potassium}\n" +
                                    "Moisture: ${sensorData.moisture}%, Temp: ${sensorData.temperature}°C\n" +
                                    "EC: ${sensorData.ec}, pH: ${sensorData.pH}"
                        selectedSensor == "LUX" && sensorData is BluetoothScanViewModel.SensorData.LuxData ->
                            "Light: ${sensorData.calculatedLux} LUX"

                        selectedSensor == "Speed Distance" && sensorData is BluetoothScanViewModel.SensorData.SDTData ->
                            "Speed: ${sensorData.speed}m/s, Distance: ${sensorData.distance}m"

                        else -> "Incompatible sensor type"
                    },
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
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