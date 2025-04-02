//package com.example.ble_jetpackcompose
//
//import android.app.Activity
//import android.media.MediaPlayer
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.animateOffsetAsState
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.zIndex
//import kotlinx.coroutines.delay
//import kotlin.math.cos
//import kotlin.math.roundToInt
//import kotlin.math.sin
//import kotlin.random.Random
//
//@Composable
//fun GuessTheCharacterScreen(
//    activity: Activity,
//    bluetoothViewModel: BluetoothScanViewModel<Any>,
//    screenWidth: Float,
//    screenHeight: Float,
//    translatedText: TranslatedGameScreenText,
//    allowedHeroes: List<String>,
//    textColor: Color,
//    secondaryTextColor: Color,
//    overlayColor: Color,
//    dialogBackgroundColor: Color,
//    correctSoundPlayer: MediaPlayer,
//    wrongSoundPlayer: MediaPlayer,
//    foundCharacters: Map<String, Int>,
//    onFoundCharactersUpdated: (Map<String, Int>) -> Unit
//) {
//    var showScratchCard by remember { mutableStateOf(false) }
//    var showTTHButton by remember { mutableStateOf(false) }
//    var showSearchImage by remember { mutableStateOf(true) }
//    var showHeroSelectionDialog by remember { mutableStateOf(false) }
//    var currentHeroToGuess by remember { mutableStateOf<String?>(null) }
//    var showGuessResult by remember { mutableStateOf<Boolean?>(null) }
//    var guessAnimationRunning by remember { mutableStateOf(false) }
//    var scratchCompleted by remember { mutableStateOf(false) }
//
//    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
//
//    val infiniteTransition = rememberInfiniteTransition(label = "searchButtonAnimation")
//    val angle by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 5000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "searchButtonRotation"
//    )
//
//    val radius = 100f
//    val searchX = remember { derivedStateOf { radius * cos(Math.toRadians(angle.toDouble())).toFloat() } }
//    val searchY = remember { derivedStateOf { radius * sin(Math.toRadians(angle.toDouble())).toFloat() } }
//
//    val animateHero by remember { mutableStateOf<String?>(null) }
//    val transitionProgress by animateFloatAsState(
//        targetValue = if (animateHero != null) 2f else 0f,
//        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
//        label = "heroTransition"
//    )
//    val gameBoxPosition = remember { Offset(40f, screenHeight - 100f) }
//    val centerPosition = remember { Offset(screenWidth / 2, screenHeight / 2) }
//    val heroAnimationState = remember { mutableStateMapOf<String, Offset>() } // Map for hero positions
//
//    val particles = remember { mutableStateListOf<Triple<Float, Float, Double>>() }
//    val centerX = screenWidth / 2
//    val centerY = screenHeight / 2
//
//    LaunchedEffect(scratchCompleted) {
//        if (scratchCompleted && screenWidth > 0 && screenHeight > 0) {
//            repeat(35) {
//                val angle = Random.nextDouble(0.0, 360.0)
//                val distance = Random.nextFloat() * 50
//                val startX = (centerX + cos(Math.toRadians(angle)) * distance).toFloat()
//                val startY = (centerY + sin(Math.toRadians(angle)) * distance).toFloat()
//                particles.add(Triple(startX, startY, angle))
//            }
//        }
//    }
//    LaunchedEffect(particles.isNotEmpty()) {
//        if (particles.isNotEmpty()) {
//            val particlesToUpdate = particles.size.coerceAtMost(10)
//            repeat(5) {
//                particles.take(particlesToUpdate).forEachIndexed { index, (x, y, angle) ->
//                    val speedX = (cos(Math.toRadians(angle)) * 5).toFloat()
//                    val speedY = (sin(Math.toRadians(angle)) * 5).toFloat()
//                    if (index < particles.size) {
//                        particles[index] = Triple(x + speedX, y + speedY, angle)
//                    }
//                }
//                delay(50)
//            }
//            particles.clear()
//        }
//    }
//
//    val isScanningActive = remember { mutableStateOf(false) }
//    val scanInterval = 3000L
//
//    LaunchedEffect(Unit) {
//        bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.GUESS_THE_CHARACTER)
//        while (true) {
//            if (!isScanningActive.value) {
//                isScanningActive.value = true
//                bluetoothViewModel.startScan(activity)
//                delay(scanInterval)
//                bluetoothViewModel.stopScan()
//                isScanningActive.value = false
//            }
//            delay(500)
//        }
//    }
//
//    val nearbyHeroDevice = bluetoothDevices.find {
//        it.name in allowedHeroes && it.rssi.toInt() in -40..0 && it.name !in foundCharacters.keys
//    }
//
//    LaunchedEffect(nearbyHeroDevice) {
//        if (nearbyHeroDevice != null) {
//            showTTHButton = true
//            showSearchImage = false
//        } else {
//            showTTHButton = false
//            showSearchImage = true
//        }
//    }
//
//    LaunchedEffect(scratchCompleted) {
//        if (scratchCompleted && nearbyHeroDevice != null) {
//            val heroName = nearbyHeroDevice.name
//            heroAnimationState[heroName] = centerPosition // Start at center
//            val updatedCharacters = foundCharacters.toMutableMap()
//            updatedCharacters[heroName] = (updatedCharacters[heroName] ?: 0) + 1
//            onFoundCharactersUpdated(updatedCharacters) // Update parent state
//            delay(1000) // Wait for animation to complete
//            heroAnimationState.remove(heroName) // Remove after animation
//            scratchCompleted = false
//            showScratchCard = false
//            showTTHButton = false
//            showSearchImage = true
//            bluetoothViewModel.startScan(activity)
//        }
//    }
//
//    if (particles.isNotEmpty()) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            particles.forEach { (x, y, _) ->
//                drawCircle(color = Color.Red, radius = 5f, center = Offset(x, y))
//            }
//        }
//    }
//
//    if (showSearchImage) {
//        Box(
//            modifier = Modifier
//                .offset {
//                    IntOffset(
//                        (searchX.value + screenWidth / 2 - 60.dp.toPx()).toInt(),
//                        (searchY.value + screenHeight / 2 - 60.dp.toPx()).toInt()
//                    )
//                }
//                .zIndex(2f)
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.search),
//                contentDescription = "Search Button",
//                modifier = Modifier.size(50.dp),
//                contentScale = ContentScale.Crop
//            )
//        }
//    }
//
//    if (showTTHButton && !showScratchCard) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .zIndex(5f),
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.ttr),
//                contentDescription = "TTR Button",
//                modifier = Modifier
//                    .size(80.dp)
//                    .clickable {
//                        showHeroSelectionDialog = true
//                        currentHeroToGuess = nearbyHeroDevice?.name
//                    },
//                contentScale = ContentScale.Crop
//            )
//        }
//    }
//
//    if (showHeroSelectionDialog) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(overlayColor)
//                .zIndex(10f),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//                modifier = Modifier
//                    .background(dialogBackgroundColor, shape = RoundedCornerShape(16.dp))
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = translatedText.whichHeroNearby,
//                    fontFamily = helveticaFont,
//                    style = MaterialTheme.typography.h6,
//                    color = textColor
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(16.dp),
//                    modifier = Modifier.horizontalScroll(rememberScrollState())
//                ) {
//                    allowedHeroes.forEach { character ->
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.clickable {
//                                val isCorrect = character == currentHeroToGuess
//                                showGuessResult = isCorrect
//                                guessAnimationRunning = true
//                                showHeroSelectionDialog = false
//                                if (isCorrect) correctSoundPlayer.start() else wrongSoundPlayer.start()
//                            }
//                        ) {
//                            val resourceId = when (character) {
//                                "Iron_Man" -> R.drawable.iron_man
//                                "Hulk" -> R.drawable.hulk_
//                                "Captain Marvel" -> R.drawable.captain_marvel
//                                "Captain America" -> R.drawable.captain_america
//                                "Scarlet Witch" -> R.drawable.scarlet_witch_
//                                "Black Widow" -> R.drawable.black_widow_
//                                "Wasp" -> R.drawable.wasp_
//                                "Hela" -> R.drawable.hela_
//                                "Thor" -> R.drawable.thor_
//                                "Spider Man" -> R.drawable.spider_man_
//                                else -> R.drawable.search
//                            }
//
//                            Image(
//                                painter = painterResource(id = resourceId),
//                                contentDescription = character,
//                                modifier = Modifier.size(80.dp)
//                            )
//
//                            Text(
//                                text = character.replace("_", " "),
//                                style = MaterialTheme.typography.caption,
//                                color = textColor
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    if (guessAnimationRunning) {
//        LaunchedEffect(guessAnimationRunning) {
//            delay(3000)
//            guessAnimationRunning = false
//            if (showGuessResult == true) showScratchCard = true else showHeroSelectionDialog = true
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(overlayColor)
//                .zIndex(10f),
//            contentAlignment = Alignment.Center
//        ) {
//            if (showGuessResult == true) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text = translatedText.correctGuess,
//                        style = MaterialTheme.typography.h4,
//                        fontFamily = helveticaFont,
//                        color = Color.Green
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "${translatedText.youFound} ${currentHeroToGuess?.replace("_", " ")}!",
//                        style = MaterialTheme.typography.h6,
//                        fontFamily = helveticaFont,
//                        color = textColor
//                    )
//                }
//            } else {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text = translatedText.wrongGuess,
//                        fontFamily = helveticaFont,
//                        style = MaterialTheme.typography.h4,
//                        color = Color.Red
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = translatedText.tryAgain,
//                        fontFamily = helveticaFont,
//                        style = MaterialTheme.typography.h6,
//                        color = textColor
//                    )
//                }
//            }
//        }
//    }
//
//    if (showScratchCard && nearbyHeroDevice != null) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .zIndex(6f),
//            contentAlignment = Alignment.Center
//        ) {
//            ScratchCardScreen(
//                heroName = nearbyHeroDevice.name,
//                modifier = Modifier.offset(y = (-50).dp),
//                onScratchCompleted = { scratchCompleted = true }
//            )
//        }
//    }
//
//    if (scratchCompleted) {
//        val offsetY by animateFloatAsState(
//            targetValue = if (scratchCompleted) screenHeight / 2 else 0f,
//            animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
//            label = "heroOffsetY"
//        )
//
//        Box(
//            modifier = Modifier
//                .offset(y = offsetY.dp)
//                .zIndex(7f),
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = painterResource(
//                    id = when (nearbyHeroDevice?.name) {
//                        "Iron_Man" -> R.drawable.iron_man
//                        "Hulk" -> R.drawable.hulk_
//                        "Captain Marvel" -> R.drawable.captain_marvel
//                        "Captain America" -> R.drawable.captain_america
//                        "Scarlet Witch" -> R.drawable.scarlet_witch_
//                        "Black Widow" -> R.drawable.black_widow_
//                        "Wasp" -> R.drawable.wasp_
//                        "Hela" -> R.drawable.hela_
//                        "Thor" -> R.drawable.thor_
//                        "Spider Man" -> R.drawable.spider_man_
//                        else -> R.drawable.search
//                    }
//                ),
//                contentDescription = "Revealed Hero",
//                modifier = Modifier
//                    .size(150.dp)
//                    .graphicsLayer {
//                        scaleX = transitionProgress
//                        scaleY = transitionProgress
//                        alpha = transitionProgress
//                    }
//            )
//        }
//
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            particles.forEach { (x, y) ->
//                drawCircle(
//                    color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)),
//                    radius = 5f,
//                    center = Offset(x, y)
//                )
//            }
//        }
//    }
//
//    val heroImages = produceState<Map<String, Int>>(initialValue = emptyMap()) {
//        value = mapOf(
//            "Iron_Man" to R.drawable.iron_man,
//            "Hulk" to R.drawable.hulk_,
//            "Captain Marvel" to R.drawable.captain_marvel,
//            "Captain America" to R.drawable.captain_america,
//            "Scarlet Witch" to R.drawable.scarlet_witch_,
//            "Black Widow" to R.drawable.black_widow_,
//            "Wasp" to R.drawable.wasp_,
//            "Hela" to R.drawable.hela_,
//            "Thor" to R.drawable.thor_,
//            "Spider Man" to R.drawable.spider_man_,
//            "Default" to R.drawable.search
//        )
//    }.value
//
//    Box(modifier = Modifier.fillMaxSize().zIndex(15f)) {
//        heroAnimationState.forEach { (heroName, initialPosition) ->
//            val animatedPosition by animateOffsetAsState(
//                targetValue = if (heroAnimationState.containsKey(heroName)) gameBoxPosition else initialPosition,
//                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
//                label = "heroAnimation_$heroName"
//            )
//            Image(
//                painter = painterResource(id = heroImages[heroName] ?: heroImages["Default"]!!),
//                contentDescription = null,
//                modifier = Modifier
//                    .offset { IntOffset(animatedPosition.x.roundToInt(), animatedPosition.y.roundToInt()) }
//                    .size(50.dp),
//                alpha = 1f
//            )
//        }
//    }
//}