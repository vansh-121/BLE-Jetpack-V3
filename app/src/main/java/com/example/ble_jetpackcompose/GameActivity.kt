package com.example.ble_jetpackcompose
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
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
@Composable
fun AnimatedImageButton(
    imageResId: Int,
    contentDescription: String,
    isExpanded: Boolean,
    expandedImageResId: Int,
    screenWidth: Float,
    screenHeight: Float,
    currentPosition: IntOffset,
    onPositioned: (IntOffset) -> Unit,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = isExpanded, label = "buttonExpansionTransition")

    // Animate scale using transition
    val scale = transition.animateFloat(
        label = "buttonScaleAnimation",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { expanded ->
        if (expanded) 2f else 1f
    }

    // Animate alpha using transition
    val alpha = transition.animateFloat(
        label = "buttonAlphaAnimation",
        transitionSpec = { tween(300) }
    ) { expanded ->
        if (expanded) 1f else 0.9f
    }

    // Animate the X offset
    val offsetX = transition.animateFloat(
        label = "buttonOffsetXAnimation",
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) }
    ) { expanded ->
        if (expanded) {
            val currentX = currentPosition.x.toFloat()
            val buttonWidthPx = dpToPx(380.dp)
            val targetX = (screenWidth - buttonWidthPx) / 2f
            targetX - currentX
        } else 0f
    }

    // Animate the Y offset
    val offsetY = transition.animateFloat(
        label = "buttonOffsetYAnimation",
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) }
    ) { expanded ->
        if (expanded) {
            val currentY = currentPosition.y.toFloat()
            val buttonHeightPx = dpToPx(282.dp)
            val targetY = (screenHeight - buttonHeightPx) / 2f
            targetY - currentY
        } else 0f
    }

    Box {
        Image(
            painter = painterResource(id = if (isExpanded) expandedImageResId else imageResId),
            contentDescription = contentDescription,
            modifier = Modifier
                .width(209.dp)
                .height(282.dp)
                .onGloballyPositioned { coordinates ->
                    if (!isExpanded) {
                        onPositioned(
                            IntOffset(
                                coordinates.boundsInWindow().left.roundToInt(),
                                coordinates.boundsInWindow().top.roundToInt()
                            )
                        )
                    }
                }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                }
                .clickable(
                    onClick = onClick,
                    indication = null,  // Disable the ripple effect
                    interactionSource = remember { MutableInteractionSource() } // Remove ripple and interaction feedback
                ),
            contentScale = ContentScale.Crop
        )

    }
}

@Composable
fun RadarScreenWithRotatingLine(modifier: Modifier = Modifier) {
    val radarColor = colorResource(id = R.color.radar_color) // Color for circles
    val centerCircleColor = colorResource(id = R.color.radar_color) // Focal circle color

    Box(
        modifier = modifier, // Apply the passed modifier here
        contentAlignment = Alignment.Center
    ) {
        RadarLayoutWithRotatingLine(radarColor, centerCircleColor)
    }
}

