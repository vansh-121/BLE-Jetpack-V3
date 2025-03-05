package com.example.ble_jetpackcompose

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
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
import androidx.compose.runtime.setValue
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// graph.kt
@Composable
fun ChartScreen(
    navController: NavController,
    deviceAddress: String? = null
) {
    // Get application context for creating the ViewModel
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = remember { BluetoothScanViewModelFactory(application) }
    val viewModel: BluetoothScanViewModel<Any?> = viewModel(factory = factory)

    // Collect sensor data from the device using the device address
    val sensorData by remember(deviceAddress) {
        viewModel.devices
            .map { devices ->
                devices.find { it.address == deviceAddress }?.sensorData
            }
    }.collectAsState(initial = null)

    // Extract all sensor data values from the different possible sensor types
    val temperatureData = (sensorData as? BluetoothScanViewModel.SensorData.SHT40Data)?.temperature?.toFloatOrNull()
    val humidityData = (sensorData as? BluetoothScanViewModel.SensorData.SHT40Data)?.humidity?.toFloatOrNull()
    val speedData = (sensorData as? BluetoothScanViewModel.SensorData.SDTData)?.speed?.toFloatOrNull()
    val distanceData = (sensorData as? BluetoothScanViewModel.SensorData.SDTData)?.distance?.toFloatOrNull()
    val xAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.x?.toFloatOrNull()
    val yAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.y?.toFloatOrNull()
    val zAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.z?.toFloatOrNull()

    // Extract soil sensor data values
    val soilMoistureData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.moisture?.toFloatOrNull()
    val soilTemperatureData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.temperature?.toFloatOrNull()
    val soilNitrogenData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.nitrogen?.toFloatOrNull()
    val soilPhosphorusData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.phosphorus?.toFloatOrNull()
    val soilPotassiumData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.potassium?.toFloatOrNull()
    val soilEcData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.ec?.toFloatOrNull()
    val soilPhData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.pH?.toFloatOrNull()

    // Create lists to store historical values for all measurements (last 20 readings)
    val temperatureHistory = remember { mutableStateListOf<Float>() }
    val humidityHistory = remember { mutableStateListOf<Float>() }
    val speedHistory = remember { mutableStateListOf<Float>() }
    val distanceHistory = remember { mutableStateListOf<Float>() }
    val xAxisHistory = remember { mutableStateListOf<Float>() }
    val yAxisHistory = remember { mutableStateListOf<Float>() }
    val zAxisHistory = remember { mutableStateListOf<Float>() }

    // Create lists for soil sensor historical values
    val soilMoistureHistory = remember { mutableStateListOf<Float>() }
    val soilTemperatureHistory = remember { mutableStateListOf<Float>() }
    val soilNitrogenHistory = remember { mutableStateListOf<Float>() }
    val soilPhosphorusHistory = remember { mutableStateListOf<Float>() }
    val soilPotassiumHistory = remember { mutableStateListOf<Float>() }
    val soilEcHistory = remember { mutableStateListOf<Float>() }
    val soilPhHistory = remember { mutableStateListOf<Float>() }

    // Store timestamps for sensor readings
    val soilDataTimestamps = remember { mutableStateListOf<String>() }
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Start Bluetooth scanning when entering the screen
    LaunchedEffect(Unit) {
        viewModel.startScan(context as Activity)
    }

    // Track if we're receiving any data to show waiting message if needed
    val isReceivingData = remember { mutableStateOf(false) }

    // Determine if we have soil sensor data
    val hasSoilSensorData = remember { mutableStateOf(false) }

    // Track if soil sensor data is clicked (to show table view)
    var isSoilSensorClicked by remember { mutableStateOf(false) }

    // Track which view is selected (Graph or Table) - only for soil sensor
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Graphs", "Soil Data Table")

    // Set isReceivingData and hasSoilSensorData based on incoming data
    LaunchedEffect(sensorData) {
        if (temperatureData != null || humidityData != null || speedData != null ||
            distanceData != null || xAxisData != null || yAxisData != null || zAxisData != null) {
            isReceivingData.value = true
        }

        if (soilMoistureData != null || soilTemperatureData != null || soilNitrogenData != null ||
            soilPhosphorusData != null || soilPotassiumData != null || soilEcData != null || soilPhData != null) {
            isReceivingData.value = true
            hasSoilSensorData.value = true
        }
    }

    // Update history lists when new data arrives
    // Maintain maximum of 20 data points by removing oldest when exceeding the limit
    LaunchedEffect(temperatureData, humidityData, speedData, distanceData, xAxisData, yAxisData, zAxisData,
        soilMoistureData, soilTemperatureData, soilNitrogenData, soilPhosphorusData, soilPotassiumData, soilEcData, soilPhData) {

        // Update SHT40 sensor data history
        temperatureData?.let {
            if (temperatureHistory.size >= 20) temperatureHistory.removeAt(0)
            temperatureHistory.add(it)
        }

        humidityData?.let {
            if (humidityHistory.size >= 20) humidityHistory.removeAt(0)
            humidityHistory.add(it)
        }

        // Update SDT sensor data history
        speedData?.let {
            if (speedHistory.size >= 20) speedHistory.removeAt(0)
            speedHistory.add(it)
        }

        distanceData?.let {
            if (distanceHistory.size >= 20) distanceHistory.removeAt(0)
            distanceHistory.add(it)
        }

        // Update LIS2DH sensor data history
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

        // Update soil sensor data history
        val shouldAddTimestamp = soilMoistureData != null || soilTemperatureData != null ||
                soilNitrogenData != null || soilPhosphorusData != null || soilPotassiumData != null ||
                soilEcData != null || soilPhData != null

        if (shouldAddTimestamp) {
            if (soilDataTimestamps.size >= 20) soilDataTimestamps.removeAt(0)
            soilDataTimestamps.add(dateFormat.format(Date()))
        }

        soilMoistureData?.let {
            if (soilMoistureHistory.size >= 20) soilMoistureHistory.removeAt(0)
            soilMoistureHistory.add(it)
        }

        soilTemperatureData?.let {
            if (soilTemperatureHistory.size >= 20) soilTemperatureHistory.removeAt(0)
            soilTemperatureHistory.add(it)
        }

        soilNitrogenData?.let {
            if (soilNitrogenHistory.size >= 20) soilNitrogenHistory.removeAt(0)
            soilNitrogenHistory.add(it)
        }

        soilPhosphorusData?.let {
            if (soilPhosphorusHistory.size >= 20) soilPhosphorusHistory.removeAt(0)
            soilPhosphorusHistory.add(it)
        }

        soilPotassiumData?.let {
            if (soilPotassiumHistory.size >= 20) soilPotassiumHistory.removeAt(0)
            soilPotassiumHistory.add(it)
        }

        soilEcData?.let {
            if (soilEcHistory.size >= 20) soilEcHistory.removeAt(0)
            soilEcHistory.add(it)
        }

        soilPhData?.let {
            if (soilPhHistory.size >= 20) soilPhHistory.removeAt(0)
            soilPhHistory.add(it)
        }
    }

    // Create the UI with a Scaffold for top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Graphs",
                        fontFamily = helveticaFont,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    // Back button to navigate to previous screen
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Export button for exporting data to tables
                    IconButton(onClick = { /* Handle Export */ }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export")
                    }
                    // Options button
                    IconButton(onClick = { /* Handle Options */ }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Options")
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        // Main content area with gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(Color.White, Color.LightGray))
                )
                .padding(paddingValues)
        ) {
            Column {
                // Show tab selector only when soil sensor is clicked and has data
                if (isSoilSensorClicked && hasSoilSensorData.value) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        backgroundColor = Color.Transparent,
                        contentColor = Color(0xFF0A74DA)
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title) },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index }
                            )
                        }
                    }
                }

                if (!isSoilSensorClicked || (isSoilSensorClicked && selectedTabIndex == 0)) {
                    // Graph View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // SHT40 Temperature and Humidity sensors
                        if (sensorData is BluetoothScanViewModel.SensorData.SHT40Data) {
                            item {
                                SensorGraphCard(
                                    title = "Temperature (°C)",
                                    currentValue = temperatureData,
                                    history = temperatureHistory,
                                    color = Color(0xFFE53935) // Red
                                )
                            }
                            item {
                                SensorGraphCard(
                                    title = "Humidity (%)",
                                    currentValue = humidityData,
                                    history = humidityHistory,
                                    color = Color(0xFF1976D2) // Blue
                                )
                            }
                        }

                        // SDT Speed and Distance sensors
                        if (sensorData is BluetoothScanViewModel.SensorData.SDTData) {
                            item {
                                SensorGraphCard(
                                    title = "Speed (m/s)",
                                    currentValue = speedData,
                                    history = speedHistory,
                                    color = Color(0xFF43A047) // Green
                                )
                            }
                            item {
                                SensorGraphCard(
                                    title = "Distance (m)",
                                    currentValue = distanceData,
                                    history = distanceHistory,
                                    color = Color(0xFFFFB300) // Amber
                                )
                            }
                        }

                        // LIS2DH Accelerometer sensors
                        if (sensorData is BluetoothScanViewModel.SensorData.LIS2DHData) {
                            item {
                                SensorGraphCard(
                                    title = "X Axis (g)",
                                    currentValue = xAxisData,
                                    history = xAxisHistory,
                                    color = Color(0xFFE91E63) // Pink
                                )
                            }
                            item {
                                SensorGraphCard(
                                    title = "Y Axis (g)",
                                    currentValue = yAxisData,
                                    history = yAxisHistory,
                                    color = Color(0xFF9C27B0) // Purple
                                )
                            }
                            item {
                                SensorGraphCard(
                                    title = "Z Axis (g)",
                                    currentValue = zAxisData,
                                    history = zAxisHistory,
                                    color = Color(0xFF009688) // Teal
                                )
                            }
                        }

                        // Soil sensor data
                        if (sensorData is BluetoothScanViewModel.SensorData.SoilSensorData) {
                            // Create a clickable card that wraps all soil sensor graphs
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isSoilSensorClicked = true },
                                    elevation = 2.dp,
                                    backgroundColor = if (isSoilSensorClicked) Color(0xFFE3F2FD) else MaterialTheme.colors.surface
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text(
                                            "Soil Sensor Data (Click for detailed view)",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0A74DA),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        SensorGraphCard(
                                            title = "Soil Moisture (%)",
                                            currentValue = soilMoistureData,
                                            history = soilMoistureHistory,
                                            color = Color(0xFF6200EA)  // Deep Purple
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SensorGraphCard(
                                            title = "Soil Temperature (°C)",
                                            currentValue = soilTemperatureData,
                                            history = soilTemperatureHistory,
                                            color = Color(0xFFFF6D00)  // Deep Orange
                                        )

                                        // Only show a preview in the main screen
                                        if (!isSoilSensorClicked) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 16.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    "Click to view all soil parameters and table view",
                                                    color = Color(0xFF0A74DA)
                                                )
                                            }
                                        } else {
                                            // Show all soil parameters when clicked
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(
                                                title = "Soil Nitrogen (ppm)",
                                                currentValue = soilNitrogenData,
                                                history = soilNitrogenHistory,
                                                color = Color(0xFF00897B)  // Teal
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(
                                                title = "Soil Phosphorus (ppm)",
                                                currentValue = soilPhosphorusData,
                                                history = soilPhosphorusHistory,
                                                color = Color(0xFFC2185B)  // Pink
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(
                                                title = "Soil Potassium (ppm)",
                                                currentValue = soilPotassiumData,
                                                history = soilPotassiumHistory,
                                                color = Color(0xFF7B1FA2)  // Purple
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(
                                                title = "Soil EC (µS/cm)",
                                                currentValue = soilEcData,
                                                history = soilEcHistory,
                                                color = Color(0xFFF57C00)  // Orange
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(
                                                title = "Soil pH",
                                                currentValue = soilPhData,
                                                history = soilPhHistory,
                                                color = Color(0xFFD32F2F)  // Red
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Show waiting message if no data is being received
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
                } else if (isSoilSensorClicked && selectedTabIndex == 1) {
                    // Table View for Soil Sensor Data (shown only when soil sensor is clicked and tab is selected)
                    SoilSensorDataTable(
                        soilMoistureHistory = soilMoistureHistory,
                        soilTemperatureHistory = soilTemperatureHistory,
                        soilNitrogenHistory = soilNitrogenHistory,
                        soilPhosphorusHistory = soilPhosphorusHistory,
                        soilPotassiumHistory = soilPotassiumHistory,
                        soilEcHistory = soilEcHistory,
                        soilPhHistory = soilPhHistory,
                        timestamps = soilDataTimestamps,
                        isReceivingData = isReceivingData.value &&
                                (soilMoistureHistory.isNotEmpty() ||
                                        soilTemperatureHistory.isNotEmpty() ||
                                        soilNitrogenHistory.isNotEmpty() ||
                                        soilPhosphorusHistory.isNotEmpty() ||
                                        soilPotassiumHistory.isNotEmpty() ||
                                        soilEcHistory.isNotEmpty() ||
                                        soilPhHistory.isNotEmpty())
                    )
                }

                // If soil sensor is clicked, show a button to go back to all sensors view
                if (isSoilSensorClicked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF0A74DA),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { isSoilSensorClicked = false }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Back to All Sensors",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
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
            // Display title of the sensor
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Display current value or N/A if null
            Text(
                "Current: ${currentValue?.toString() ?: "N/A"}",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Draw the graph if we have history data
            if (history.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val points = history.toList()
                    if (points.isNotEmpty()) {
                        // Calculate value range for scaling the graph
                        val maxValue = points.maxOrNull() ?: 0f
                        val minValue = (points.minOrNull() ?: 0f).coerceAtMost(maxValue - 1f)
                        val range = (maxValue - minValue).coerceAtLeast(1f)

                        // Calculate horizontal step between points
                        val stepX = size.width / (points.size.coerceAtLeast(2) - 1)
                        val heightPadding = size.height * 0.1f

                        // Draw X-axis line
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height - heightPadding),
                            end = Offset(size.width, size.height - heightPadding),
                            strokeWidth = 1f
                        )

                        // Draw lines connecting data points
                        for (i in 0 until points.size - 1) {
                            val x1 = i * stepX
                            val x2 = (i + 1) * stepX

                            // Calculate Y positions based on value scaling
                            val y1 = heightPadding + (size.height - 2 * heightPadding) *
                                    (1 - (points[i] - minValue) / range)
                            val y2 = heightPadding + (size.height - 2 * heightPadding) *
                                    (1 - (points[i + 1] - minValue) / range)

                            // Draw line between points with smooth corners
                            drawLine(
                                color = color,
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 4f,
                                pathEffect = PathEffect.cornerPathEffect(10f)
                            )

                            // Draw circle at each data point
                            drawCircle(
                                color = color,
                                radius = 6f,
                                center = Offset(x1, y1)
                            )
                        }

                        // Draw the last point since the loop only handles up to points.size-2
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
                // Display waiting message if no data is available yet
                Text(
                    "Waiting for data...",
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}