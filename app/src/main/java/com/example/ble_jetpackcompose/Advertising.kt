package com.example.ble_jetpackcompose

import android.app.Activity
import android.content.ContentResolver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun AdvertisingDataScreen(
    contentResolver: ContentResolver,
    deviceAddress: String,
    deviceName: String,
    navController: NavController,
    sensorType: String,
    deviceId: String,
    sensorData: BluetoothScanViewModel.SensorData? = null
) {
    // Get ViewModel instance using the remember helper
    val context = LocalContext.current
    val viewModel = remember {
        BluetoothScanViewModel<Any?>(context)
    }

    // Collect devices flow
    val devices by viewModel.devices.collectAsState()

    // Find the current device and its latest data
    val currentDevice = devices.find { it.address == deviceAddress }
    val currentSensorData = currentDevice?.sensorData
    val activity = context as Activity

    var displayData by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // Update display data whenever currentSensorData changes
    LaunchedEffect(currentSensorData) {
        displayData = when (currentSensorData) {
            is BluetoothScanViewModel.SHT40Data -> listOf(
                "Temperature" to "${currentSensorData.temperature}°C",
                "Humidity" to "${currentSensorData.humidity}%"
            )
            is BluetoothScanViewModel.LIS2DHData -> listOf(
                "X-Axis" to "${currentSensorData.x} m/s²",
                "Y-Axis" to "${currentSensorData.y} m/s²",
                "Z-Axis" to "${currentSensorData.z} m/s²"
            )
            is BluetoothScanViewModel.SoilSensorData -> listOf(
                "Nitrogen" to "${currentSensorData.nitrogen} mg/kg",
                "Phosphorus" to "${currentSensorData.phosphorus} mg/kg",
                "Potassium" to "${currentSensorData.potassium} mg/kg",
                "Moisture" to "${currentSensorData.moisture}%",
                "Temperature" to "${currentSensorData.temperature}°C",
                "Electric Conductivity" to "${currentSensorData.ec} mS/cm",
                "pH" to "${currentSensorData.pH}"
            )
            is BluetoothScanViewModel.LuxData -> listOf(
                "Light Intensity" to "${currentSensorData.calculatedLux} LUX"
            )
            null -> emptyList()
        }
    }

    // Start scanning when the screen is launched
    LaunchedEffect(Unit) {
        viewModel.startScan(activity)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF0A74DA), Color(0xFFADD8E6))))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = {
                    viewModel.stopScan() // Stop scanning when leaving the screen
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Advertising Data",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { navController.navigate("chart_screen") }) {
                    Image(
                        painter = painterResource(id = R.drawable.graph),
                        contentDescription = "Graph Icon",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .width(365.dp)
                    .height(49.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0A8AE6),
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFF2A9EE5), Color(0xFF076FB8))))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Device Name: $deviceName ($deviceAddress)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .width(365.dp)
                    .height(49.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0A8AE6),
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFF2A9EE5), Color(0xFF076FB8))))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Node ID: ${deviceId ?: "Unknown"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display sensor data using ResponsiveDataCards
            ResponsiveDataCards(
                sensorType = sensorType,
                data = displayData
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Show sun animation only for LUX sensor
            if (currentSensorData is BluetoothScanViewModel.LuxData) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    SunWithRayAnimation(lux = currentSensorData.calculatedLux)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "DOWNLOAD DATA",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Clean up when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScan()
        }
    }
}

@Composable
fun ResponsiveDataCards(sensorType: String, data: List<Pair<String, String>>) {
    when (data.size) {
        1 -> { // Single card, centered
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                DataCard(label = data[0].first, value = data[0].second)
            }
        }
        2 -> { // Two cards in a single row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { (label, value) ->
                    DataCard(label = label, value = value)
                }
            }
        }
        else -> { // More than 2, display in rows of 2
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data.chunked(2).forEach { rowItems -> // Split the list into chunks of 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { (label, value) ->
                            DataCard(label = label, value = value)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Add space between rows
                }
            }
        }
    }
}

@Composable
fun SunWithRayAnimation(
    lux: Float,
    rayThickness: Dp = 2.dp,
    rayCount: Int = 12,
    maxLux: Float = 255f
) {
    // Define lux thresholds for ray growth stages
    val luxThresholds = listOf(200f, 700f, 1500f)

    // Determine the current ray growth stage
    val rayStage = when {
        lux >= luxThresholds[2] -> 3
        lux >= luxThresholds[1] -> 2
        lux >= luxThresholds[0] -> 1
        else -> 0
    }

    // Calculate ray lengths for different stages
    val rayLengths = listOf(
        10.dp,   // Stage 0: Minimal rays
        100.dp,   // Stage 1: Short rays
        150.dp,  // Stage 2: Medium rays
        200.dp   // Stage 3: Long rays
    )

    // Select current ray length based on stage
    val currentRayLength = rayLengths[rayStage]

    // Ray opacity and color based on lux intensity
    val normalizedLux = lux.coerceIn(0f, maxLux) / maxLux
    val rayOpacity = normalizedLux.coerceIn(0.1f, 1f)
    val rayColor = Color(
        red = 1f,
        green = (0.7f + 0.3f * normalizedLux).coerceIn(0.7f, 1f),
        blue = (0.2f * normalizedLux).coerceIn(0f, 0.2f)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        // Render sunrays with stage-based length
        for (i in 0 until rayCount) {
            Box(
                modifier = Modifier
                    .width(rayThickness)
                    .height(currentRayLength)
                    .graphicsLayer {
                        rotationZ = (i * (360f / rayCount))
                        alpha = rayOpacity
                    }
                    .background(rayColor)
            )
        }

        // Sun image
        Image(
            painter = painterResource(id = R.drawable.sun),
            contentDescription = "Sun",
            modifier = Modifier.size(100.dp)
        )
    }
}
@Composable
fun DataCard(label: String, value: String) {
    Surface(
        modifier = Modifier
            .height(95.dp)
            .width(141.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2A9EE5), Color(0xFF076FB8))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertisingDataScreenPreview() {
    val deviceName = ""
    AdvertisingDataScreen(
        contentResolver = LocalContext.current.contentResolver,
        deviceAddress = TODO(),
        deviceName = deviceName,
        navController = TODO(),
        sensorType = TODO(),
        deviceId = TODO(),
    )
}