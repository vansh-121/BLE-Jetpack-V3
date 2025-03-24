package com.example.ble_jetpackcompose

import android.app.Activity
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

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
    var showHeroSelectionDialog by remember { mutableStateOf(false) }
    var currentHeroToGuess by remember { mutableStateOf<String?>(null) }
    var showGuessResult by remember { mutableStateOf<Boolean?>(null) } // null, true (correct), false (incorrect)
    var guessAnimationRunning by remember { mutableStateOf(false) }


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
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Create and manage MediaPlayer using DisposableEffect with lifecycle
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.bgmusic).apply {
            isLooping = true
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            setVolume(0.5f, 0.5f)
        }
    }
    // Use different audio attributes for sound effects
    val correctSoundPlayer = remember {
        MediaPlayer.create(context, R.raw.yayy).apply {
            isLooping = false
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            setVolume(1.0f, 1.0f) // Full volume for effects
        }
    }

    val wrongSoundPlayer = remember {
        MediaPlayer.create(context, R.raw.wrong).apply {
            isLooping = false
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            setVolume(1.0f, 1.0f) // Full volume for effects
        }
    }


    // Your other MediaPlayer instances

    // Properly dispose of all resources when the composable leaves composition OR when the activity is destroyed
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Pause the audio when the app goes to background
                if (mediaPlayer.isPlaying) mediaPlayer.pause()
                // Also pause any other playing audio
                if (correctSoundPlayer.isPlaying) correctSoundPlayer.pause()
                if (wrongSoundPlayer.isPlaying) wrongSoundPlayer.pause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // Resume background music if it was on when app returns to foreground
                if (isSoundOn && !mediaPlayer.isPlaying) mediaPlayer.start()
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                // Release all media resources when activity is destroyed
                mediaPlayer.release()
                correctSoundPlayer.release()
                wrongSoundPlayer.release()
                bluetoothViewModel.stopScan()
            }
        }

        // Add the observer to the lifecycle
        lifecycle.addObserver(observer)

        // When the effect leaves composition, remove the observer and clean up
        onDispose {
            lifecycle.removeObserver(observer)
            bluetoothViewModel.stopScan()
            mediaPlayer.release()
            correctSoundPlayer.release()
            wrongSoundPlayer.release()
        }
    }




