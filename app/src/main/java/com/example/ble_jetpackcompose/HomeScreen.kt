package com.example.ble_jetpackcompose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

data class TranslatedMainScreenText(
    val appTitle: String = "BLE Sense",
    val nearbyDevices: String = "Nearby Devices",
    val gameDevices: String = "Game Devices",
    val bluetoothPermissionsRequired: String = "Bluetooth permissions required",
    val scanningForDevices: String = "Scanning for devices...",
    val noDevicesFound: String = "No devices found",
    val scanningForGameDevices: String = "Scanning for game devices...",
    val noGameDevicesFound: String = "No game devices found",
    val showMore: String = "Show More",
    val showLess: String = "Show Less"
)

@Composable
fun MainScreen(
    navController: NavHostController,
    bluetoothViewModel: BluetoothScanViewModel<Any?>
) {
    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
    val isScanning by bluetoothViewModel.isScanning.collectAsState()
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val isPermissionGranted = remember { mutableStateOf(checkBluetoothPermissions(context)) }
    var expanded by remember { mutableStateOf(false) }
    val sensorTypes = listOf("SHT40", "LIS2DH", "Soil Sensor", "Weather", "LUX", "Speed Distance", "Metal Detector", "Step Counter", "Ammonia Sensor")
    var selectedSensor by remember { mutableStateOf(sensorTypes[0]) }
    var showAllDevices by remember { mutableStateOf(false) }

    // Sync with ThemeManager and LanguageManager
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // State for translated text
    var translatedText by remember {
        mutableStateOf(
            TranslatedMainScreenText(
                appTitle = TranslationCache.get("BLE Sense-$currentLanguage") ?: "BLE Sense",
                nearbyDevices = TranslationCache.get("Nearby Devices-$currentLanguage") ?: "Nearby Devices",
                gameDevices = TranslationCache.get("Game Devices-$currentLanguage") ?: "Game Devices",
                bluetoothPermissionsRequired = TranslationCache.get("Bluetooth permissions required-$currentLanguage") ?: "Bluetooth permissions required",
                scanningForDevices = TranslationCache.get("Scanning for devices...-$currentLanguage") ?: "Scanning for devices...",
                noDevicesFound = TranslationCache.get("No devices found-$currentLanguage") ?: "No devices found",
                scanningForGameDevices = TranslationCache.get("Scanning for game devices...-$currentLanguage") ?: "Scanning for game devices...",
                noGameDevicesFound = TranslationCache.get("No game devices found-$currentLanguage") ?: "No game devices found",
                showMore = TranslationCache.get("Show More-$currentLanguage") ?: "Show More",
                showLess = TranslationCache.get("Show Less-$currentLanguage") ?: "Show Less"
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf(
            "BLE Sense", "Nearby Devices", "Game Devices", "Bluetooth permissions required",
            "Scanning for devices...", "No devices found", "Scanning for game devices...",
            "No game devices found", "Show More", "Show Less"
        )
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedMainScreenText(
            appTitle = translatedList[0],
            nearbyDevices = translatedList[1],
            gameDevices = translatedList[2],
            bluetoothPermissionsRequired = translatedList[3],
            scanningForDevices = translatedList[4],
            noDevicesFound = translatedList[5],
            scanningForGameDevices = translatedList[6],
            noGameDevicesFound = translatedList[7],
            showMore = translatedList[8],
            showLess = translatedList[9]
        )
    }

    // Define colors based on theme
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val cardBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF757575)
    val dividerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)

    // Check permissions initially
    LaunchedEffect(Unit) {
        isPermissionGranted.value = checkBluetoothPermissions(context)
    }

    // Handle permission requests
    BluetoothPermissionHandler(
        onPermissionsGranted = {
            isPermissionGranted.value = true
        }
    )

    // Start periodic scanning when screen is visible, stop when disposed
    DisposableEffect(isPermissionGranted.value) {
        if (isPermissionGranted.value) {
            bluetoothViewModel.startPeriodicScan(activity)
        }
        onDispose {
            bluetoothViewModel.stopScan()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material.TopAppBar(
                backgroundColor = cardBackgroundColor,
                elevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = translatedText.appTitle,
                        fontFamily = helveticaFont,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { ThemeManager.toggleDarkMode(!isDarkMode) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkMode) Color.Yellow else Color(0xFF5D4037)
                        )
                    }
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
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = cardBackgroundColor
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
                                    text = "${translatedText.nearbyDevices} (${bluetoothDevices.size})",
                                    style = MaterialTheme.typography.h6,
                                    color = textColor
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPermissionGranted.value && !isScanning) {
                                                bluetoothViewModel.startPeriodicScan(activity)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = textColor
                                        )
                                    }
                                    Box {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "More Options",
                                                tint = textColor
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.background(cardBackgroundColor)
                                        ) {
                                            sensorTypes.forEach { sensor ->
                                                DropdownMenuItem(
                                                    text = { Text(sensor, color = textColor) },
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
                                    text = translatedText.bluetoothPermissionsRequired,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = textColor
                                )
                            } else if (bluetoothDevices.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isScanning) {
                                        CircularProgressIndicator(
                                            color = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF2196F3)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(translatedText.scanningForDevices, color = textColor)
                                    } else {
                                        Text(translatedText.noDevicesFound, color = textColor)
                                    }
                                }
                            } else {
                                val devicesToShow = if (showAllDevices) bluetoothDevices else bluetoothDevices.take(4)
                                devicesToShow.forEach { device ->
                                    BluetoothDeviceItem(
                                        device = device,
                                        navController = navController,
                                        selectedSensor = selectedSensor,
                                        isDarkMode = isDarkMode
                                    )
                                    Divider(color = dividerColor)
                                }
                                if (bluetoothDevices.size > 4) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable { showAllDevices = !showAllDevices }
                                            .background(
                                                if (isDarkMode) Color(0xFF2A2A2A) else Color.LightGray,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (showAllDevices) translatedText.showLess else translatedText.showMore,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    val allowedGameDevices = listOf(
                        "Scarlet Witch", "Black Widow", "Captain Marvel", "Wasp", "Hela",
                        "Hulk", "Thor", "Iron_Man", "Spider Man", "Captain America"
                    )
                    val gameDevices = bluetoothDevices.filter { it.name in allowedGameDevices }
                    val devicesToShow = if (showAllDevices) gameDevices else gameDevices.take(4)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp), // Match Nearby Devices padding
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = cardBackgroundColor
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${translatedText.gameDevices} (${gameDevices.size})",
                                    style = MaterialTheme.typography.h6,
                                    color = textColor
                                )
                                IconButton(
                                    onClick = {
                                        if (isPermissionGranted.value && !isScanning) {
                                            bluetoothViewModel.startPeriodicScan(activity)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = textColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (!isPermissionGranted.value) {
                                Text(
                                    text = translatedText.bluetoothPermissionsRequired,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = textColor
                                )
                            } else if (gameDevices.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isScanning) {
                                        CircularProgressIndicator(
                                            color = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF2196F3)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(translatedText.scanningForGameDevices, color = textColor)
                                    } else {
                                        Text(translatedText.noGameDevicesFound, color = textColor)
                                    }
                                }
                            } else {
                                devicesToShow.forEach { device ->
                                    BluetoothDeviceItem(
                                        device = device,
                                        navController = navController,
                                        selectedSensor = selectedSensor,
                                        isDarkMode = isDarkMode
                                    )
                                    Divider(color = dividerColor)
                                }
                                if (gameDevices.size > 4) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable { showAllDevices = !showAllDevices }
                                            .background(
                                                if (isDarkMode) Color(0xFF2A2A2A) else Color.LightGray,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (showAllDevices) translatedText.showLess else translatedText.showMore,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box {
                CustomBottomNavigation(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    navController = navController,
                    isDarkMode = isDarkMode
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
    selectedSensor: String,
    isDarkMode: Boolean
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF757575)
    val iconBackgroundColor = if (isDarkMode) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
    val iconTintColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF2196F3)

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
                .background(iconBackgroundColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bluetooth),
                contentDescription = "Bluetooth Device",
                modifier = Modifier.size(24.dp),
                tint = iconTintColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = device.name,
                style = MaterialTheme.typography.subtitle1,
                color = textColor
            )
            Text(
                text = "Address: ${device.address}",
                style = MaterialTheme.typography.caption,
                color = secondaryTextColor
            )

            Text(
                text = "Signal Strength: ${device.rssi} dBm",
                style = MaterialTheme.typography.caption,
                color = secondaryTextColor
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
                            sensorData is BluetoothScanViewModel.SensorData.StepCounterData ->
                                "Steps: ${sensorData.steps}"
                            sensorData is BluetoothScanViewModel.SensorData.ObjectDetectorData ->
                                "Metal Detected: ${if (sensorData.detection) "Yes" else "No"}"
                            sensorData is BluetoothScanViewModel.SensorData.SDTData ->
                                "Speed: ${sensorData.speed}m/s, Distance: ${sensorData.distance}m"
                            sensorData is BluetoothScanViewModel.SensorData.AmmoniaSensorData ->
                                "Ammonia: ${sensorData.ammonia}ppm"

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
                        selectedSensor == "Step Counter" && sensorData is BluetoothScanViewModel.SensorData.StepCounterData ->
                            "Steps: ${sensorData.steps}"
                        selectedSensor == "LUX" && sensorData is BluetoothScanViewModel.SensorData.LuxData ->
                            "Light: ${sensorData.calculatedLux} LUX"

                        selectedSensor == "Speed Distance" && sensorData is BluetoothScanViewModel.SensorData.SDTData ->
                            "Speed: ${sensorData.speed}m/s, Distance: ${sensorData.distance}m"

                        selectedSensor == "Ammonia Sensor" && sensorData is BluetoothScanViewModel.SensorData.AmmoniaSensorData ->
                            "Ammonia: ${sensorData.ammonia}"

                        else -> "Incompatible sensor type"
                    },
                    style = MaterialTheme.typography.caption,
                    color = if (isDarkMode) Color(0xFF64B5F6) else MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isDarkMode: Boolean = false
) {
    val backgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val selectedColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF2196F3)

    BottomNavigation(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.bluetooth),
                    contentDescription = "Bluetooth",
                    modifier = Modifier.size(24.dp),
                    tint = selectedColor
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
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
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
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            },
            selected = false,
            onClick = { navController.navigate("settings_screen") }
        )
    }
}