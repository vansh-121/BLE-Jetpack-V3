package com.example.ble_jetpackcompose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Data class for translatable text in LoadingDialog
data class TranslatedLoadingDialogText(
    val waitMessage: String = "Wait.It's Sensing..."
)

@Composable
fun LoadingDialog(onDismissRequest: () -> Unit) {
    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedLoadingDialogText(
                waitMessage = TranslationCache.get("Wait.It's Sensing...-$currentLanguage") ?: "Wait.It's Sensing..."
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf("Wait.It's Sensing...")
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedLoadingDialogText(
            waitMessage = translatedList[0]
        )
    }

    // Theme-based colors
    val dialogBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val primaryColor = if (isDarkMode) Color(0xFFBB86FC) else MaterialTheme.colorScheme.primary // Use Material primary in light mode

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium)
                .background(
                    color = dialogBackgroundColor,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                OptimizedAnimatedArcs(
                    primaryColor = primaryColor,
                    modifier = Modifier.matchParentSize()
                )
                BluetoothIcon(primaryColor = primaryColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = translatedText.waitMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun BluetoothIcon(primaryColor: Color) {
    Icon(
        imageVector = Icons.Default.Bluetooth,
        contentDescription = "Bluetooth Icon",
        modifier = Modifier.size(60.dp),
        tint = primaryColor
    )
}

@Composable
fun OptimizedAnimatedArcs(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "Arc Animation")

    // Combined animation value to reduce number of animations
    val animationValue by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Combined Animation"
    )

    Canvas(modifier = modifier) {
        val baseRadius = size.minDimension / 4
        val strokeWidth = 4.dp.toPx()

        // Pre-calculate common values
        val scale = 1f + (animationValue * 0.3f)

        for (i in 0..2) {
            val alphaMultiplier = (3 - i) / 3f
            val alpha = (animationValue * alphaMultiplier).coerceIn(0f, 0.7f)
            val scaledRadius = baseRadius * scale + (i * 20)

            drawArc(
                color = primaryColor.copy(alpha = alpha),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = Size(scaledRadius * 1.6f, scaledRadius * 2.0f),
                topLeft = Offset(
                    center.x - (scaledRadius * 0.8f),
                    center.y - scaledRadius
                )
            )
        }
    }
}