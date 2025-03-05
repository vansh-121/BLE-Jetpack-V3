package com.example.ble_jetpackcompose

import android.app.Activity
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import kotlin.math.roundToInt

fun dpToPx(dp: Dp): Float {
    return dp.value
}

@Composable
fun GameActivityScreen(
    activity: Activity,
    bluetoothViewModel: BluetoothScanViewModel<Any> = viewModel(
        factory = BluetoothScanViewModelFactory (LocalContext.current)
    )
) {
    var expandedImage by remember { mutableStateOf<Int?>(null) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var isSoundOn by remember { mutableStateOf(true) }
    var showScratchCard by remember { mutableStateOf(false) }
    var showTTHButton by remember { mutableStateOf(false) }
    var showSearchImage by remember { mutableStateOf(true) } // Initially visible
    var showPopup by remember { mutableStateOf(false) }
    var scratchCompleted by remember { mutableStateOf(false) }

    // Bluetooth-related states
    val allowedHeroes = listOf(
        "Scarlet Witch", "Black Widow", "Captain Marvel", "Wasp", "Hela",
        "Hulk", "Thor", "Iron_Man", "Spider Man", "Captain America"
    )
    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
    var foundCharacters by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Extract LocalContext outside of remember
    val context = LocalContext.current

// Create and manage MediaPlayer using DisposableEffect
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.bgmusic).apply {
            isLooping = true // Ensure the music loops
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    LaunchedEffect(isSoundOn) {
        if (isSoundOn) {
            if (!mediaPlayer.isPlaying) mediaPlayer.start()
        } else {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        }
    }

    // Infinite circular animation for search button
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
    val searchX =
        remember { derivedStateOf { radius * cos(Math.toRadians(angle.toDouble())).toFloat() } }
    val searchY =
        remember { derivedStateOf { radius * sin(Math.toRadians(angle.toDouble())).toFloat() } }

    // Animation states
    val animateHero by remember { mutableStateOf<String?>(null) }
    val transitionProgress by animateFloatAsState(
        targetValue = if (animateHero != null) 2f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "heroTransition"
    )

    // State for Game Box visibility
    var isGameBoxOpen by remember { mutableStateOf(false) }

// Position of the Game Box Button (bottom-left corner)
    val gameBoxPosition = remember { Offset(40f, screenHeight - 100f) }

// Animation state for moving heroes into the Game Box
    val heroAnimationState = remember { mutableStateListOf<Pair<String, Offset>>() }


    // Particle explosion effect
    val particles = remember { mutableStateListOf<Triple<Float, Float, Double>>() }
    val centerX = screenWidth / 2
    val centerY = screenHeight / 2

    LaunchedEffect(scratchCompleted) {
        if (scratchCompleted && screenWidth > 0 && screenHeight > 0) {
            repeat(100) {
                val angle = Random.nextDouble(0.0, 360.0)
                val distance = Random.nextFloat() * 50
                val startX = (centerX + cos(Math.toRadians(angle)) * distance).toFloat()
                val startY = (centerY + sin(Math.toRadians(angle)) * distance).toFloat()
                particles.add(Triple(startX, startY, angle))
            }

            while (particles.isNotEmpty()) {
                particles.forEachIndexed { index, (x, y, angle) ->
                    val speedX = (cos(Math.toRadians(angle)) * Random.nextFloat() * 10).toFloat()
                    val speedY = (sin(Math.toRadians(angle)) * Random.nextFloat() * 10).toFloat()
                    val decay = Random.nextFloat() * 0.1f
                    particles[index] = Triple(
                        x + speedX,
                        y + speedY - decay,
                        angle
                    )
                }
                delay(16)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (x, y, _) ->
            val baseSize = Random.nextFloat() * 10 + 5
            val trailLength = 5
            for (i in 0 until trailLength) {
                val trailAlpha = 1f - (i / trailLength.toFloat())
                val trailSize = baseSize * (1f - (i / trailLength.toFloat()))
                drawCircle(
                    color = Color(
                        Random.nextInt(256),
                        Random.nextInt(256),
                        Random.nextInt(256),
                        (trailAlpha * 255).toInt()
                    ),
                    radius = trailSize,
                    center = Offset(
                        x - i * 2,
                        y - i * 2
                    )
                )
            }
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
            modifier = Modifier.fillMaxSize()
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

            // Stack "Hunt the Heroes" and "Guess the Character" buttons vertically
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between buttons
            ) {

                // Hunt the Heroes Section
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
                            showSearchImage = true
                            showTTHButton = false
                            showScratchCard = false
                        }
                    )
                }


                // Guess the Character Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 16.dp)
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
                            expandedImage =
                                if (expandedImage == R.drawable.guess_the_character) null
                                else R.drawable.guess_the_character
                            showPopup = !showPopup
                            showSearchImage = true
                            showTTHButton = false
                            showScratchCard = false
                        },
                    )
                }
            }
        }

        LaunchedEffect(expandedImage) {
            if (expandedImage == R.drawable.hunt_the_heroes) {
                // Add this line
                bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.HUNT_THE_HEROES)

                bluetoothViewModel.startScan(activity)
            } else {
                // Add this line when exiting the mode
                bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.NONE)
            }
        }

        // Hunt the Heroes Logic
        if (expandedImage == R.drawable.hunt_the_heroes) {
            val nearbyHeroDevice = bluetoothDevices.find {
                it.name in allowedHeroes && it.rssi.toInt() in -40..0 && it.name !in foundCharacters.keys
            }





            LaunchedEffect(nearbyHeroDevice) {
                if (nearbyHeroDevice != null) {
                    showTTHButton = true
                    showSearchImage = false
                } else {
                    showTTHButton = false
                    showSearchImage = true
                }
            }

            // Handle Scratch Card Logic
            LaunchedEffect(scratchCompleted) {
                if (scratchCompleted && nearbyHeroDevice != null) {
                    val heroName = nearbyHeroDevice.name

                    // Start the animation for the hero moving into the Game Box
                    heroAnimationState.add(Pair(heroName, Offset(centerX, centerY)))

                    // Simulate the hero moving to the Game Box
                    delay(500) // Wait for the particle explosion effect
                    heroAnimationState.clear() // Clear the animation after it's done

                    // Add the hero to the found characters list
                    val updatedCharacters = foundCharacters.toMutableMap()
                    updatedCharacters[heroName] = (updatedCharacters[heroName] ?: 0) + 1
                    foundCharacters = updatedCharacters

                    // Reset states
                    scratchCompleted = false
                    showScratchCard = false
                    showTTHButton = false
                    showSearchImage = true
                    bluetoothViewModel.startScan(activity)
                }
            }

            if (showSearchImage) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (searchX.value + screenWidth / 2 - 60.dp.toPx()).toInt(),
                                (searchY.value + screenHeight / 2 - 60.dp.toPx()).toInt()
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

            if (showTTHButton && !showScratchCard) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(5f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ttr),
                        contentDescription = "TTR Button",
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { showScratchCard = true },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Scratch Card Screen
            if (expandedImage == R.drawable.hunt_the_heroes && showScratchCard && nearbyHeroDevice != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(6f),
                    contentAlignment = Alignment.Center
                ) {
                    ScratchCardScreen(
                        heroName = nearbyHeroDevice.name,
                        modifier = Modifier.offset(y = (-50).dp),
                        onScratchCompleted = { scratchCompleted = true }
                    )
                }
            }
            // Pop-up Animation for Revealed Hero
            if (scratchCompleted) {
                val offsetY by animateFloatAsState(
                    targetValue = if (scratchCompleted) screenHeight / 2 else 0f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                    label = "heroOffsetY"
                )

                Box(
                    modifier = Modifier
                        .offset(y = offsetY.dp)
                        .zIndex(7f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = when (nearbyHeroDevice?.name) {
                                "Iron_Man" -> R.drawable.iron_man
                                "Hulk" -> R.drawable.hulk_
                                "Captain Marvel" -> R.drawable.captain_marvel
                                "Captain America" -> R.drawable.captain_america
                                else -> R.drawable.search // Placeholder
                            }
                        ),
                        contentDescription = "Revealed Hero",
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                scaleX = transitionProgress // Scale horizontally
                                scaleY = transitionProgress // Scale vertically
                                alpha = transitionProgress // Fade in
                            }
                    )
                }


                // Particle Explosion Effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    particles.forEach { (x, y) ->
                        drawCircle(
                            color = Color(
                                Random.nextInt(256),
                                Random.nextInt(256),
                                Random.nextInt(256)
                            ),
                            radius = 5f,
                            center = Offset(x, y)
                        )
                    }
                }

            }

            // Load hero images as ImageBitmap outside the Canvas
            val heroImages = remember {
                mapOf(
                    "Iron_Man" to R.drawable.iron_man,
                    "Hulk" to R.drawable.hulk_,
                    "Captain Marvel" to R.drawable.captain_marvel,
                    "Captain America" to R.drawable.captain_america,
                    "Default" to R.drawable.search
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                heroAnimationState.forEach { (heroName, startPosition) ->
                    // Interpolate the position of the hero
                    val animatedPosition by animateOffsetAsState(
                        targetValue = gameBoxPosition,
                        animationSpec = tween(
                            durationMillis = 1000,
                            easing = FastOutSlowInEasing
                        ),
                        label = "heroAnimation"
                    )

                    // Use Image composable with painterResource
                    Image(
                        painter = painterResource(id = heroImages[heroName] ?: heroImages["Default"]!!),
                        contentDescription = null,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    animatedPosition.x.roundToInt(),
                                    animatedPosition.y.roundToInt()
                                )
                            } // Convert Offset to IntOffset
                            .size(50.dp), // Adjust size as needed
                        alpha = 1f
                    )
                }
            }


            // Pop-up with Found Characters
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
                    .zIndex(8f),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = showPopup && expandedImage == R.drawable.hunt_the_heroes,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Found Characters:",
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            foundCharacters.forEach { (character, count) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = when (character) {
                                                "Iron_Man" -> R.drawable.iron_man
                                                "Hulk" -> R.drawable.hulk_
                                                "Captain Marvel" -> R.drawable.captain_marvel
                                                "Captain America" -> R.drawable.captain_america
                                                else -> R.drawable.search
                                            }
                                        ),
                                        contentDescription = character,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        text = "×$count",
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // Guess the Character Logic
        if (expandedImage == R.drawable.guess_the_character) {
            var showRadar by remember { mutableStateOf(false) }
            var showCharacterReveal by remember { mutableStateOf(false) }
            var detectedCharacter by remember { mutableStateOf<String?>(null) }

            val nearbyHeroForGuessing = bluetoothDevices.find { device ->
                device.name in allowedHeroes &&
                        device.rssi.toInt() in -40..0 &&
                        device.name !in foundCharacters.keys
            }

//            val nearbyHeroForGuessing = bluetoothDevices.find { device ->
//                device.name in allowedHeroes &&
//                        device.rssi.toInt() in -40..0
//            }

            LaunchedEffect(expandedImage) {
                if (expandedImage == R.drawable.guess_the_character) {
                    // Add this line
                    bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.GUESS_THE_CHARACTER)

                    delay(100)
                    showRadar = true
                    bluetoothViewModel.startScan(activity)
                } else {
                    // Add this line when exiting the mode
                    bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.NONE)
                    showRadar = false
                    showCharacterReveal = false
                }
            }
            LaunchedEffect(nearbyHeroForGuessing) {
                if (nearbyHeroForGuessing != null && expandedImage == R.drawable.guess_the_character) {
                    delay(1500)
                    detectedCharacter = nearbyHeroForGuessing.name
                    showCharacterReveal = true
                } else {
                    showCharacterReveal = false
                    detectedCharacter = null
                }
            }

            if (showRadar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(7f)
                        .offset(y = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RadarScreenWithRotatingLine()

                    if (showCharacterReveal && detectedCharacter != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .zIndex(8f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = when (detectedCharacter) {
                                            "Iron_Man" -> R.drawable.iron_man
                                            "Hulk" -> R.drawable.hulk_
                                            "Captain Marvel" -> R.drawable.captain_marvel
                                            "Captain America" -> R.drawable.captain_america
                                            else -> R.drawable.search
                                        }
                                    ),
                                    contentDescription = "Character Revealed",
                                    modifier = Modifier.size(200.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = detectedCharacter ?: "",
                                    style = MaterialTheme.typography.h6,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(
                                            Color(0x99000000),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }


        // Sound Button - Bottom Right
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable { isSoundOn = !isSoundOn }
        ) {
            Image(
                painter = painterResource(id = if (isSoundOn) R.drawable.soundon else R.drawable.soundoff),
                contentDescription = if (isSoundOn) "Sound On" else "Sound Off",
                modifier = Modifier.size(60.dp)
            )
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Game Box Button in the bottom-left corner
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(80.dp)
                    .clickable {
                        isGameBoxOpen = true // Open the Game Box when clicked
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.close_box),
                    contentDescription = "Game Box",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Display the number of collected heroes on the Game Box
                if (foundCharacters.isNotEmpty()) {
                    Text(
                        text = "${foundCharacters.size}",
                        style = MaterialTheme.typography.body2,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                }
            }

    // Open Game Box Popup
    if (isGameBoxOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { isGameBoxOpen = false }, // Close the popup when clicking outside
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Collected Heroes",
                    style = MaterialTheme.typography.h6,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display all collected heroes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    foundCharacters.forEach { (character, count) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(
                                    id = when (character) {
                                        "Iron_Man" -> R.drawable.iron_man
                                        "Hulk" -> R.drawable.hulk_
                                        "Captain Marvel" -> R.drawable.captain_marvel
                                        "Captain America" -> R.drawable.captain_america
                                        else -> R.drawable.search
                                    }
                                ),
                                contentDescription = character,
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                text = "×$count",
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            }
        }
    }
}
        }
    }