package com.example.ble_jetpackcompose
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext


fun dpToPx(dp: Dp): Float {
    return dp.value
}

@Composable
fun GameActivityScreen() {
    var expandedImage by remember { mutableStateOf<Int?>(null) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var showSearchImage by remember { mutableStateOf(true) }
    var showTTHButton by remember { mutableStateOf(false) }
    var showScratchCard by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    // State for pop-up visibility
    var showPopup by remember { mutableStateOf(false) }

    // Infinite circular movement animation variables
    val infiniteTransition = rememberInfiniteTransition(label = "searchButtonAnimation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "searchButtonRotation"
    )

    val radius = 100f
    val searchX = remember { derivedStateOf { radius * cos(Math.toRadians(angle.toDouble())).toFloat() } }
    val searchY = remember { derivedStateOf { radius * sin(Math.toRadians(angle.toDouble())).toFloat() } }
    var isSoundOn by remember { mutableStateOf(true) } // Initially sound is on
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.bgmusic).apply {
            isLooping = true // Ensure the music loops if needed
        }
    }
    LaunchedEffect(isSoundOn) {
        if (isSoundOn) {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start() // Resume playback if previously paused
            }
        } else {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause() // Pause the music
            }
        }
    }

    // Release mediaPlayer when the composable is removed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenWidth = coordinates.size.width.toFloat()
                screenHeight = coordinates.size.height.toFloat()
            }
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.1.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Title Image
            Image(
                painter = painterResource(id = R.drawable.ble_games),
                contentDescription = "BLE Games Title",
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                // First Row - Hunt the Heroes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .offset(y = (-60).dp)
                        .zIndex(if (expandedImage == R.drawable.hunt_the_heroes) 2f else 1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    var buttonPosition by remember { mutableStateOf(IntOffset.Zero) }

                    AnimatedImageButton(
                        imageResId = R.drawable.hunt_the_heroes,
                        contentDescription = "Hunt the Heroes",
                        isExpanded = expandedImage == R.drawable.hunt_the_heroes,
                        expandedImageResId = R.drawable.hth,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        currentPosition = buttonPosition,
                        onPositioned = { buttonPosition = it },
                        onClick = {
                            expandedImage = if (expandedImage == R.drawable.hunt_the_heroes) null
                            else R.drawable.hunt_the_heroes
                            showPopup = !showPopup
                            showSearchImage = true // Reset search button visibility when reopened
                            showTTHButton = false // Reset TTR button visibility when reopened
                            showScratchCard = false // Hide scratch card initially
                        }
                    )
                }

                // Search button overlay
                if (expandedImage == R.drawable.hunt_the_heroes) {
                    if (showSearchImage) {
                        LaunchedEffect(Unit) {
                            delay(2000) // Wait for 2 seconds
                            showSearchImage = false
                            showTTHButton = true


                        }
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (searchX.value + screenWidth / 2 - 50.dp.toPx()).toInt(),
                                        (searchY.value + screenHeight / 2 - 180.dp.toPx()).toInt()
                                    )
                                }
                                .zIndex(2f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search Button",
                                modifier = Modifier.size(50.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    if (showTTHButton) {
                        Box(
                            modifier = Modifier
                                .align(Center)
                                .zIndex(6f)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null // Disables ripple effect
                                ) {
                                    showScratchCard = true
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ttr),
                                contentDescription = "TTR Button",
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                            if (showScratchCard) {
                                ScratchCardScreen()
                            }
                        }
                    }

                    // Pop-up with images (Jack, Parrot, Pirate)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                            .zIndex(8f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Animate the images appearing from the bottom
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showPopup,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.jack),
                                    contentDescription = "Jack",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(8.dp)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.parrot),
                                    contentDescription = "Parrot",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(8.dp)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.pirate),
                                    contentDescription = "Pirate",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Second Row - Guess the Character
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, top = 250.dp)  // Added top padding to move it down
                        .zIndex(if (expandedImage == R.drawable.guess_the_character) 2f else 1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    var buttonPosition by remember { mutableStateOf(IntOffset.Zero) }

                    AnimatedImageButton(
                        imageResId = R.drawable.guess_the_character,
                        contentDescription = "Guess the Character",
                        isExpanded = expandedImage == R.drawable.guess_the_character,
                        expandedImageResId = R.drawable.gth,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        currentPosition = buttonPosition,
                        onPositioned = { buttonPosition = it },
                        onClick = {
                            expandedImage = if (expandedImage == R.drawable.guess_the_character) null
                            else R.drawable.guess_the_character
                        }
                    )
                }

                // Show Radar only when "Guess the Character" is expanded
                if (expandedImage == R.drawable.guess_the_character) {
                    var showRadar by remember { mutableStateOf(false) }

                    // Trigger the delay when expandedImage changes to guess_the_character
                    LaunchedEffect(expandedImage) {
                        if (expandedImage == R.drawable.guess_the_character) {
                            delay(100) // delay
                            showRadar = true
                        } else {
                            showRadar = false
                        }
                    }

                    if (showRadar) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 150.dp)
                                .zIndex(7f),
                            contentAlignment = Center,  // This ensures the radar is centered
                        ) {
                            RadarScreenWithRotatingLine()
                        }
                    }
                }
            }
        }

        // Home Button - Bottom Left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null ) {
                    // Handle Home button click action here
                    println("Home button clicked")
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.home),
                contentDescription = "Home Button",
                modifier = Modifier.size(40.dp)
            )
        }

        // Sound Button - Bottom Right


        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null // Disable ripple effect
                ) {
                    // Toggle the sound state (mute/unmute)
                    isSoundOn = !isSoundOn
                }
        ) {
            // Conditionally show sound on or off image based on the state
            Image(
                painter = painterResource(id = if (isSoundOn) R.drawable.soundon else R.drawable.soundoff),
                contentDescription = if (isSoundOn) "Sound On" else "Sound Off", // Update the content description for accessibility
                modifier = Modifier.size(40.dp)
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ScratchCardScreenPreview() {
    ScratchCardScreen()
}