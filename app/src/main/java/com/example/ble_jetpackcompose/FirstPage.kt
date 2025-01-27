package com.example.ble_jetpackcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedFirstScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    // Animations
    val backgroundScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    // Trigger animations
    LaunchedEffect(Unit) {
        backgroundScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )

        iconAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )

        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )

        buttonAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Animated Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = backgroundScale.value,
                    scaleY = backgroundScale.value,
                    transformOrigin = TransformOrigin(0f, 1f)
                )
                .clip(GenericShape { size, _ ->
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.9f)
                        quadraticBezierTo(
                            size.width * 0.1f, size.height * 0.62f,
                            size.width * 0.55f, size.height * 0.55f
                        )
                        quadraticBezierTo(
                            size.width * 1f, size.height * 0.47f,
                            size.width, size.height * 0.4f
                        )
                        lineTo(size.width, 0f)
                        lineTo(0f, 0f)
                        close()
                    }
                    addPath(path)
                })
                .background(Color(0xFFD9EFFF))
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_remove_ble),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(iconAlpha.value)
            )

            Text(
                text = "BLE Sense",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = helveticaFont,
                color = Color.Black,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(bottom = 140.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Login Button
            Button(
                onClick = { onNavigateToLogin() },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .alpha(buttonAlpha.value)
                    .padding(vertical = 8.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.btnColor)),
                elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sign Up Button
            Button(
                onClick = { onNavigateToSignup() },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .alpha(buttonAlpha.value)
                    .padding(vertical = 8.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.btnColor)),
                elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}