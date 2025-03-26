package com.example.ble_jetpackcompose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Data class for translatable text in SplashScreen
data class TranslatedSplashScreenText(
    val appName: String = "BLE Sense",
    val developedBy: String = "Developed by AWaDH, IIT Ropar"
)

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    LocalContext.current

    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedSplashScreenText(
                appName = TranslationCache.get("BLE Sense-$currentLanguage") ?: "BLE Sense",
                developedBy = TranslationCache.get("Developed by AWaDH, IIT Ropar-$currentLanguage") ?: "Developed by AWaDH, IIT Ropar"
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf("BLE Sense", "Developed by AWaDH, IIT Ropar")
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedSplashScreenText(
            appName = translatedList[0],
            developedBy = translatedList[1]
        )
    }

    // Theme-based colors
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f)

    // Trigger navigation after 2-second delay
    LaunchedEffect(key1 = true) {
        delay(2000L) // 2 seconds delay
        onNavigateToLogin()
    }

    val infiniteTransition = rememberInfiniteTransition()

    // Slow zoom animation for the image
    val imageScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Slow zoom animation for "BLE Sense" text
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Slow zoom animation for "Developed by AWaDH, IIT Ropar" text
    val footerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Background image with slow zoom effect
            Image(
                painter = painterResource(id = R.drawable.bg_remove_2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer(scaleX = imageScale, scaleY = imageScale),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(40.dp))

            // "BLE Sense" text with zoom animation
            BasicText(
                text = translatedText.appName,
                style = TextStyle(
                    fontSize = 40.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = helveticaFont
                ),
                modifier = Modifier
                    .graphicsLayer(scaleX = titleScale, scaleY = titleScale)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "Developed by AWaDH, IIT Ropar" text with zoom animation
            BasicText(
                text = translatedText.developedBy,
                style = TextStyle(
                    fontSize = 18.sp,
                    color = secondaryTextColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontFamily = helveticaFont
                ),
                modifier = Modifier
                    .graphicsLayer(scaleX = footerScale, scaleY = footerScale)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onNavigateToLogin = {})
}