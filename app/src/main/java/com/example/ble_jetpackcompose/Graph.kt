//greaph.kt

package com.example.ble_jetpackcompose

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
//greaph.kt

@Composable
fun ChartScreen(
    navController: NavController,
    deviceAddress: String? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = remember { BluetoothScanViewModelFactory(application) }
    val viewModel: BluetoothScanViewModel = viewModel(factory = factory)

    // Collect sensor data from the device
    val sensorData by remember(deviceAddress) {
        viewModel.devices
            .map { devices ->
                devices.find { it.address == deviceAddress }?.sensorData
            }
    }.collectAsState(initial = null)

    // Extract all sensor data
    val temperatureData = (sensorData as? BluetoothScanViewModel.SensorData.SHT40Data)?.temperature?.toFloatOrNull()
    val humidityData = (sensorData as? BluetoothScanViewModel.SensorData.SHT40Data)?.humidity?.toFloatOrNull()
    val speedData = (sensorData as? BluetoothScanViewModel.SensorData.SDTData)?.speed?.toFloatOrNull()
    val distanceData = (sensorData as? BluetoothScanViewModel.SensorData.SDTData)?.distance?.toFloatOrNull()
    val xAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.x?.toFloatOrNull()
    val yAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.y?.toFloatOrNull()
    val zAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.z?.toFloatOrNull()

    // Store historical values for all measurements
    val temperatureHistory = remember { mutableStateListOf<Float>() }
    val humidityHistory = remember { mutableStateListOf<Float>() }
    val speedHistory = remember { mutableStateListOf<Float>() }
    val distanceHistory = remember { mutableStateListOf<Float>() }
    val xAxisHistory = remember { mutableStateListOf<Float>() }
    val yAxisHistory = remember { mutableStateListOf<Float>() }
    val zAxisHistory = remember { mutableStateListOf<Float>() }

    // Start scanning when entering the screen
    LaunchedEffect(Unit) {
        viewModel.startScan(context as Activity)
    }

    val isReceivingData = remember { mutableStateOf(false) }

    LaunchedEffect(sensorData) {
        if (temperatureData != null || humidityData != null || speedData != null ||
            distanceData != null || xAxisData != null || yAxisData != null || zAxisData != null) {
            isReceivingData.value = true
        }
    }

    // Update history when new data arrives
    LaunchedEffect(temperatureData, humidityData, speedData, distanceData, xAxisData, yAxisData, zAxisData) {
        temperatureData?.let {
            if (temperatureHistory.size >= 20) temperatureHistory.removeAt(0)
            temperatureHistory.add(it)
        }

        humidityData?.let {
            if (humidityHistory.size >= 20) humidityHistory.removeAt(0)
            humidityHistory.add(it)
        }

        speedData?.let {
            if (speedHistory.size >= 20) speedHistory.removeAt(0)
            speedHistory.add(it)
        }

        distanceData?.let {
            if (distanceHistory.size >= 20) distanceHistory.removeAt(0)
            distanceHistory.add(it)
        }

        xAxisData?.let {
            if (xAxisHistory.size >= 20) xAxisHistory.removeAt(0)
            xAxisHistory.add(it)
        }

        yAxisData?.let {
            if (yAxisHistory.size >= 20) yAxisHistory.removeAt(0)
            yAxisHistory.add(it)
        }

        zAxisData?.let {
            if (zAxisHistory.size >= 20) zAxisHistory.removeAt(0)
            zAxisHistory.add(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sensor Data Graphs",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Export */ }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export")
                    }
                    IconButton(onClick = { /* Handle Options */ }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Options")
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(Color.White, Color.LightGray))
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (sensorData is BluetoothScanViewModel.SensorData.SHT40Data) {
                    item {
                        SensorGraphCard(
                            title = "Temperature (°C)",
                            currentValue = temperatureData,
                            history = temperatureHistory,
                            color = Color(0xFF0A74DA)
                        )
                    }
                    item {
                        SensorGraphCard(
                            title = "Humidity (%)",
                            currentValue = humidityData,
                            history = humidityHistory,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                if (sensorData is BluetoothScanViewModel.SensorData.SDTData) {
                    item {
                        SensorGraphCard(
                            title = "Speed (m/s)",
                            currentValue = speedData,
                            history = speedHistory,
                            color = Color(0xFFE91E63)
                        )
                    }
                    item {
                        SensorGraphCard(
                            title = "Distance (m)",
                            currentValue = distanceData,
                            history = distanceHistory,
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                if (sensorData is BluetoothScanViewModel.SensorData.LIS2DHData) {
                    item {
                        SensorGraphCard(
                            title = "X-Axis Acceleration (m/s²)",
                            currentValue = xAxisData,
                            history = xAxisHistory,
                            color = Color(0xFFE53935)  // Deep Red
                        )
                    }
                    item {
                        SensorGraphCard(
                            title = "Y-Axis Acceleration (m/s²)",
                            currentValue = yAxisData,
                            history = yAxisHistory,
                            color = Color(0xFF43A047)  // Deep Green
                        )
                    }
                    item {
                        SensorGraphCard(
                            title = "Z-Axis Acceleration (m/s²)",
                            currentValue = zAxisData,
                            history = zAxisHistory,
                            color = Color(0xFF1E88E5)  // Deep Blue
                        )
                    }
                }

                // Waiting message
                if (!isReceivingData.value) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Waiting for sensor data...",
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                            Text(
                                "Make sure the device is connected and sending data",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun SensorGraphCard(title: String, currentValue: Float?, history: List<Float>, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Current: ${currentValue?.toString() ?: "N/A"}",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (history.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val points = history.toList()
                    if (points.isNotEmpty()) {
                        val maxValue = points.maxOrNull() ?: 0f
                        val minValue = (points.minOrNull() ?: 0f).coerceAtMost(maxValue - 1f)
                        val range = (maxValue - minValue).coerceAtLeast(1f)

                        val stepX = size.width / (points.size.coerceAtLeast(2) - 1)
                        val heightPadding = size.height * 0.1f

                        // Draw axis
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height - heightPadding),
                            end = Offset(size.width, size.height - heightPadding),
                            strokeWidth = 1f
                        )

                        // Draw points and lines
                        for (i in 0 until points.size - 1) {
                            val x1 = i * stepX
                            val x2 = (i + 1) * stepX
                            val y1 = heightPadding + (size.height - 2 * heightPadding) *
                                    (1 - (points[i] - minValue) / range)
                            val y2 = heightPadding + (size.height - 2 * heightPadding) *
                                    (1 - (points[i + 1] - minValue) / range)

                            // Draw line between points
                            drawLine(
                                color = color,
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 4f,
                                pathEffect = PathEffect.cornerPathEffect(10f)
                            )

                            // Draw points
                            drawCircle(
                                color = color,
                                radius = 6f,
                                center = Offset(x1, y1)
                            )
                        }

                        // Draw last point
                        val lastX = (points.size - 1) * stepX
                        val lastY = heightPadding + (size.height - 2 * heightPadding) *
                                (1 - (points.last() - minValue) / range)
                        drawCircle(
                            color = color,
                            radius = 6f,
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            } else {
                Text(
                    "Waiting for data...",
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}