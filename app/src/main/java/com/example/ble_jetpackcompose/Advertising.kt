package com.example.ble_jetpackcompose

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun AdvertisingDataScreen(
    deviceAddress: String,
    deviceName: String,
    navController: NavController,
    deviceId: String,
) {
    val context = LocalContext.current
    // Use viewModel() for proper lifecycle management
    val viewModel: BluetoothScanViewModel = viewModel()
    val activity = context as Activity

    // Collect the device list from the ViewModel
    val devices by viewModel.devices.collectAsState()
    val currentDevice by remember(devices, deviceAddress) {
        derivedStateOf { devices.find { it.address == deviceAddress } }
    }

    // Prepare display data based on the sensor data type
    val displayData by remember(currentDevice?.sensorData) {
        derivedStateOf {
            when (val sensorData = currentDevice?.sensorData) {
                is BluetoothScanViewModel.SensorData.SHT40Data -> listOf(
                    "Temperature" to "${sensorData.temperature}°C",
                    "Humidity" to "${sensorData.humidity}%"
                )
                is BluetoothScanViewModel.SensorData.LIS2DHData -> listOf(
                    "X-Axis" to "${sensorData.x} m/s²",
                    "Y-Axis" to "${sensorData.y} m/s²",
                    "Z-Axis" to "${sensorData.z} m/s²"
                )
                is BluetoothScanViewModel.SensorData.SoilSensorData -> listOf(
                    "Nitrogen" to "${sensorData.nitrogen} mg/kg",
                    "Phosphorus" to "${sensorData.phosphorus} mg/kg",
                    "Potassium" to "${sensorData.potassium} mg/kg",
                    "Moisture" to "${sensorData.moisture}%",
                    "Temperature" to "${sensorData.temperature}°C",
                    "Electric Conductivity" to "${sensorData.ec} mS/cm",
                    "pH" to sensorData.pH
                )
                is BluetoothScanViewModel.SensorData.LuxData -> listOf(
                    "Light Intensity" to "${sensorData.calculatedLux} LUX"
                )
                is BluetoothScanViewModel.SensorData.SDTData -> listOf(
                    "Speed" to "${sensorData.speed} m/s",
                    "Distance" to "${sensorData.distance} m"
                )
                is BluetoothScanViewModel.SensorData.ObjectDetectorData -> listOf(
                    "Object Detected" to sensorData.detection.toString()
                )
                null -> emptyList()
            }
        }
    }

    // Start scanning when the screen is composed
    LaunchedEffect(Unit) {
        viewModel.startScan(activity)
    }

    // Clean up when leaving the screen
    DisposableEffect(navController) {
        onDispose {
            viewModel.stopScan()
            viewModel.clearDevices()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A74DA), Color(0xFFADD8E6))
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HeaderSection(
                navController = navController,
                viewModel = viewModel,
                deviceAddress = deviceAddress
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Device Info Cards
            DeviceInfoSection(
                deviceName = deviceName,
                deviceAddress = deviceAddress,
                deviceId = deviceId
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sensor Data Cards
            ResponsiveDataCards(
                data = displayData
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Conditional LUX Animation
            if (currentDevice?.sensorData is BluetoothScanViewModel.SensorData.LuxData) {
                LuxAnimationSection(
                    luxData = currentDevice?.sensorData as BluetoothScanViewModel.SensorData.LuxData
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Download Button
            DownloadButton(
                viewModel = viewModel,
                deviceAddress = deviceAddress,
                deviceName = deviceName,
                deviceId = deviceId
            )
        }
    }
}



@Composable
private fun DownloadButton(
    viewModel: BluetoothScanViewModel,
    deviceAddress: String,
    deviceName: String,
    deviceId: String
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    // Create a launcher for the file creation
    val createDocumentLauncher = rememberLauncherForActivityResult (
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            isExporting = true
            exportDataToCSV(context, uri, viewModel, deviceAddress, deviceName, deviceId) {
                isExporting = false
            }
        }
    }

    Button(
        onClick = {
            // Generate a filename with device info and timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
            val filename = "sensor_data_${deviceId}_$timestamp.csv"
            createDocumentLauncher.launch(filename)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = !isExporting
    ) {
        Text(
            text = if (isExporting) "EXPORTING DATA..." else "DOWNLOAD DATA",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun exportDataToCSV(
    context: Context,
    uri: Uri,
    viewModel: BluetoothScanViewModel,
    deviceAddress: String,
    deviceName: String,
    deviceId: String,
    onComplete: () -> Unit
) {
    // Launch the export process in an IO coroutine to avoid blocking the UI
    kotlinx.coroutines.MainScope().launch {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    // Get historical data
                    val historicalData = viewModel.getHistoricalDataForDevice(deviceAddress)

                    if (historicalData.isEmpty()) {
                        // If no historical data, add the current device data
                        val devices = viewModel.devices.value
                        val currentDevice = devices.find { it.address == deviceAddress }
                        currentDevice?.sensorData?.let { sensorData ->
                            val currentTime = System.currentTimeMillis()
                            val entry = BluetoothScanViewModel.HistoricalDataEntry(
                                timestamp = currentTime,
                                sensorData = sensorData
                            )
                            historicalData.toMutableList().add(entry)
                        }
                    }

                    // Create CSV header
                    val headerBuilder = StringBuilder()
                    headerBuilder.append("Timestamp,")
                    headerBuilder.append("Device Name,")
                    headerBuilder.append("Device Address,")
                    headerBuilder.append("Node ID,")

                    // Add sensor-specific headers
                    val sensorType = if (historicalData.isNotEmpty()) historicalData[0].sensorData else null
                    when (sensorType) {
                        is BluetoothScanViewModel.SensorData.SHT40Data -> {
                            headerBuilder.append("Temperature (°C),")
                            headerBuilder.append("Humidity (%)")
                        }
                        is BluetoothScanViewModel.SensorData.LIS2DHData -> {
                            headerBuilder.append("X-Axis (m/s²),")
                            headerBuilder.append("Y-Axis (m/s²),")
                            headerBuilder.append("Z-Axis (m/s²)")
                        }
                        is BluetoothScanViewModel.SensorData.SoilSensorData -> {
                            headerBuilder.append("Nitrogen (mg/kg),")
                            headerBuilder.append("Phosphorus (mg/kg),")
                            headerBuilder.append("Potassium (mg/kg),")
                            headerBuilder.append("Moisture (%),")
                            headerBuilder.append("Temperature (°C),")
                            headerBuilder.append("Electric Conductivity (mS/cm),")
                            headerBuilder.append("pH")
                        }
                        is BluetoothScanViewModel.SensorData.LuxData -> {
                            headerBuilder.append("Light Intensity (LUX)")
                        }
                        is BluetoothScanViewModel.SensorData.SDTData -> {
                            headerBuilder.append("Speed (m/s),")
                            headerBuilder.append("Distance (m)")
                        }
                        is BluetoothScanViewModel.SensorData.ObjectDetectorData -> {
                            headerBuilder.append("Object Detected")
                        }
                        else -> {}
                    }
                    headerBuilder.append("\n")
                    outputStream.write(headerBuilder.toString().toByteArray())

                    // Format for timestamps
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())

                    // Write each data row
                    historicalData.forEach { entry ->
                        val dataBuilder = StringBuilder()
                        // Add timestamp
                        dataBuilder.append("${dateFormat.format(Date(entry.timestamp))},")
                        // Add device info
                        dataBuilder.append("$deviceName,")
                        dataBuilder.append("$deviceAddress,")
                        dataBuilder.append("$deviceId,")

                        // Add sensor-specific data
                        when (val sensorData = entry.sensorData) {
                            is BluetoothScanViewModel.SensorData.SHT40Data -> {
                                dataBuilder.append("${sensorData.temperature},")
                                dataBuilder.append(sensorData.humidity)
                            }
                            is BluetoothScanViewModel.SensorData.LIS2DHData -> {
                                dataBuilder.append("${sensorData.x},")
                                dataBuilder.append("${sensorData.y},")
                                dataBuilder.append(sensorData.z)
                            }
                            is BluetoothScanViewModel.SensorData.SoilSensorData -> {
                                dataBuilder.append("${sensorData.nitrogen},")
                                dataBuilder.append("${sensorData.phosphorus},")
                                dataBuilder.append("${sensorData.potassium},")
                                dataBuilder.append("${sensorData.moisture},")
                                dataBuilder.append("${sensorData.temperature},")
                                dataBuilder.append("${sensorData.ec},")
                                dataBuilder.append(sensorData.pH)
                            }
                            is BluetoothScanViewModel.SensorData.LuxData -> {
                                dataBuilder.append("${sensorData.calculatedLux}")
                            }
                            is BluetoothScanViewModel.SensorData.SDTData -> {
                                dataBuilder.append("${sensorData.speed},")
                                dataBuilder.append(sensorData.distance)
                            }
                            is BluetoothScanViewModel.SensorData.ObjectDetectorData -> {
                                dataBuilder.append("${sensorData.detection}")
                            }
                            null -> {}
                        }
                        dataBuilder.append("\n")
                        outputStream.write(dataBuilder.toString().toByteArray())

                        // Flush periodically to avoid memory build-up
                        if (historicalData.indexOf(entry) % 100 == 0) {
                            outputStream.flush()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}


@Composable
private fun HeaderSection(
    navController: NavController,
    viewModel: BluetoothScanViewModel,
    deviceAddress: String // Add this parameter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                viewModel.stopScan()
                navController.popBackStack()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "Advertising Data",
            fontFamily = helveticaFont,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        IconButton(
            onClick = {
                navController.navigate("chart_screen/$deviceAddress")
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.graph),
                contentDescription = "Graph Icon",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun DeviceInfoSection(
    deviceName: String,
    deviceAddress: String,
    deviceId: String
) {
    InfoCard(text = "Device Name: $deviceName ($deviceAddress)")
    Spacer(modifier = Modifier.height(8.dp))
    InfoCard(text = "Node ID: $deviceId")
}

@Composable
private fun InfoCard(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0A8AE6)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2A9EE5), Color(0xFF076FB8))
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LuxAnimationSection(luxData: BluetoothScanViewModel.SensorData.LuxData) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        SunWithRayAnimation(lux = luxData.calculatedLux)
    }
}

@Composable
private fun DownloadButton() {
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

@Composable
fun ResponsiveDataCards(data: List<Pair<String, String>>) {
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
                data.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { (label, value) ->
                            DataCard(label = label, value = value)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
        100.dp,  // Stage 1: Short rays
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
