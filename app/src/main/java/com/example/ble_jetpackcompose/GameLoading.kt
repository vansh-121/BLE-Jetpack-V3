package com.example.ble_jetpackcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun BLEGamesScreen( navController: NavHostController) {
    val lineWidth = remember { Animatable(0f) } // Red bar width animation

    LaunchedEffect(Unit) {
        // Start the bar animation
        lineWidth.animateTo(
            targetValue = 198f,
            animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)

        )
        delay(500) // Optional small delay to ensure animation fully completes
        navController.navigate("game_screen")
    }

    // Calculate progress (value between 0 and 1) based on the bar's width animation
    val progress = lineWidth.value / 198f

    // Derived animations for text
    val alpha = progress // Opacity matches progress
    val offset = -45f + (progress * 50f) // Text moves upward based on progress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4D426D)) // Purple background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center), // Center content
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title text that moves upwards and becomes visible
            Text(
                text = "BLE Games",
                fontFamily = helveticaFont,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 39.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .alpha(alpha) // Gradually make text visible based on loading progress
                    .offset(y = offset.dp) // Move text upwards as loading progresses
                    .align(Alignment.CenterHorizontally) // Center horizontally
            )

            // Space between the text and the bar
            Spacer(modifier = Modifier.height(32.dp))

            // Gray bar (background rectangle)
            Box(
                modifier = Modifier
                    .width(198.dp) // Fixed width for the gray background bar
                    .height(5.dp) // Fixed height
                    .background(Color.Gray, shape = RoundedCornerShape(2.dp))
            ) {
                // Red bar (animated loading bar)
                Box(
                    modifier = Modifier
                        .width(lineWidth.value.dp) // Animate red bar's width
                        .fillMaxHeight() // Match the height of the gray bar
                        .background(Color.Red, shape = RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun BLEGameScreen() {
//    BLEGamesScreen()
//}