// Proper cleanup of Bluetooth scanning
//    DisposableEffect(Unit) {
//        onDispose {
//            bluetoothViewModel.stopScan()
//            mediaPlayer.release()
//            correctSoundPlayer.release()
//            wrongSoundPlayer.release()
//        }
//    }

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
            animation = tween(durationMillis = 5000, easing = LinearEasing), // Slower animation
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
            repeat(35) { // Reduced particle count
                val angle = Random.nextDouble(0.0, 360.0)
                val distance = Random.nextFloat() * 50
                val startX = (centerX + cos(Math.toRadians(angle)) * distance).toFloat()
                val startY = (centerY + sin(Math.toRadians(angle)) * distance).toFloat()
                particles.add(Triple(startX, startY, angle))
            }
        }
    }
    LaunchedEffect(particles.isNotEmpty()) {
        if (particles.isNotEmpty()) {
            // Reduce update frequency
            val particlesToUpdate = particles.size.coerceAtMost(10) // Limit to 10 particles at a time

            repeat(5) { // Reduce animatio
                // n duration
                particles.take(particlesToUpdate).forEachIndexed { index, (x, y, angle) ->
                    val speedX = (cos(Math.toRadians(angle)) * 5).toFloat() // Reduce speed
                    val speedY = (sin(Math.toRadians(angle)) * 5).toFloat()

                    if (index < particles.size) {
                        particles[index] = Triple(
                            x + speedX,
                            y + speedY,
                            angle
                        )
                    }
                }
                delay(50) // Slower frame rate
            }
            particles.clear() // Clear after brief animation
        }
    }

    if (particles.isNotEmpty()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { (x, y, _) ->
                // Simplified drawing
                drawCircle(
                    color = Color.Red, // Use fixed color instead of random
                    radius = 5f,
                    center = Offset(x, y)
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
                        imageResId = R.drawable.guess_the_character,
                        contentDescription = "Hunt the Heroes",
                        isExpanded = expandedImage == R.drawable.hunt_the_heroes,
                        expandedImageResId = R.drawable.gth,
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
                        imageResId = R.drawable.hunt_the_heroes,
                        contentDescription = "Guess the Character",
                        isExpanded = expandedImage == R.drawable.guess_the_character,
                        expandedImageResId = R.drawable.hth,
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

        // Add scan throttling
        val isScanningActive = remember { mutableStateOf(false) }
        val scanInterval = 3000L // milliseconds between scans

        LaunchedEffect(expandedImage) {
            if (expandedImage == R.drawable.hunt_the_heroes || expandedImage == R.drawable.guess_the_character) {
                // Set appropriate game mode
                bluetoothViewModel.setGameMode(
                    when (expandedImage) {
                        R.drawable.hunt_the_heroes -> BluetoothScanViewModel.GameMode.HUNT_THE_HEROES
                        else -> BluetoothScanViewModel.GameMode.GUESS_THE_CHARACTER
                    }
                )

                // Throttled scanning
                while (true) {
                    if (!isScanningActive.value) {
                        isScanningActive.value = true
                        bluetoothViewModel.startScan(activity)
                        delay(scanInterval)
                        bluetoothViewModel.stopScan() // Make sure you implement this method
                        isScanningActive.value = false
                    }
                    delay(500) // Check if we need to scan again after a short delay
                }
            } else {
                bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.NONE)
                bluetoothViewModel.stopScan()
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

            // Replace the existing TTR button section
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
                            .clickable {
                                // Instead of showing scratchcard directly, show the hero selection dialog
                                showHeroSelectionDialog = true
                                // Randomly select a hero to guess from detected devices
                                currentHeroToGuess = nearbyHeroDevice?.name
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Add this inside the main Box, at the same level as other dialogs
            if (showHeroSelectionDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .zIndex(10f),
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
                            text = "Which hero do you think is nearby?",
                            fontFamily = helveticaFont,
                            style = MaterialTheme.typography.h6,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display all heroes for selection
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            allowedHeroes.forEach { character ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        // Handle hero selection
                                        val isCorrect = character == currentHeroToGuess
                                        showGuessResult = isCorrect
                                        guessAnimationRunning = true
                                        showHeroSelectionDialog = false

                                        // Play appropriate sound
                                        if (isCorrect) {
                                            correctSoundPlayer.start()
                                        } else {
                                            wrongSoundPlayer.start()
                                        }
                                    }
                                ) {
                                    // Get the resource ID for the character
                                    val resourceId = when (character) {
                                        "Iron_Man" -> R.drawable.iron_man
                                        "Hulk" -> R.drawable.hulk_
                                        "Captain Marvel" -> R.drawable.captain_marvel
                                        "Captain America" -> R.drawable.captain_america
                                        "Scarlet Witch" -> R.drawable.scarlet_witch_
                                        "Black Widow" -> R.drawable.black_widow_
                                        "Wasp" -> R.drawable.wasp_
                                        "Hela" -> R.drawable.hela_
                                        "Thor" -> R.drawable.thor_
                                        "Spider Man" -> R.drawable.spider_man_
                                        else -> R.drawable.search
                                    }

                                    Image(
                                        painter = painterResource(id = resourceId),
                                        contentDescription = character,
                                        modifier = Modifier.size(80.dp)
                                    )

                                    Text(
                                        text = character.replace("_", " "),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Add this to show the result of the guess
            if (guessAnimationRunning) {
                // Use LaunchedEffect to handle the animation timing
                LaunchedEffect(guessAnimationRunning) {
                    delay(3000) // Show result for 3 seconds
                    guessAnimationRunning = false

                    if (showGuessResult == true) {
                        // If correct, proceed to scratch card and reveal
                        showScratchCard = true
                    } else {
                        // If wrong, reset and allow another guess
                        showHeroSelectionDialog = true
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .zIndex(10f),
                    contentAlignment = Alignment.Center
                ) {
                    if (showGuessResult == true) {
                        // Correct guess animation
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Party popper animation
//                            PartyPopperAnimation()

                            Text(
                                text = "Correct!",
                                style = MaterialTheme.typography.h4,
                                fontFamily = helveticaFont,
                                color = Color.Green
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "You found ${currentHeroToGuess?.replace("_", " ")}!",
                                style = MaterialTheme.typography.h6,
                                fontFamily = helveticaFont,
                                color = Color.White
                            )
                        }
                    } else {
                        // Wrong guess animation
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Wrong Guess!",
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.h4,
                                color = Color.Red
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Try again...",
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.h6,
                                color = Color.White
                            )
                        }
                    }
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
                                "Scarlet Witch" -> R.drawable.scarlet_witch_ // Replace with actual resource
                                "Black Widow" -> R.drawable.black_widow_ // Replace with actual resource
                                "Wasp" -> R.drawable.wasp_ // Replace with actual resource
                                "Hela" -> R.drawable.hela_ // Replace with actual resource
                                "Thor" -> R.drawable.thor_ // Replace with actual resource
                                "Spider Man" -> R.drawable.spider_man_ // Replace with actual resource
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

            // Use lazy loading for hero images
            val heroImages = produceState<Map<String, Int>>(initialValue = emptyMap()) {
                value = mapOf(
                    "Iron_Man" to R.drawable.iron_man,
                    "Hulk" to R.drawable.hulk_,
                    "Captain Marvel" to R.drawable.captain_marvel,
                    "Captain America" to R.drawable.captain_america,
                    "Scarlet Witch" to R.drawable.scarlet_witch_,
                    "Black Widow" to R.drawable.black_widow_,
                    "Wasp" to R.drawable.wasp_,
                    "Hela" to R.drawable.hela_,
                    "Thor" to R.drawable.thor_,
                    "Spider Man" to R.drawable.spider_man_,
                    "Default" to R.drawable.search
                )
            }.value

            Box(modifier = Modifier.fillMaxSize()) {
                heroAnimationState.forEach { (heroName, _) ->
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
                        painter = painterResource(
                            id = heroImages[heroName] ?: heroImages["Default"]!!
                        ),
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
        }
        // Guess the Character Logic
        if (expandedImage == R.drawable.guess_the_character) {
            var showRadar by remember { mutableStateOf(false) }
            var showCharacterReveal by remember { mutableStateOf(false) }
            var detectedCharacters by remember { mutableStateOf<List<String>>(emptyList()) }

            // Detect nearby Bluetooth devices (multiple)
            val nearbyHeroes = bluetoothDevices.filter {
                it.name in allowedHeroes && it.rssi.toInt() in -40..0
            }

            // Show radar when Guess the Character is selected
            LaunchedEffect(expandedImage) {
                if (expandedImage == R.drawable.guess_the_character) {
                    bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.GUESS_THE_CHARACTER)
                    delay(100)
                    showRadar = true
                    bluetoothViewModel.startScan(activity)
                } else {
                    showRadar = false
                    showCharacterReveal = false
                    bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.NONE)
                }
            }

            // Handle character detection
            LaunchedEffect(nearbyHeroes) {
                if (nearbyHeroes.isNotEmpty() && expandedImage == R.drawable.guess_the_character) {
                    delay(1000) // Slight delay for dramatic effect
                    detectedCharacters = nearbyHeroes.map { it.name }
                    showCharacterReveal = true
                } else {
                    showCharacterReveal = false
                    detectedCharacters = emptyList()
                }
            }

            // Use lazy loading for hero images
            val heroImages = produceState <Map<String, Int>>(initialValue = emptyMap()) {
                value = mapOf(
                    "Iron_Man" to R.drawable.iron_man,
                    "Hulk" to R.drawable.hulk_,
                    "Captain Marvel" to R.drawable.captain_marvel,
                    "Captain America" to R.drawable.captain_america,
                    "Scarlet Witch" to R.drawable.scarlet_witch_,
                    "Black Widow" to R.drawable.black_widow_,
                    "Wasp" to R.drawable.wasp_,
                    "Hela" to R.drawable.hela_,
                    "Thor" to R.drawable.thor_,
                    "Spider Man" to R.drawable.spider_man_,
                    "Default" to R.drawable.search
                )
            }.value
            // Create a list of all heroes with their positions on the radar
            val allHeroesDeviceList = remember {
                val positions = generateMultiplePositions(allowedHeroes.size, 600f)

                // Pair each hero with its image resource and position
                allowedHeroes.mapIndexed { index, heroName ->
                    val imageRes = heroImages[heroName] ?: R.drawable.search
                    Pair(imageRes, positions.getOrElse(index) { Offset.Zero })
                }
            }

            // Display radar with all heroes

            if (showRadar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(7f)
                        .offset(y = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RadarScreenWithAllCharacters(
                        activatedDevices = detectedCharacters,
                        deviceList = allHeroesDeviceList
                    )
                }
            }

            // Optional: Display detected character names at the bottom
            if (detectedCharacters.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                        .zIndex(8f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0x99000000), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Heroes Detected:",
                            //style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        detectedCharacters.forEach { heroName ->
                            Text(
                                text = heroName,
                                //style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Game Box Button in the bottom-left corner
                Box(
                    modifier = Modifier
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
                    Text(
                        text = "${foundCharacters.size}/${allowedHeroes.size}",
                        style = MaterialTheme.typography.body2,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                }

                // Sound Button - Moved to bottom-right corner
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { isSoundOn = !isSoundOn }
                        .zIndex(10f) // Higher z-index to ensure it doesn't get covered
                ) {
                    Image(
                        painter = painterResource(id = if (isSoundOn) R.drawable.soundon else R.drawable.soundoff),
                        contentDescription = if (isSoundOn) "Sound On" else "Sound Off",
                        modifier = Modifier.fillMaxSize()
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
                        text = "Heroes Collection",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.h6,
                        color = Color.Black
                    )

                    Text(
                        text = "Collected: ${foundCharacters.size}/${allowedHeroes.size}",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.body1,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display all possible heroes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        allowedHeroes.forEach { character ->
                            val isCollected = character in foundCharacters.keys
                            val count = foundCharacters[character] ?: 0

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Get the resource ID for the character
                                val resourceId = when (character) {
                                    "Iron_Man" -> R.drawable.iron_man
                                    "Hulk" -> R.drawable.hulk_
                                    "Captain Marvel" -> R.drawable.captain_marvel
                                    "Captain America" -> R.drawable.captain_america
                                    "Scarlet Witch" -> R.drawable.scarlet_witch_ // Replace with actual resource
                                    "Black Widow" -> R.drawable.black_widow_ // Replace with actual resource
                                    "Wasp" -> R.drawable.wasp_ // Replace with actual resource
                                    "Hela" -> R.drawable.hela_ // Replace with actual resource
                                    "Thor" -> R.drawable.thor_ // Replace with actual resource
                                    "Spider Man" -> R.drawable.spider_man_ // Replace with actual resource
                                    else -> R.drawable.search
                                }

                                // Use two separate Image composables instead of modifiers
                                if (isCollected) {
                                    // Show colored version for collected heroes
                                    Image(
                                        painter = painterResource(id = resourceId),
                                        contentDescription = character,
                                        modifier = Modifier.size(80.dp)
                                    )
                                } else {
                                    // Show grayscale version with alpha for uncollected heroes
                                    Box(modifier = Modifier.size(80.dp)) {
                                        Image(
                                            painter = painterResource(id = resourceId),
                                            contentDescription = character,
                                            colorFilter = ColorFilter.tint(
                                                Color.Gray.copy(alpha = 0.5f),
                                                blendMode = BlendMode.SrcAtop
                                            ),
                                            modifier = Modifier
                                                .size(80.dp)
                                                .alpha(0.7f)
                                        )

                                        // Add a semi-transparent overlay to indicate it's not collected yet
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(Color.Black.copy(alpha = 0.3f))
                                        )
                                    }
                                }

                                // Show the count if collected, otherwise show "?"
                                Text(
                                    text = if (isCollected) "×$count" else "?",
                                    style = MaterialTheme.typography.body2,
                                    color = if (isCollected) Color.Black else Color.Gray
                                )

                                // Show the character name
                                Text(
                                    text = character.replace("_", " "),
                                    style = MaterialTheme.typography.caption,
                                    color = if (isCollected) Color.Black else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

