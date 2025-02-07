package com.example.ble_jetpackcompose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
@Composable
fun LoadingDialog() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Full-screen overlay with a semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        // Centered column with shadow and background
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(elevation = 30.dp, shape = MaterialTheme.shapes.medium)
                .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            // A box that holds the animated arcs and the Bluetooth icon together
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animated arcs drawn to fill the entire Box
                AnimatedWiFiArcs(
                    primaryColor = primaryColor,
                    modifier = Modifier.matchParentSize()
                )
                // Bluetooth icon placed on top of the arcs
                BluetoothIcon(primaryColor = primaryColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Wait... it's Sensing",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceColor
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
fun AnimatedWiFiArcs(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    // Set up an infinite transition for alpha and scale
    val infiniteTransition = rememberInfiniteTransition(label = "WiFi Arc Animation Transition")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WiFi Arc Alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WiFi Arc Scale"
    )

    // Use Canvas to draw the arcs behind the icon
    Canvas(modifier = modifier) {
        val baseRadius = size.minDimension / 4

        for (i in 1..3) {
            val scaledRadius = baseRadius * scale + (i * 20)
            drawArc(
                color = primaryColor.copy(alpha = (alpha - (0.15f * i)).coerceAtLeast(0f)),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx()),
                size = Size((scaledRadius * 1.6).toFloat(), (scaledRadius * 2.0).toFloat()),
                topLeft = Offset(
                    center.x - scaledRadius,
                    center.y - scaledRadius
                )
            )
        }
    }
}
