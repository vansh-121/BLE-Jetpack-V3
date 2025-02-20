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

@Composable
fun ChartScreen(
    navController: NavController,
    deviceAddress: String? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = remember { BluetoothScanViewModelFactory(application) }
    val viewModel: BluetoothScanViewModel = viewModel(factory = factory)

    // Collect temperature data from the device
    val temperatureData by remember(deviceAddress) {
        viewModel.devices
            .map { devices ->
                devices
                    .find { it.address == deviceAddress }
                    ?.sensorData
                    ?.let { data ->
                        when (data) {
                            is BluetoothScanViewModel.SensorData.SHT40Data -> {
                                data.temperature.toFloatOrNull() ?: 0f
                            }

                            is BluetoothScanViewModel.SensorData.SoilSensorData -> {
                                data.temperature.toFloatOrNull() ?: 0f
                            }

                            else -> null
                        }
                    }
            }
    }.collectAsState(initial = null)

    // Store historical temperature values
    val temperatureHistory = remember { mutableStateListOf<Float>() }


    // Start scanning when entering the screen
    LaunchedEffect(Unit) {
        viewModel.startScan(context as Activity)
    }

    val isReceivingData = remember { mutableStateOf(false) }

    LaunchedEffect(temperatureData) {
        temperatureData?.let {
            isReceivingData.value = true
        }
    }

// Update the waiting message to be more informative
    if (!isReceivingData.value) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Waiting for temperature data...",
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


    // Update history when new temperature data arrives
    LaunchedEffect(temperatureData) {
        temperatureData?.let { temp ->
            if (temperatureHistory.size >= 10) {
                temperatureHistory.removeAt(0)
            }
            temperatureHistory.add(temp)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Temperature Chart",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Temperature Chart Card
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
                            "Real-time Temperature",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Current: ${temperatureData?.toString() ?: "N/A"}°C",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Temperature Graph
                        if (temperatureHistory.isNotEmpty()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                val points = temperatureHistory.toList()
                                if (points.isNotEmpty()) {
                                    val maxTemp = points.maxOrNull() ?: 0f
                                    val minTemp = (points.minOrNull() ?: 0f).coerceAtMost(maxTemp - 1f)  // Ensure range
                                    val tempRange = (maxTemp - minTemp).coerceAtLeast(1f)

                                    val stepX = size.width / (points.size.coerceAtLeast(2) - 1)
                                    val heightPadding = size.height * 0.1f // 10% padding top and bottom

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
                                                (1 - (points[i] - minTemp) / tempRange)
                                        val y2 = heightPadding + (size.height - 2 * heightPadding) *
                                                (1 - (points[i + 1] - minTemp) / tempRange)

                                        // Draw line between points
                                        drawLine(
                                            color = Color(0xFF0A74DA),
                                            start = Offset(x1, y1),
                                            end = Offset(x2, y2),
                                            strokeWidth = 4f,
                                            pathEffect = PathEffect.cornerPathEffect(10f)
                                        )

                                        // Draw points
                                        drawCircle(
                                            color = Color(0xFF0A74DA),
                                            radius = 6f,
                                            center = Offset(x1, y1)
                                        )
                                    }

                                    // Draw last point
                                    if (points.isNotEmpty()) {
                                        val lastX = (points.size - 1) * stepX
                                        val lastY = heightPadding + (size.height - 2 * heightPadding) *
                                                (1 - (points.last() - minTemp) / tempRange)
                                        drawCircle(
                                            color = Color(0xFF0A74DA),
                                            radius = 6f,
                                            center = Offset(lastX, lastY)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Waiting for temperature data...",
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }
                    }
                }
            }




        }
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewChartScreen() {
//    ChartScreen()
//}