@Composable
fun RadarLayoutWithRotatingLine(
    radarColor: Color,
    centerCircleColor: Color
) {
    val lineRotation = remember { Animatable(0f) }

    // State to manage the visibility and position of the image
    val showImage = remember { mutableStateOf(false) }
    val imagePosition = remember { mutableStateOf(Offset.Zero) }

    // State for burst effect
    val burstEffectVisible = remember { mutableStateOf(false) }
    val burstScale = remember { Animatable(0f) }

    // Infinite rotation animation
    LaunchedEffect(Unit) {
        lineRotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // Show image after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000L) // Wait for 3 seconds
        burstEffectVisible.value = true
        imagePosition.value = generateRandomPosition(radius = 300f) // Generate random position
        delay(300) // Brief delay to show burst effect
        showImage.value = true
        burstScale.animateTo(1f, animationSpec = tween(500))
    }

    Box(
        modifier = Modifier
            .size(300.dp) // Outer box size
            .clip(CircleShape)
            .background(Color.Transparent), // Background color for the radar
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxSize = size.minDimension
            val radius = boxSize / 2 // Outer circle matches half of the box size

            // Three concentric circles
            val circles = listOf(
                radius * 0.33f to 2.dp,  // Smallest circle
                radius * 0.66f to 4.dp,  // Medium circle
                radius to 6.dp           // Outermost circle
            )

            // Draw each circle
            circles.forEach { (circleRadius, strokeWidth) ->
                drawCircle(
                    color = radarColor,
                    radius = circleRadius,
                    style = Stroke(width = strokeWidth.toPx())
                )
            }

            // Draw the opaque center circle
            drawCircle(
                color = centerCircleColor,
                radius = radius * 0.06f, // Small circle at the center
                alpha = 1f // Fully opaque
            )

            // Draw the rotating line
            rotate(degrees = lineRotation.value) {
                drawLine(
                    color = radarColor,
                    start = center,
                    end = center.copy(
                        x = center.x,
                        y = center.y - radius // Use outer radius for line length
                    ),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // 3D Effect: Radar "pie slice" shadow effect
            val angle = lineRotation.value
            val shadowColor = Color.Black.copy(alpha = 0.2f) // Light shadow effect

            // Draw the radar sector (shadow effect)
            rotate(degrees = angle) {
                drawArc(
                    color = shadowColor,
                    startAngle = 240f,
                    sweepAngle = 30f, // Adjust the size of the shadow area
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Fill
                )
            }
        }

        // Display image with burst effect
        Box(
            modifier = Modifier
                .size(60.dp) // Set image size
                .offset(
                    x = with(LocalDensity.current) { imagePosition.value.x.toDp() },
                    y = with(LocalDensity.current) { imagePosition.value.y.toDp() }
                )
        ) {
            // Burst effect animation (glowing light effect)
            if (burstEffectVisible.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.3f), shape = CircleShape)
                        .scale(burstScale.value)
                        .animateContentSize()
                )
            }

            // Actual Image
            AnimatedVisibility(
                visible = showImage.value,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jjack), // Replace with your image resource
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Function to generate a random position within one of the two circles
fun generateRandomPosition(radius: Float): Offset {
    val randomRadius = listOf(radius * 0.33f, radius * 0.66f).random() // Choose one of the two radii
    val randomAngle = Math.random() * 2 * Math.PI // Random angle in radians

    val x = (randomRadius * cos(randomAngle)).toFloat()
    val y = (randomRadius * sin(randomAngle)).toFloat()

    return Offset(x, y)
}





//@OptIn(ExperimentalComposeUiApi::class) // Opting into experimental API @Composable
@Composable
fun ScratchCardScreen(modifier: Modifier = Modifier) {
    val overlayImage = ImageBitmap.imageResource(id = R.drawable.scratch)
    val baseImage = ImageBitmap.imageResource(id = R.drawable.inner)

    val currentPathState = remember { mutableStateOf(DraggedPath(path = Path(), width = 150f)) }
    var scratchedAreaPercentage by remember { mutableFloatStateOf(0f) }


    val canvasSizePx = with(LocalDensity.current) { 300.dp.toPx() }

    LaunchedEffect(Unit) {
        val stepSize = canvasSizePx / 8
        var scratchedArea = 0f
        canvasSizePx * canvasSizePx

        // Zigzag pattern
        for (y in 0..canvasSizePx.toInt() step (stepSize / 1.5f).toInt()) {
            // Alternate direction for each row
            val xRange = if (y % (stepSize.toInt() * 2) == 0) {
                (0..canvasSizePx.toInt() step (stepSize / 1.5f).toInt())
            } else {
                (canvasSizePx.toInt() downTo 0 step (stepSize / 1.5f).toInt())
            }

            for (x in xRange) {
                currentPathState.value.path.addOval(
                    androidx.compose.ui.geometry.Rect(
                        center = Offset(x.toFloat(), y.toFloat()),
                        radius = stepSize * 1.2f
                    )
                )

                // Add diagonal connections for smoother zigzag
                if (y > 0 && x > stepSize && x < canvasSizePx - stepSize) {
                    currentPathState.value.path.addOval(
                        androidx.compose.ui.geometry.Rect(
                            center = Offset(
                                x.toFloat() - stepSize/2,
                                y.toFloat() - stepSize/2
                            ),
                            radius = stepSize * 1.2f
                        )
                    )
                }

                scratchedArea = (y * canvasSizePx + x) / (canvasSizePx * canvasSizePx) * 100f
                scratchedAreaPercentage = scratchedArea.coerceAtMost(100f)

                delay(30)

            }

        }
    }

    Box(
        contentAlignment = TopCenter
    ) {
        ScratchCanvas(
            overlayImage = overlayImage,
            baseImage = baseImage,
            movedOffset = null,
            onMovedOffset = { _, _ -> },
            currentPath = currentPathState.value.path,
            currentPathThickness = currentPathState.value.width,
            modifier = Modifier
                .align(alignment = TopStart)
                .size(300.dp)
        )

        Text(
            text = " ${scratchedAreaPercentage.toInt()}%",
            modifier = Modifier.size(height =  0.dp, width = 0.dp),
        )


    }
}

@Composable
fun ScratchCanvas(
    overlayImage: ImageBitmap,
    baseImage: ImageBitmap,
    movedOffset: Offset?,
    onMovedOffset: (Float, Float) -> Unit,
    currentPath: Path,
    currentPathThickness: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(400.dp)
            .clipToBounds()
            .background(Color(0xFFF7DCA7)) // Added background color F7DCA7
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw the overlay image to fit the entire canvas
        drawImage(
            image = overlayImage,
            dstSize = IntSize(
                canvasWidth.toInt(),
                canvasHeight.toInt()
            )
        )

        // Clip the base image to the current path
        clipPath(path = currentPath) {
            // Calculate a smaller size for the base image
            val baseImageScaleFactor = 0.8f
            val baseImageWidth = (canvasWidth * baseImageScaleFactor).toInt()
            val baseImageHeight = (canvasHeight * baseImageScaleFactor).toInt()

            // Center the base image within the canvas
            val baseImageOffsetX = (canvasWidth - baseImageWidth) / 2
            val baseImageOffsetY = (canvasHeight - baseImageHeight) / 2

            drawImage(
                image = baseImage,
                dstSize = IntSize(baseImageWidth, baseImageHeight),
                dstOffset = IntOffset(
                    x = baseImageOffsetX.toInt(),
                    y = baseImageOffsetY.toInt()
                )
            )
        }
    }
}

data class DraggedPath(
    val path: Path,
    val width: Float = 50f
)

@Preview(showBackground = true)
@Composable
fun ScratchCardScreenPreview() {
    ScratchCardScreen()
}