package com.example.ble_jetpackcompose

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

// Data class for translatable text in AdvertisingDataScreen
// Update TranslatedAdvertisingText data class to include new fields
data class TranslatedAdvertisingText(
    val advertisingDataTitle: String = "Advertising Data",
    val deviceNameLabel: String = "Device Name",
    val nodeIdLabel: String = "Node ID",
    val downloadData: String = "DOWNLOAD DATA",
    val exportingData: String = "EXPORTING DATA...",
    val temperature: String = "Temperature",
    val humidity: String = "Humidity",
    val xAxis: String = "X-Axis",
    val yAxis: String = "Y-Axis",
    val zAxis: String = "Z-Axis",
    val nitrogen: String = "Nitrogen",
    val phosphorus: String = "Phosphorus",
    val potassium: String = "Potassium",
    val moisture: String = "Moisture",
    val electricConductivity: String = "Electric Conductivity",
    val pH: String = "pH",
    val lightIntensity: String = "Light Intensity",
    val speed: String = "Speed",
    val distance: String = "Distance",
    val objectDetected: String = "Object Detected",
    val steps: String = "Steps",
    val resetSteps: String = "RESET STEPS",
    val warningTitle: String = "Warning",
    val warningMessage: String = "The %s has exceeded the threshold of %s!",
    val dismissButton: String = "Dismiss"
)@Composable
fun AdvertisingDataScreen(
    deviceAddress: String,
    deviceName: String,
    navController: NavController,
    deviceId: String,
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf(MediaPlayer.create(context, R.raw.beep)) }
    val viewModel: BluetoothScanViewModel<Any?> = viewModel(factory = BluetoothScanViewModelFactory(context))
    val activity = context as Activity

    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Collect device list and current device
    val devices by viewModel.devices.collectAsState()
    val currentDevice by remember(devices, deviceAddress) {
        derivedStateOf { devices.find { it.address == deviceAddress } }
    }

    // Threshold and alarm state
    var thresholdValue by remember { mutableStateOf("") }
    var isAlarmActive by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }
    var parameterType by remember { mutableStateOf("Temperature") }
    var isThresholdSet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Blinking animation state
    val isBlinking by remember(isAlarmActive) { derivedStateOf { isAlarmActive } }
    val blinkAlpha by animateFloatAsState(
        targetValue = if (isBlinking) 0.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    // Threshold check for SHT40Data only
    LaunchedEffect(currentDevice, thresholdValue, parameterType, isThresholdSet) {
        delay(500L) // Debounce
        if (isThresholdSet && currentDevice?.sensorData is BluetoothScanViewModel.SensorData.SHT40Data) {
            val sht40Data = currentDevice!!.sensorData as BluetoothScanViewModel.SensorData.SHT40Data
            val threshold = thresholdValue.toFloatOrNull()
            if (threshold != null) {
                val valueToCheck = when (parameterType) {
                    "Temperature" -> sht40Data.temperature.toFloatOrNull()
                    "Humidity" -> sht40Data.humidity.toFloatOrNull()
                    else -> {
                        isAlarmActive = false
                        return@LaunchedEffect
                    }
                }
                if (valueToCheck != null) {
                    isAlarmActive = valueToCheck > threshold
                    if (isAlarmActive) {
                        showAlertDialog = true
                        if (!mediaPlayer.isPlaying) {
                            try {
                                mediaPlayer.isLooping = true
                                mediaPlayer.start()
                            } catch (e: IllegalStateException) {
                                mediaPlayer.reset()
                                MediaPlayer.create(context, R.raw.beep)?.let {
                                    mediaPlayer.release()
                                    mediaPlayer = it
                                    mediaPlayer.isLooping = true
                                    mediaPlayer.start()
                                }
                            }
                        }
                    } else {
                        try {
                            mediaPlayer.stop()
                            mediaPlayer.prepare()
                        } catch (e: IllegalStateException) {
                            mediaPlayer.reset()
                            MediaPlayer.create(context, R.raw.beep)?.let {
                                mediaPlayer.release()
                                mediaPlayer = it
                            }
                        }
                        showAlertDialog = false
                    }
                } else {
                    isAlarmActive = false
                    showAlertDialog = false
                    try {
                        mediaPlayer.stop()
                        mediaPlayer.prepare()
                    } catch (e: IllegalStateException) {
                        mediaPlayer.reset()
                        MediaPlayer.create(context, R.raw.beep)?.let {
                            mediaPlayer.release()
                            mediaPlayer = it
                        }
                    }
                }
            } else {
                isAlarmActive = false
                showAlertDialog = false
                try {
                    mediaPlayer.stop()
                    mediaPlayer.prepare()
                } catch (e: IllegalStateException) {
                    mediaPlayer.reset()
                    MediaPlayer.create(context, R.raw.beep)?.let {
                        mediaPlayer.release()
                        mediaPlayer = it
                    }
                }
            }
        } else {
            isAlarmActive = false
            showAlertDialog = false
            try {
                mediaPlayer.stop()
                mediaPlayer.prepare()
            } catch (e: IllegalStateException) {
                mediaPlayer.reset()
                MediaPlayer.create(context, R.raw.beep)?.let {
                    mediaPlayer.release()
                    mediaPlayer = it
                }
            }
        }
    }

    // Clean up MediaPlayer on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedAdvertisingText(
                advertisingDataTitle = TranslationCache.get("Advertising Data-$currentLanguage") ?: "Advertising Data",
                deviceNameLabel = TranslationCache.get("Device Name-$currentLanguage") ?: "Device Name",
                nodeIdLabel = TranslationCache.get("Node ID-$currentLanguage") ?: "Node ID",
                downloadData = TranslationCache.get("DOWNLOAD DATA-$currentLanguage") ?: "DOWNLOAD DATA",
                exportingData = TranslationCache.get("EXPORTING DATA...-$currentLanguage") ?: "EXPORTING DATA...",
                temperature = TranslationCache.get("Temperature-$currentLanguage") ?: "Temperature",
                humidity = TranslationCache.get("Humidity-$currentLanguage") ?: "Humidity",
                xAxis = TranslationCache.get("X-Axis-$currentLanguage") ?: "X-Axis",
                yAxis = TranslationCache.get("Y-Axis-$currentLanguage") ?: "Y-Axis",
                zAxis = TranslationCache.get("Z-Axis-$currentLanguage") ?: "Z-Axis",
                nitrogen = TranslationCache.get("Nitrogen-$currentLanguage") ?: "Nitrogen",
                phosphorus = TranslationCache.get("Phosphorus-$currentLanguage") ?: "Phosphorus",
                potassium = TranslationCache.get("Potassium-$currentLanguage") ?: "Potassium",
                moisture = TranslationCache.get("Moisture-$currentLanguage") ?: "Moisture",
                electricConductivity = TranslationCache.get("Electric Conductivity-$currentLanguage") ?: "Electric Conductivity",
                pH = TranslationCache.get("pH-$currentLanguage") ?: "pH",
                lightIntensity = TranslationCache.get("Light Intensity-$currentLanguage") ?: "Light Intensity",
                speed = TranslationCache.get("Speed-$currentLanguage") ?: "Speed",
                distance = TranslationCache.get("Distance-$currentLanguage") ?: "Distance",
                objectDetected = TranslationCache.get("Object Detected-$currentLanguage") ?: "Object Detected",
                steps = TranslationCache.get("Steps-$currentLanguage") ?: "Steps",
                resetSteps = TranslationCache.get("RESET STEPS-$currentLanguage") ?: "RESET STEPS",
                warningTitle = TranslationCache.get("Warning-$currentLanguage") ?: "Warning",
                warningMessage = TranslationCache.get("Threshold Exceeded-$currentLanguage") ?: "The %s has exceeded the threshold of %s!",
                dismissButton = TranslationCache.get("Dismiss-$currentLanguage") ?: "Dismiss"
            )
        )
    }

    // Preload translations
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf(
            "Advertising Data", "Device Name", "Node ID", "DOWNLOAD DATA", "EXPORTING DATA...",
            "Temperature", "Humidity", "X-Axis", "Y-Axis", "Z-Axis",
            "Nitrogen", "Phosphorus", "Potassium", "Moisture", "Electric Conductivity",
            "pH", "Light Intensity", "Speed", "Distance", "Object Detected", "Steps", "RESET STEPS",
            "Warning", "Threshold Exceeded", "Dismiss"
        )
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedAdvertisingText(
            advertisingDataTitle = translatedList[0],
            deviceNameLabel = translatedList[1],
            nodeIdLabel = translatedList[2],
            downloadData = translatedList[3],
            exportingData = translatedList[4],
            temperature = translatedList[5],
            humidity = translatedList[6],
            xAxis = translatedList[7],
            yAxis = translatedList[8],
            zAxis = translatedList[9],
            nitrogen = translatedList[10],
            phosphorus = translatedList[11],
            potassium = translatedList[12],
            moisture = translatedList[13],
            electricConductivity = translatedList[14],
            pH = translatedList[15],
            lightIntensity = translatedList[16],
            speed = translatedList[17],
            distance = translatedList[18],
            objectDetected = translatedList[19],
            steps = translatedList[20],
            resetSteps = translatedList[21],
            warningTitle = translatedList[22],
            warningMessage = translatedList[23],
            dismissButton = translatedList[24]
        )
    }

    // Restored display data for all sensor types
    val displayData by remember(currentDevice?.sensorData, translatedText) {
        derivedStateOf {
            when (val sensorData = currentDevice?.sensorData) {
                is BluetoothScanViewModel.SensorData.SHT40Data -> listOf(
                    translatedText.temperature to "${sensorData.temperature.takeIf { it.isNotEmpty() } ?: "0"}°C",
                    translatedText.humidity to "${sensorData.humidity.takeIf { it.isNotEmpty() } ?: "0"}%"
                )
                is BluetoothScanViewModel.SensorData.LuxData -> listOf(
                    translatedText.lightIntensity to "${sensorData.calculatedLux} LUX"
                )
                is BluetoothScanViewModel.SensorData.LIS2DHData -> listOf(
                    translatedText.xAxis to "${sensorData.x.takeIf { it.isNotEmpty() } ?: "0"} m/s²",
                    translatedText.yAxis to "${sensorData.y.takeIf { it.isNotEmpty() } ?: "0"} m/s²",
                    translatedText.zAxis to "${sensorData.z.takeIf { it.isNotEmpty() } ?: "0"} m/s²"
                )
                is BluetoothScanViewModel.SensorData.SoilSensorData -> listOf(
                    translatedText.nitrogen to "${sensorData.nitrogen.takeIf { it.isNotEmpty() } ?: "0"} mg/kg",
                    translatedText.phosphorus to "${sensorData.phosphorus.takeIf { it.isNotEmpty() } ?: "0"} mg/kg",
                    translatedText.potassium to "${sensorData.potassium.takeIf { it.isNotEmpty() } ?: "0"} mg/kg",
                    translatedText.moisture to "${sensorData.moisture.takeIf { it.isNotEmpty() } ?: "0"}%",
                    translatedText.temperature to "${sensorData.temperature.takeIf { it.isNotEmpty() } ?: "0"}°C",
                    translatedText.electricConductivity to "${sensorData.ec.takeIf { it.isNotEmpty() } ?: "0"} mS/cm",
                    translatedText.pH to "${sensorData.pH.takeIf { it.isNotEmpty() } ?: "0"}"
                )
                is BluetoothScanViewModel.SensorData.SDTData -> listOf(
                    translatedText.speed to "${sensorData.speed.takeIf { it.isNotEmpty() } ?: "0"} m/s",
                    translatedText.distance to "${sensorData.distance.takeIf { it.isNotEmpty() } ?: "0"} m"
                )
                is BluetoothScanViewModel.SensorData.ObjectDetectorData -> listOf(
                    translatedText.objectDetected to if (sensorData.detection) "Yes" else "No"
                )
                is BluetoothScanViewModel.SensorData.StepCounterData -> listOf(
                    translatedText.steps to "${sensorData.steps.takeIf { it.isNotEmpty() } ?: "0"}"
                )
                else -> emptyList()
            }
        }
    }

    // Theme-based colors
    val backgroundGradient = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF424242)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0A74DA), Color(0xFFADD8E6)))
    }
    val cardBackground = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFF2A9EE5)
    val cardGradient = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF424242), Color(0xFF212121)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF2A9EE5), Color(0xFF076FB8)))
    }
    val textColor = if (isDarkMode) Color.White else Color.White
    val buttonColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF0A74DA)

    // Start scanning
    LaunchedEffect(Unit) {
        viewModel.startScan(activity)
    }

    // Clean up scanning
    DisposableEffect(navController) {
        onDispose {
            viewModel.stopScan()
            viewModel.clearDevices()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Blinking red overlay
        if (isAlarmActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = blinkAlpha))
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection(
                navController = navController,
                viewModel = viewModel,
                deviceAddress = deviceAddress,
                translatedText = translatedText,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeviceInfoSection(
                deviceName = deviceName,
                deviceAddress = deviceAddress,
                deviceId = deviceId,
                translatedText = translatedText,
                cardBackground = cardBackground,
                cardGradient = cardGradient,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            ResponsiveDataCards(
                data = displayData,
                cardBackground = cardBackground,
                cardGradient = cardGradient,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Threshold input section for SHT40Data only
            if (currentDevice?.sensorData is BluetoothScanViewModel.SensorData.SHT40Data) {
                ThresholdInputSection(
                    thresholdValue = thresholdValue,
                    onThresholdChange = { thresholdValue = it },
                    parameterType = parameterType,
                    onParameterChange = { parameterType = it },
                    isDarkMode = isDarkMode,
                    onConfirmThreshold = {
                        if (thresholdValue.toFloatOrNull() != null) {
                            isThresholdSet = true
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reset Steps button for StepCounterData
            if (currentDevice?.sensorData is BluetoothScanViewModel.SensorData.StepCounterData) {
                ResetStepsButton(
                    viewModel = viewModel,
                    deviceAddress = deviceAddress,
                    translatedText = translatedText
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DownloadButton(
                viewModel = viewModel,
                deviceAddress = deviceAddress,
                deviceName = deviceName,
                deviceId = deviceId,
                translatedText = translatedText
            )

            // Alert Dialog for SHT40Data threshold
            if (showAlertDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showAlertDialog = false
                        isAlarmActive = false
                        isThresholdSet = false
                        try {
                            mediaPlayer.stop()
                            mediaPlayer.prepare()
                        } catch (e: IllegalStateException) {
                            mediaPlayer.reset()
                            MediaPlayer.create(context, R.raw.beep)?.let {
                                mediaPlayer.release()
                                mediaPlayer = it
                            }
                        }
                    },
                    title = { Text(translatedText.warningTitle) },
                    text = {
                        Text(
                            text = translatedText.warningMessage.format(
                                parameterType,
                                thresholdValue
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAlertDialog = false
                                isAlarmActive = false
                                isThresholdSet = false
                                try {
                                    mediaPlayer.stop()
                                    mediaPlayer.prepare()
                                } catch (e: IllegalStateException) {
                                    mediaPlayer.reset()
                                    MediaPlayer.create(context, R.raw.beep)?.let {
                                        mediaPlayer.release()
                                        mediaPlayer = it
                                    }
                                }
                            }
                        ) {
                            Text(translatedText.dismissButton)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThresholdInputSection(
    thresholdValue: String,
    onThresholdChange: (String) -> Unit,
    parameterType: String,
    onParameterChange: (String) -> Unit,
    isDarkMode: Boolean,
    onConfirmThreshold: () -> Unit // New callback for confirming threshold
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Parameter type toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Temperature", "Humidity").forEach { type ->
                Button(
                    onClick = { onParameterChange(type) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (parameterType == type) {
                            if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF0A74DA)
                        } else {
                            if (isDarkMode) Color(0xFF424242) else Color(0xFFADD8E6)
                        }
                    )
                ) {
                    Text(type)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Threshold input field
        TextField(
            value = thresholdValue,
            onValueChange = { onThresholdChange(it) },
            label = { Text("Enter $parameterType Threshold") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = thresholdValue.isNotEmpty() && thresholdValue.toFloatOrNull() == null,
            supportingText = {
                if (thresholdValue.isNotEmpty() && thresholdValue.toFloatOrNull() == null) {
                    Text("Please enter a valid number")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
                unfocusedContainerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
                focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                unfocusedTextColor = if (isDarkMode) Color.White else Color.Black,
                errorContainerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Confirm button
        Button(
            onClick = onConfirmThreshold,
            enabled = thresholdValue.isNotEmpty() && thresholdValue.toFloatOrNull() != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF0A74DA)
            )
        ) {
            Text("Confirm Threshold")
        }
    }
}



@Composable
private fun ResetStepsButton(
    viewModel: BluetoothScanViewModel<Any?>,
    deviceAddress: String,
    translatedText: TranslatedAdvertisingText
) {
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    // Define button colors based on theme
    val buttonBackgroundColor = if (isDarkMode) Color(0xFFFF5252) else Color(0xFFE53935)  // Red color for reset
    val buttonTextColor = Color.White

    Button(
        onClick = {
            viewModel.resetStepCounter(deviceAddress)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonBackgroundColor)
    ) {
        Text(
            text = translatedText.resetSteps,
            color = buttonTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HeaderSection(
    navController: NavController,
    viewModel: BluetoothScanViewModel<Any?>,
    deviceAddress: String,
    translatedText: TranslatedAdvertisingText,
    textColor: Color
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
                tint = textColor
            )
        }

        Text(
            text = translatedText.advertisingDataTitle,
            fontFamily = helveticaFont,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
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
    deviceId: String,
    translatedText: TranslatedAdvertisingText,
    cardBackground: Color,
    cardGradient: Brush,
    textColor: Color
) {
    InfoCard(
        text = "${translatedText.deviceNameLabel}: $deviceName ($deviceAddress)",
        cardBackground = cardBackground,
        cardGradient = cardGradient,
        textColor = textColor
    )
    Spacer(modifier = Modifier.height(8.dp))
    InfoCard(
        text = "${translatedText.nodeIdLabel}: $deviceId",
        cardBackground = cardBackground,
        cardGradient = cardGradient,
        textColor = textColor
    )
}

@Composable
private fun InfoCard(
    text: String,
    cardBackground: Color,
    cardGradient: Brush,
    textColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBackground
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(16.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}@Composable
private fun DownloadButton(
    viewModel: BluetoothScanViewModel<Any?>,
    deviceAddress: String,
    deviceName: String,
    deviceId: String,
    translatedText: TranslatedAdvertisingText,
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            isExporting = true
            exportDataToCSV(context, uri, viewModel, deviceAddress, deviceName, deviceId) {
                isExporting = false
            }
        }
    }

    // Define button colors based on theme
    val buttonBackgroundColor = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF0A74DA)
    val buttonTextColor = if (isDarkMode) Color.Black else Color.White

    Button(
        onClick = {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
            val filename = "sensor_data_${deviceId}_$timestamp.csv"
            createDocumentLauncher.launch(filename)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = !isExporting,
        colors = ButtonDefaults.buttonColors(containerColor = buttonBackgroundColor)
    ) {
        Text(
            text = if (isExporting) translatedText.exportingData else translatedText.downloadData,
            color = buttonTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun exportDataToCSV(
    context: Context,
    uri: Uri,
    viewModel: BluetoothScanViewModel<Any?>,
    deviceAddress: String,
    deviceName: String,
    deviceId: String,
    onComplete: () -> Unit
) {
    // Same as original, no translation needed here as it’s CSV data
    kotlinx.coroutines.MainScope().launch {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val historicalData = viewModel.getHistoricalDataForDevice(deviceAddress)
                    if (historicalData.isEmpty()) {
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

                    val headerBuilder = StringBuilder()
                    headerBuilder.append("Timestamp,Device Name,Device Address,Node ID,")
                    val sensorType = if (historicalData.isNotEmpty()) historicalData[0].sensorData else null
                    when (sensorType) {
                        is BluetoothScanViewModel.SensorData.SHT40Data -> headerBuilder.append("Temperature (°C),Humidity (%)")
                        is BluetoothScanViewModel.SensorData.LIS2DHData -> headerBuilder.append("X-Axis (m/s²),Y-Axis (m/s²),Z-Axis (m/s²)")
                        is BluetoothScanViewModel.SensorData.SoilSensorData -> headerBuilder.append("Nitrogen (mg/kg),Phosphorus (mg/kg),Potassium (mg/kg),Moisture (%),Temperature (°C),Electric Conductivity (mS/cm),pH")
                        is BluetoothScanViewModel.SensorData.LuxData -> headerBuilder.append("Light Intensity (LUX)")
                        is BluetoothScanViewModel.SensorData.SDTData -> headerBuilder.append("Speed (m/s),Distance (m)")
                        is BluetoothScanViewModel.SensorData.ObjectDetectorData -> headerBuilder.append("Object Detected")
                        else -> {}
                    }
                    headerBuilder.append("\n")
                    outputStream.write(headerBuilder.toString().toByteArray())

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
                    historicalData.forEach { entry ->
                        val dataBuilder = StringBuilder()
                        dataBuilder.append("${dateFormat.format(Date(entry.timestamp))},$deviceName,$deviceAddress,$deviceId,")
                        when (val sensorData = entry.sensorData) {
                            is BluetoothScanViewModel.SensorData.SHT40Data -> dataBuilder.append("${sensorData.temperature},${sensorData.humidity}")
                            is BluetoothScanViewModel.SensorData.LIS2DHData -> dataBuilder.append("${sensorData.x},${sensorData.y},${sensorData.z}")
                            is BluetoothScanViewModel.SensorData.SoilSensorData -> dataBuilder.append("${sensorData.nitrogen},${sensorData.phosphorus},${sensorData.potassium},${sensorData.moisture},${sensorData.temperature},${sensorData.ec},${sensorData.pH}")
                            is BluetoothScanViewModel.SensorData.LuxData -> dataBuilder.append("${sensorData.calculatedLux}")
                            is BluetoothScanViewModel.SensorData.SDTData -> dataBuilder.append("${sensorData.speed},${sensorData.distance}")
                            is BluetoothScanViewModel.SensorData.ObjectDetectorData -> dataBuilder.append("${sensorData.detection}")
                            is BluetoothScanViewModel.SensorData.StepCounterData -> dataBuilder.append("${sensorData.steps}")
                            null -> {}
                        }
                        dataBuilder.append("\n")
                        outputStream.write(dataBuilder.toString().toByteArray())
                        if (historicalData.indexOf(entry) % 100 == 0) outputStream.flush()
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
private fun LuxAnimationSection(
    luxData: BluetoothScanViewModel.SensorData.LuxData,
    isDarkMode: Boolean
) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        SunWithRayAnimation(
            lux = luxData.calculatedLux,
            isDarkMode = isDarkMode
        )
    }
}

@Composable
fun SunWithRayAnimation(
    lux: Float,
    isDarkMode: Boolean,
    rayThickness: Dp = 2.dp,
    rayCount: Int = 12,
    maxLux: Float = 255f
) {
    val luxThresholds = listOf(200f, 700f, 1500f)
    val rayStage = when {
        lux >= luxThresholds[2] -> 3
        lux >= luxThresholds[1] -> 2
        lux >= luxThresholds[0] -> 1
        else -> 0
    }
    val rayLengths = listOf(10.dp, 100.dp, 150.dp, 200.dp)
    val currentRayLength = rayLengths[rayStage]
    val normalizedLux = lux.coerceIn(0f, maxLux) / maxLux
    val rayOpacity = normalizedLux.coerceIn(0.1f, 1f)
    val rayColor = if (isDarkMode) {
        Color(
            red = 1f,
            green = (0.7f + 0.3f * normalizedLux).coerceIn(0.7f, 1f),
            blue = (0.5f * normalizedLux).coerceIn(0f, 0.5f)
        )
    } else {
        Color(
            red = 1f,
            green = (0.7f + 0.3f * normalizedLux).coerceIn(0.7f, 1f),
            blue = (0.2f * normalizedLux).coerceIn(0f, 0.2f)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
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
        Image(
            painter = painterResource(id = R.drawable.sun),
            contentDescription = "Sun",
            modifier = Modifier.size(100.dp)
        )
    }
}
@Composable
private fun ResponsiveDataCards(
    data: List<Pair<String, String>>,
    cardBackground: Color,
    cardGradient: Brush,
    textColor: Color
) {
    when (data.size) {
        0 -> {
            // Handle empty data case
            Box(modifier = Modifier.height(100.dp))
        }
        1 -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DataCard(
                    label = data[0].first,
                    value = data[0].second,
                    cardBackground = cardBackground,
                    cardGradient = cardGradient,
                    textColor = textColor
                )
            }
        }
        2 -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { (label, value) ->
                    DataCard(
                        label = label,
                        value = value,
                        cardBackground = cardBackground,
                        cardGradient = cardGradient,
                        textColor = textColor
                    )
                }
            }
        }
        else -> {
            // For 3+ items, use a more compact layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { (label, value) ->
                            DataCard(
                                label = label,
                                value = value,
                                cardBackground = cardBackground,
                                cardGradient = cardGradient,
                                textColor = textColor
                            )
                        }
                        // If we have odd number of items, add a spacer for balance
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.width(141.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DataCard(
    label: String,
    value: String,
    cardBackground: Color,
    cardGradient: Brush,
    textColor: Color
) {
    Surface(
        modifier = Modifier
            .height(95.dp)
            .width(141.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBackground,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}