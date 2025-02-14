package com.example.ble_jetpackcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedFirstScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onGuestSignIn: () -> Unit
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
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing) // Reduced from 1200 to 800
        )

        iconAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing) // Reduced from 800 to 500
        )

        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing) // Reduced from 800 to 500
        )

        buttonAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing) // Reduced from 800 to 500
        )
    }

    var isLoading by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(80.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .alpha(buttonAlpha.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.5f),
                    color = Color.Gray,
                    thickness = 1.dp
                )
                Text(
                    text = "  OR  ",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = helveticaFont,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.5f),
                    color = Color.Gray,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                LoadingAnimation(
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(buttonAlpha.value)
                )
            } else {
                Text(
                    text = "Continue as Guest",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .alpha(buttonAlpha.value)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isLoading = true
                            onGuestSignIn()
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing), // Reduced from 1000 to 800
            repeatMode = RepeatMode.Restart
        )
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing), // Reduced from 700 to 500
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotationAngle
                }
                .size(32.dp),
            color = colorResource(R.color.btnColor),
            strokeWidth = 3.dp
        )
    }
}