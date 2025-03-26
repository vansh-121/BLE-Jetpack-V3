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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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

// Data class for translatable text in AnimatedFirstScreen
data class TranslatedFirstScreenText(
    val appName: String = "BLE Sense",
    val login: String = "Login",
    val signUp: String = "Sign Up",
    val or: String = "OR",
    val continueAsGuest: String = "Continue as Guest"
)

@Composable
fun AnimatedFirstScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onGuestSignIn: () -> Unit
) {
    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedFirstScreenText(
                appName = TranslationCache.get("BLE Sense-$currentLanguage") ?: "BLE Sense",
                login = TranslationCache.get("Login-$currentLanguage") ?: "Login",
                signUp = TranslationCache.get("Sign Up-$currentLanguage") ?: "Sign Up",
                or = TranslationCache.get("OR-$currentLanguage") ?: "OR",
                continueAsGuest = TranslationCache.get("Continue as Guest-$currentLanguage") ?: "Continue as Guest"
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf("BLE Sense", "Login", "Sign Up", "OR", "Continue as Guest")
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedFirstScreenText(
            appName = translatedList[0],
            login = translatedList[1],
            signUp = translatedList[2],
            or = translatedList[3],
            continueAsGuest = translatedList[4]
        )
    }

    // Theme-based colors
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val shapeBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFD9EFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val dividerColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
    val buttonBackgroundColor = if (isDarkMode) Color(0xFFBB86FC) else colorResource(R.color.btnColor)
    val buttonTextColor = if (isDarkMode) Color.Black else Color.White
    val loadingIndicatorColor = if (isDarkMode) Color(0xFFBB86FC) else colorResource(R.color.btnColor)

    // Animations
    val backgroundScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    // Trigger animations
    LaunchedEffect(Unit) {
        backgroundScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        iconAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        buttonAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
    }

    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Animated Background Shape
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
                .background(shapeBackgroundColor)
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
                text = translatedText.appName,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = helveticaFont,
                color = textColor,
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
                colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackgroundColor),
                elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = translatedText.login,
                    fontSize = 18.sp,
                    color = buttonTextColor,
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
                colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackgroundColor),
                elevation = ButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = translatedText.signUp,
                    fontSize = 18.sp,
                    color = buttonTextColor,
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
                    color = dividerColor,
                    thickness = 1.dp
                )
                Text(
                    text = "  ${translatedText.or}  ",
                    color = dividerColor,
                    fontSize = 14.sp,
                    fontFamily = helveticaFont,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.5f),
                    color = dividerColor,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                LoadingAnimation(
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(buttonAlpha.value),
                    color = loadingIndicatorColor
                )
            } else {
                Text(
                    text = translatedText.continueAsGuest,
                    fontSize = 16.sp,
                    color = textColor,
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
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
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
            color = color,
            strokeWidth = 3.dp
        )
    }
}