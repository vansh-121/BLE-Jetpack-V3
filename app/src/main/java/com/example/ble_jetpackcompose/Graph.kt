package com.example.ble_jetpackcompose

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class for translatable text in ChartScreen
data class TranslatedChartText(
    val graphsTitle: String = "Graphs",
    val temperatureLabel: String = "Temperature (°C)",
    val humidityLabel: String = "Humidity (%)",
    val speedLabel: String = "Speed (m/s)",
    val distanceLabel: String = "Distance (m)",
    val xAxisLabel: String = "X Axis (g)",
    val yAxisLabel: String = "Y Axis (g)",
    val zAxisLabel: String = "Z Axis (g)",
    val soilSensorDataTitle: String = "Soil Sensor Data (Click for detailed view)",
    val soilMoistureLabel: String = "Soil Moisture (%)",
    val soilTemperatureLabel: String = "Soil Temperature (°C)",
    val soilNitrogenLabel: String = "Soil Nitrogen (ppm)",
    val soilPhosphorusLabel: String = "Soil Phosphorus (ppm)",
    val soilPotassiumLabel: String = "Soil Potassium (ppm)",
    val soilEcLabel: String = "Soil EC (µS/cm)",
    val soilPhLabel: String = "Soil pH",
    val clickToViewAll: String = "Click to view all soil parameters and table view",
    val waitingForData: String = "Waiting for data...",
    val waitingForSensorData: String = "Waiting for sensor data...",
    val ensureDeviceConnected: String = "Make sure the device is connected and sending data",
    val currentLabel: String = "Current",
    val naLabel: String = "N/A",
    val graphsTab: String = "Graphs",
    val soilDataTableTab: String = "Soil Data Table",
    val backToAllSensors: String = "Back to All Sensors"
)
@Composable
fun ChartScreen(
    navController: NavController,
    deviceAddress: String? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = remember { BluetoothScanViewModelFactory(application) }
    val viewModel: BluetoothScanViewModel<Any?> = viewModel(factory = factory)

    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Collect sensor data
    val sensorData by remember(deviceAddress) {
        viewModel.devices
            .map { devices -> devices.find { it.address == deviceAddress }?.sensorData }
    }.collectAsState(initial = null)

    // Extract sensor data values
    val temperatureData = (sensorData as? BluetoothScanViewModel.SensorData.SHT40Data)?.temperature?.toFloatOrNull()
    val humidityData = (sensorData as? BluetoothScanViewModel.SensorData.SHT40Data)?.humidity?.toFloatOrNull()
    val speedData = (sensorData as? BluetoothScanViewModel.SensorData.SDTData)?.speed?.toFloatOrNull()
    val distanceData = (sensorData as? BluetoothScanViewModel.SensorData.SDTData)?.distance?.toFloatOrNull()
    val xAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.x?.toFloatOrNull()
    val yAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.y?.toFloatOrNull()
    val zAxisData = (sensorData as? BluetoothScanViewModel.SensorData.LIS2DHData)?.z?.toFloatOrNull()
    val soilMoistureData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.moisture?.toFloatOrNull()
    val soilTemperatureData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.temperature?.toFloatOrNull()
    val soilNitrogenData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.nitrogen?.toFloatOrNull()
    val soilPhosphorusData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.phosphorus?.toFloatOrNull()
    val soilPotassiumData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.potassium?.toFloatOrNull()
    val soilEcData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.ec?.toFloatOrNull()
    val soilPhData = (sensorData as? BluetoothScanViewModel.SensorData.SoilSensorData)?.pH?.toFloatOrNull()

    // Historical data lists
    val temperatureHistory = remember { mutableStateListOf<Float>() }
    val humidityHistory = remember { mutableStateListOf<Float>() }
    val speedHistory = remember { mutableStateListOf<Float>() }
    val distanceHistory = remember { mutableStateListOf<Float>() }
    val xAxisHistory = remember { mutableStateListOf<Float>() }
    val yAxisHistory = remember { mutableStateListOf<Float>() }
    val zAxisHistory = remember { mutableStateListOf<Float>() }
    val soilMoistureHistory = remember { mutableStateListOf<Float>() }
    val soilTemperatureHistory = remember { mutableStateListOf<Float>() }
    val soilNitrogenHistory = remember { mutableStateListOf<Float>() }
    val soilPhosphorusHistory = remember { mutableStateListOf<Float>() }
    val soilPotassiumHistory = remember { mutableStateListOf<Float>() }
    val soilEcHistory = remember { mutableStateListOf<Float>() }
    val soilPhHistory = remember { mutableStateListOf<Float>() }
    val soilDataTimestamps = remember { mutableStateListOf<String>() }
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedChartText(
                graphsTitle = TranslationCache.get("Graphs-$currentLanguage") ?: "Graphs",
                temperatureLabel = TranslationCache.get("Temperature (°C)-$currentLanguage") ?: "Temperature (°C)",
                humidityLabel = TranslationCache.get("Humidity (%)-$currentLanguage") ?: "Humidity (%)",
                speedLabel = TranslationCache.get("Speed (m/s)-$currentLanguage") ?: "Speed (m/s)",
                distanceLabel = TranslationCache.get("Distance (m)-$currentLanguage") ?: "Distance (m)",
                xAxisLabel = TranslationCache.get("X Axis (g)-$currentLanguage") ?: "X Axis (g)",
                yAxisLabel = TranslationCache.get("Y Axis (g)-$currentLanguage") ?: "Y Axis (g)",
                zAxisLabel = TranslationCache.get("Z Axis (g)-$currentLanguage") ?: "Z Axis (g)",
                soilSensorDataTitle = TranslationCache.get("Soil Sensor Data (Click for detailed view)-$currentLanguage") ?: "Soil Sensor Data (Click for detailed view)",
                soilMoistureLabel = TranslationCache.get("Soil Moisture (%)-$currentLanguage") ?: "Soil Moisture (%)",
                soilTemperatureLabel = TranslationCache.get("Soil Temperature (°C)-$currentLanguage") ?: "Soil Temperature (°C)",
                soilNitrogenLabel = TranslationCache.get("Soil Nitrogen (ppm)-$currentLanguage") ?: "Soil Nitrogen (ppm)",
                soilPhosphorusLabel = TranslationCache.get("Soil Phosphorus (ppm)-$currentLanguage") ?: "Soil Phosphorus (ppm)",
                soilPotassiumLabel = TranslationCache.get("Soil Potassium (ppm)-$currentLanguage") ?: "Soil Potassium (ppm)",
                soilEcLabel = TranslationCache.get("Soil EC (µS/cm)-$currentLanguage") ?: "Soil EC (µS/cm)",
                soilPhLabel = TranslationCache.get("Soil pH-$currentLanguage") ?: "Soil pH",
                clickToViewAll = TranslationCache.get("Click to view all soil parameters and table view-$currentLanguage") ?: "Click to view all soil parameters and table view",
                waitingForData = TranslationCache.get("Waiting for data...-$currentLanguage") ?: "Waiting for data...",
                waitingForSensorData = TranslationCache.get("Waiting for sensor data...-$currentLanguage") ?: "Waiting for sensor data...",
                ensureDeviceConnected = TranslationCache.get("Make sure the device is connected and sending data-$currentLanguage") ?: "Make sure the device is connected and sending data",
                currentLabel = TranslationCache.get("Current-$currentLanguage") ?: "Current",
                naLabel = TranslationCache.get("N/A-$currentLanguage") ?: "N/A",
                graphsTab = TranslationCache.get("Graphs-$currentLanguage") ?: "Graphs",
                soilDataTableTab = TranslationCache.get("Soil Data Table-$currentLanguage") ?: "Soil Data Table",
                backToAllSensors = TranslationCache.get("Back to All Sensors-$currentLanguage") ?: "Back to All Sensors"
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf(
            "Graphs", "Temperature (°C)", "Humidity (%)", "Speed (m/s)", "Distance (m)",
            "X Axis (g)", "Y Axis (g)", "Z Axis (g)", "Soil Sensor Data (Click for detailed view)",
            "Soil Moisture (%)", "Soil Temperature (°C)", "Soil Nitrogen (ppm)", "Soil Phosphorus (ppm)",
            "Soil Potassium (ppm)", "Soil EC (µS/cm)", "Soil pH",
            "Click to view all soil parameters and table view", "Waiting for data...",
            "Waiting for sensor data...", "Make sure the device is connected and sending data",
            "Current", "N/A", "Graphs", "Soil Data Table", "Back to All Sensors"
        )
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedChartText(
            graphsTitle = translatedList[0],
            temperatureLabel = translatedList[1],
            humidityLabel = translatedList[2],
            speedLabel = translatedList[3],
            distanceLabel = translatedList[4],
            xAxisLabel = translatedList[5],
            yAxisLabel = translatedList[6],
            zAxisLabel = translatedList[7],
            soilSensorDataTitle = translatedList[8],
            soilMoistureLabel = translatedList[9],
            soilTemperatureLabel = translatedList[10],
            soilNitrogenLabel = translatedList[11],
            soilPhosphorusLabel = translatedList[12],
            soilPotassiumLabel = translatedList[13],
            soilEcLabel = translatedList[14],
            soilPhLabel = translatedList[15],
            clickToViewAll = translatedList[16],
            waitingForData = translatedList[17],
            waitingForSensorData = translatedList[18],
            ensureDeviceConnected = translatedList[19],
            currentLabel = translatedList[20],
            naLabel = translatedList[21],
            graphsTab = translatedList[22],
            soilDataTableTab = translatedList[23],
            backToAllSensors = translatedList[24]
        )
    }

    // Theme-based colors
    val backgroundGradient = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF121212), Color(0xFF424242)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color.LightGray))
    }
    val cardBackground = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
    val accentColor = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF0A74DA)
    val tabBackground = if (isDarkMode) Color(0xFF2A2A2A) else Color.Transparent
    val appBarBackground = if (isDarkMode) Color(0xFF121212) else Color.Transparent // Updated for TopAppBar

    // State variables
    val isReceivingData = remember { mutableStateOf(false) }
    val hasSoilSensorData = remember { mutableStateOf(false) }
    var isSoilSensorClicked by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf(translatedText.graphsTab, translatedText.soilDataTableTab)

    // Start scanning
    LaunchedEffect(Unit) {
        viewModel.startScan(context as Activity)
    }

    // Update data states
    LaunchedEffect(sensorData) {
        if (temperatureData != null || humidityData != null || speedData != null ||
            distanceData != null || xAxisData != null || yAxisData != null || zAxisData != null ||
            soilMoistureData != null || soilTemperatureData != null || soilNitrogenData != null ||
            soilPhosphorusData != null || soilPotassiumData != null || soilEcData != null || soilPhData != null) {
            isReceivingData.value = true
        }
        if (soilMoistureData != null || soilTemperatureData != null || soilNitrogenData != null ||
            soilPhosphorusData != null || soilPotassiumData != null || soilEcData != null || soilPhData != null) {
            hasSoilSensorData.value = true
        }
    }

    // Update history lists
    LaunchedEffect(temperatureData, humidityData, speedData, distanceData, xAxisData, yAxisData, zAxisData,
        soilMoistureData, soilTemperatureData, soilNitrogenData, soilPhosphorusData, soilPotassiumData, soilEcData, soilPhData) {
        temperatureData?.let { updateHistory(temperatureHistory, it) }
        humidityData?.let { updateHistory(humidityHistory, it) }
        speedData?.let { updateHistory(speedHistory, it) }
        distanceData?.let { updateHistory(distanceHistory, it) }
        xAxisData?.let { updateHistory(xAxisHistory, it) }
        yAxisData?.let { updateHistory(yAxisHistory, it) }
        zAxisData?.let { updateHistory(zAxisHistory, it) }
        val shouldAddTimestamp = soilMoistureData != null || soilTemperatureData != null ||
                soilNitrogenData != null || soilPhosphorusData != null || soilPotassiumData != null ||
                soilEcData != null || soilPhData != null
        if (shouldAddTimestamp) {
            if (soilDataTimestamps.size >= 20) soilDataTimestamps.removeAt(0)
            soilDataTimestamps.add(dateFormat.format(Date()))
        }
        soilMoistureData?.let { updateHistory(soilMoistureHistory, it) }
        soilTemperatureData?.let { updateHistory(soilTemperatureHistory, it) }
        soilNitrogenData?.let { updateHistory(soilNitrogenHistory, it) }
        soilPhosphorusData?.let { updateHistory(soilPhosphorusHistory, it) }
        soilPotassiumData?.let { updateHistory(soilPotassiumHistory, it) }
        soilEcData?.let { updateHistory(soilEcHistory, it) }
        soilPhData?.let { updateHistory(soilPhHistory, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        translatedText.graphsTitle,
                        fontFamily = helveticaFont,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Export */ }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export", tint = textColor)
                    }
                    IconButton(onClick = { /* Handle Options */ }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Options", tint = textColor)
                    }
                },
                backgroundColor = appBarBackground, // Updated to use theme-based color
                elevation = 0.dp
            )
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column {
                if (isSoilSensorClicked && hasSoilSensorData.value) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        backgroundColor = tabBackground,
                        contentColor = accentColor
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title, color = textColor) },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index }
                            )
                        }
                    }
                }

                if (!isSoilSensorClicked || (isSoilSensorClicked && selectedTabIndex == 0)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (sensorData is BluetoothScanViewModel.SensorData.SHT40Data) {
                            item { SensorGraphCard(translatedText.temperatureLabel, temperatureData, temperatureHistory, Color(0xFFE53935), cardBackground, textColor, secondaryTextColor, translatedText) }
                            item { SensorGraphCard(translatedText.humidityLabel, humidityData, humidityHistory, Color(0xFF1976D2), cardBackground, textColor, secondaryTextColor, translatedText) }
                        }
                        if (sensorData is BluetoothScanViewModel.SensorData.SDTData) {
                            item { SensorGraphCard(translatedText.speedLabel, speedData, speedHistory, Color(0xFF43A047), cardBackground, textColor, secondaryTextColor, translatedText) }
                            item { SensorGraphCard(translatedText.distanceLabel, distanceData, distanceHistory, Color(0xFFFFB300), cardBackground, textColor, secondaryTextColor, translatedText) }
                        }
                        if (sensorData is BluetoothScanViewModel.SensorData.LIS2DHData) {
                            item { SensorGraphCard(translatedText.xAxisLabel, xAxisData, xAxisHistory, Color(0xFFE91E63), cardBackground, textColor, secondaryTextColor, translatedText) }
                            item { SensorGraphCard(translatedText.yAxisLabel, yAxisData, yAxisHistory, Color(0xFF9C27B0), cardBackground, textColor, secondaryTextColor, translatedText) }
                            item { SensorGraphCard(translatedText.zAxisLabel, zAxisData, zAxisHistory, Color(0xFF009688), cardBackground, textColor, secondaryTextColor, translatedText) }
                        }
                        if (sensorData is BluetoothScanViewModel.SensorData.SoilSensorData) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isSoilSensorClicked = true },
                                    elevation = 2.dp,
                                    backgroundColor = if (isSoilSensorClicked) accentColor.copy(alpha = 0.1f) else cardBackground
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            translatedText.soilSensorDataTitle,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        SensorGraphCard(translatedText.soilMoistureLabel, soilMoistureData, soilMoistureHistory, Color(0xFF6200EA), cardBackground, textColor, secondaryTextColor, translatedText)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SensorGraphCard(translatedText.soilTemperatureLabel, soilTemperatureData, soilTemperatureHistory, Color(0xFFFF6D00), cardBackground, textColor, secondaryTextColor, translatedText)
                                        if (!isSoilSensorClicked) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 16.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(translatedText.clickToViewAll, color = accentColor)
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(translatedText.soilNitrogenLabel, soilNitrogenData, soilNitrogenHistory, Color(0xFF00897B), cardBackground, textColor, secondaryTextColor, translatedText)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(translatedText.soilPhosphorusLabel, soilPhosphorusData, soilPhosphorusHistory, Color(0xFFC2185B), cardBackground, textColor, secondaryTextColor, translatedText)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(translatedText.soilPotassiumLabel, soilPotassiumData, soilPotassiumHistory, Color(0xFF7B1FA2), cardBackground, textColor, secondaryTextColor, translatedText)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(translatedText.soilEcLabel, soilEcData, soilEcHistory, Color(0xFFF57C00), cardBackground, textColor, secondaryTextColor, translatedText)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SensorGraphCard(translatedText.soilPhLabel, soilPhData, soilPhHistory, Color(0xFFD32F2F), cardBackground, textColor, secondaryTextColor, translatedText)
                                        }
                                    }
                                }
                            }
                        }
                        if (!isReceivingData.value) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        translatedText.waitingForSensorData,
                                        modifier = Modifier.padding(vertical = 32.dp),
                                        color = textColor
                                    )
                                    Text(
                                        translatedText.ensureDeviceConnected,
                                        fontSize = 14.sp,
                                        color = secondaryTextColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (isSoilSensorClicked && selectedTabIndex == 1) {
                    SoilSensorDataTable(
                        soilMoistureHistory = soilMoistureHistory,
                        soilTemperatureHistory = soilTemperatureHistory,
                        soilNitrogenHistory = soilNitrogenHistory,
                        soilPhosphorusHistory = soilPhosphorusHistory,
                        soilPotassiumHistory = soilPotassiumHistory,
                        soilEcHistory = soilEcHistory,
                        soilPhHistory = soilPhHistory,
                        timestamps = soilDataTimestamps,
                        isReceivingData = isReceivingData.value && (soilMoistureHistory.isNotEmpty() || soilTemperatureHistory.isNotEmpty() ||
                                soilNitrogenHistory.isNotEmpty() || soilPhosphorusHistory.isNotEmpty() || soilPotassiumHistory.isNotEmpty() ||
                                soilEcHistory.isNotEmpty() || soilPhHistory.isNotEmpty()),
                        translatedText = translatedText,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        cardBackground = cardBackground
                    )
                }

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
                                    color = accentColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { isSoilSensorClicked = false }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                translatedText.backToAllSensors,
                                color = if (isDarkMode) Color.Black else Color.White,
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
fun SensorGraphCard(
    title: String,
    currentValue: Float?,
    history: List<Float>,
    color: Color,
    cardBackground: Color,
    textColor: Color,
    secondaryTextColor: Color,
    translatedText: TranslatedChartText
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = 4.dp,
        backgroundColor = cardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${translatedText.currentLabel}: ${currentValue?.toString() ?: translatedText.naLabel}",
                fontSize = 16.sp,
                color = secondaryTextColor
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

                        drawLine(
                            color = secondaryTextColor,
                            start = Offset(0f, size.height - heightPadding),
                            end = Offset(size.width, size.height - heightPadding),
                            strokeWidth = 1f
                        )

                        for (i in 0 until points.size - 1) {
                            val x1 = i * stepX
                            val x2 = (i + 1) * stepX
                            val y1 = heightPadding + (size.height - 2 * heightPadding) * (1 - (points[i] - minValue) / range)
                            val y2 = heightPadding + (size.height - 2 * heightPadding) * (1 - (points[i + 1] - minValue) / range)

                            drawLine(
                                color = color,
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 4f,
                                pathEffect = PathEffect.cornerPathEffect(10f)
                            )
                            drawCircle(
                                color = color,
                                radius = 6f,
                                center = Offset(x1, y1)
                            )
                        }
                        val lastX = (points.size - 1) * stepX
                        val lastY = heightPadding + (size.height - 2 * heightPadding) * (1 - (points.last() - minValue) / range)
                        drawCircle(
                            color = color,
                            radius = 6f,
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            } else {
                Text(
                    translatedText.waitingForData,
                    modifier = Modifier.padding(vertical = 32.dp),
                    color = textColor
                )
            }
        }
    }
}

// Helper function to update history lists
private fun updateHistory(history: MutableList<Float>, value: Float) {
    if (history.size >= 20) history.removeAt(0)
    history.add(value)
}

// Assuming SoilSensorDataTable exists, update it similarly
@Composable
fun SoilSensorDataTable(
    soilMoistureHistory: List<Float>,
    soilTemperatureHistory: List<Float>,
    soilNitrogenHistory: List<Float>,
    soilPhosphorusHistory: List<Float>,
    soilPotassiumHistory: List<Float>,
    soilEcHistory: List<Float>,
    soilPhHistory: List<Float>,
    timestamps: List<String>,
    isReceivingData: Boolean,
    translatedText: TranslatedChartText,
    textColor: Color,
    secondaryTextColor: Color,
    cardBackground: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = cardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isReceivingData) {
                LazyColumn {
                    items(timestamps.size) { index ->
                        Column {
                            Text(
                                "Timestamp: ${timestamps.getOrNull(index) ?: "-"}",
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${translatedText.soilMoistureLabel}: ${soilMoistureHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Text("${translatedText.soilTemperatureLabel}: ${soilTemperatureHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Text("${translatedText.soilNitrogenLabel}: ${soilNitrogenHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Text("${translatedText.soilPhosphorusLabel}: ${soilPhosphorusHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Text("${translatedText.soilPotassiumLabel}: ${soilPotassiumHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Text("${translatedText.soilEcLabel}: ${soilEcHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Text("${translatedText.soilPhLabel}: ${soilPhHistory.getOrNull(index) ?: "-"}", color = secondaryTextColor)
                            Divider(color = secondaryTextColor.copy(alpha = 0.2f), thickness = 1.dp)
                        }
                    }
                }
            } else {
                Text(
                    translatedText.waitingForSensorData,
                    modifier = Modifier.padding(vertical = 32.dp),
                    color = textColor
                )
            }
        }
    }
}