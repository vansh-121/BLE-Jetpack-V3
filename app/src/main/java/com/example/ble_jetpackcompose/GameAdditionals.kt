package com.example.ble_jetpackcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
            val buttonWidthPx = dpToPx(200.dp) // Width of the button in pixels
            val targetX = (screenWidth - buttonWidthPx) / 3.5f // Center horizontally
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
            val buttonHeightPx = dpToPx(282.dp) // Height of the button in pixels
            val targetY = (screenHeight - buttonHeightPx) / 2.3f // Center vertically
            targetY - currentY
        } else 0f
    }

    Box {
        Image(
            painter = painterResource(id = if (isExpanded) expandedImageResId else imageResId),
            contentDescription = contentDescription,
            modifier = Modifier
                .width(200.dp)
                .height(280.dp)
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
                    indication = null, // Disable the ripple effect
                    interactionSource = remember { MutableInteractionSource() } // Remove ripple and interaction feedback
                ),
            contentScale = ContentScale.Crop
        )
    }
}


//GameAdditionals


@Composable
fun RadarScreenWithAllCharacters(
    modifier: Modifier = Modifier,
    deviceList: List<Pair<Int, Offset>> = emptyList(),  // List of image resources and positions
    activatedDevices: List<String> = emptyList()  // List of activated device names
) {
    val radarColor = colorResource(id = R.color.radar_color)
    val centerCircleColor = colorResource(id = R.color.radar_color)

    // List of characters and their corresponding image resources
    val characters = listOf(
        R.drawable.iron_man to "Iron_Man",
        R.drawable.hulk_ to "Hulk",
        R.drawable.captain_marvel to "Captain Marvel",
        R.drawable.captain_america to "Captain America",
        R.drawable.scarlet_witch_ to "Scarlet Witch",
        R.drawable.black_widow_ to "Black Widow",
        R.drawable.wasp_ to "Wasp",
        R.drawable.thor_ to "Thor"
    )

    // Generate symmetrical positions for the characters
    val radius = 250f // Radius of the radar circle
    val positions = generateSymmetricalPositions(characters.size, radius)

    // Combine characters with their positions
    val deviceList = characters.zip(positions).map { (imageResId, position) ->
        imageResId.first to position
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        RadarLayoutWithRotatingLine(
            radarColor = radarColor,
            centerCircleColor = centerCircleColor,
            deviceList = deviceList,
            activatedDevices = activatedDevices
        )
    }
}

// Helper function to generate symmetrical positions
fun generateSymmetricalPositions(count: Int, radius: Float): List<Offset> {
    val positions = mutableListOf<Offset>()
    for (i in 0 until count) {
        val angle = 2 * Math.PI * i / count // Calculate the angle for each character
        val x = (radius * cos(angle)).toFloat()
        val y = (radius * sin(angle)).toFloat()
        positions.add(Offset(x, y))
    }
    return positions
}

//@Composable
//fun RadarLayout(
//    radarColor: Color,
//    centerCircleColor: Color,
//    deviceList: List<Pair<Int, Offset>> = emptyList(),
//    activatedDevices: List<String> = emptyList()
//) {
//    // Blinking animation for activated devices
//    val blinkAlpha = remember { Animatable(1f) }
//
//    // Trigger blinking animation when activatedDevices changes
//    LaunchedEffect(activatedDevices) {
//        if (activatedDevices.isNotEmpty()) {
//            while (true) {
//                blinkAlpha.animateTo(0.3f, animationSpec = tween(500))
//                blinkAlpha.animateTo(1f, animationSpec = tween(500))
//            }
//        } else {
//            // Reset alpha if no devices are activated
//            blinkAlpha.snapTo(1f)
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .size(300.dp)
//            .clip(CircleShape)
//            .background(Color.Transparent),
//        contentAlignment = Alignment.Center
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val boxSize = size.minDimension
//            val radius = boxSize / 2
//
//            // Draw rotating line
//            rotate(degrees = 0f) {
//                drawLine(
//                    color = radarColor,
//                    start = center,
//                    end = center.copy(x = center.x, y = center.y - radius),
//                    strokeWidth = 3.dp.toPx()
//                )
//            }
//        }
//
//        // Display all devices organized in circles
//        deviceList.forEach { (imageResId, position) ->
//            // Get the device name from the resource ID
//            val deviceName = getDeviceNameFromResId(imageResId)
//            val isActivated = activatedDevices.contains(deviceName)
//
//            Box(
//                modifier = Modifier
//                    .size(60.dp)
//                    .offset(
//                        x = with(LocalDensity.current) { position.x.toDp() },
//                        y = with(LocalDensity.current) { position.y.toDp() }
//                    )
//            ) {
//                // Device image with conditional blinking
//                Image(
//                    painter = painterResource(id = imageResId),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .graphicsLayer {
//                            alpha = if (isActivated) blinkAlpha.value else 1f
//                            scaleX = if (isActivated) 1.2f else 1f
//                            scaleY = if (isActivated) 1.2f else 1f
//                        }
//                )
//            }
//        }
//    }
//}


@Composable
fun RadarLayoutWithRotatingLine(
    radarColor: Color,
    centerCircleColor: Color,
    deviceList: List<Pair<Int, Offset>> = emptyList(),
    activatedDevices: List<String> = emptyList() // Add parameter for activated devices
) {
    val lineRotation = remember { Animatable(0f) }

    // State for the visibility of devices
    val showDevices = remember { mutableStateOf(false) }

    // State for burst effect
    val burstEffectVisible = remember { mutableStateOf(false) }
    val burstScale = remember { Animatable(0f) }

    // Blinking animation for activated devices
    val blinkAlpha = remember { Animatable(1f) }

    // Infinite rotation animation
    LaunchedEffect(Unit) {
        lineRotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000,  // Slow down the animation
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    // Blinking animation for activated devices
    LaunchedEffect(activatedDevices) {
        if (activatedDevices.isNotEmpty()) {
            while (true) {
                blinkAlpha.animateTo(0.3f, animationSpec = tween(500))
                blinkAlpha.animateTo(1f, animationSpec = tween(500))
            }
        }
    }

    // Show devices after delay
    LaunchedEffect(Unit) {
        delay(1500L)
        burstEffectVisible.value = true
        delay(300)
        showDevices.value = true
        burstScale.animateTo(1f, animationSpec = tween(500))
    }

    Box(
        modifier = Modifier
            .size(300.dp)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxSize = size.minDimension
            val radius = boxSize / 2

            // Draw the radar circles
            val circles = listOf(
                radius * 0.33f to 2.dp,
                radius * 0.66f to 4.dp,
                radius to 6.dp
            )

            circles.forEach { (circleRadius, strokeWidth) ->
                drawCircle(
                    color = radarColor,
                    radius = circleRadius,
                    style = Stroke(width = strokeWidth.toPx())
                )
            }

            // Draw center circle
            drawCircle(
                color = centerCircleColor,
                radius = radius * 0.06f,
                alpha = 1f
            )

            // Draw rotating line
            rotate(degrees = lineRotation.value) {
                drawLine(
                    color = radarColor,
                    start = center,
                    end = center.copy(x = center.x, y = center.y - radius),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Draw radar sector shadow
            val angle = lineRotation.value
            val shadowColor = Color.Black.copy(alpha = 0.2f)

            rotate(degrees = angle) {
                drawArc(
                    color = shadowColor,
                    startAngle = 240f,
                    sweepAngle = 30f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Fill
                )
            }
        }

        // Display all devices with burst effects
        if (showDevices.value) {
            deviceList.forEach { (imageResId, position) ->
                // Get the device name from the resource ID
                val deviceName = getDeviceNameFromResId(imageResId)
                val isActivated = activatedDevices.contains(deviceName)

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(
                            x = with(LocalDensity.current) { position.x.toDp() },
                            y = with(LocalDensity.current) { position.y.toDp() }
                        )
                ) {
                    // Burst effect
                    if (burstEffectVisible.value) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.3f), shape = CircleShape)
                                .scale(burstScale.value)
                                .animateContentSize()
                        )
                    }

                    // Device image with conditional blinking
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = if (isActivated) blinkAlpha.value else 1f
                                    scaleX = if (isActivated) 1.2f else 1f
                                    scaleY = if (isActivated) 1.2f else 1f
                                }
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get device name from resource ID
private fun getDeviceNameFromResId(resId: Int): String {
    return when (resId) {
        R.drawable.iron_man -> "Iron_Man"
        R.drawable.hulk_ -> "Hulk"
        R.drawable.captain_marvel -> "Captain Marvel"
        R.drawable.captain_america -> "Captain America"
        R.drawable.scarlet_witch_ -> "Scarlet Witch"
        R.drawable.black_widow_ -> "Black Widow"
        R.drawable.wasp_ -> "Wasp"
        R.drawable.hela_ -> "Hela"
        R.drawable.thor_ -> "Thor"
        R.drawable.spider_man_ -> "Spider Man"
        else -> ""
    }
}

fun generateMultiplePositions(count: Int, radius: Float): List<Offset> {
    val positions = mutableListOf<Offset>()

    // Number of items in inner and outer circles
    val innerCount = count / 3
    val middleCount = count / 3
    val outerCount = count - innerCount - middleCount

    // Generate positions for inner circle
    for (i in 0 until innerCount) {
        val angle = 2 * Math.PI * i / innerCount
        val x = (radius * 0.33f * cos(angle)).toFloat()
        val y = (radius * 0.33f * sin(angle)).toFloat()
        positions.add(Offset(x, y))
    }

    // Generate positions for middle circle
    for (i in 0 until middleCount) {
        val angle = 2 * Math.PI * i / middleCount
        val x = (radius * 0.66f * cos(angle)).toFloat()
        val y = (radius * 0.66f * sin(angle)).toFloat()
        positions.add(Offset(x, y))
    }

    // Generate positions for outer circle
    for (i in 0 until outerCount) {
        val angle = 2 * Math.PI * i / outerCount
        val x = (radius * 0.95f * cos(angle)).toFloat()
        val y = (radius * 0.95f * sin(angle)).toFloat()
        positions.add(Offset(x, y))
    }

    return positions
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
fun ScratchCardScreen(
    heroName: String,
    modifier: Modifier = Modifier,
    onScratchCompleted: () -> Unit = {}
) {
    val overlayImage = ImageBitmap.imageResource(id = R.drawable.scratch)
    // Select base image based on hero name
    val baseImage = ImageBitmap.imageResource(
        id = when (heroName) {
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
            else -> R.drawable.inner // Default fallback image
        }
    )

    val currentPathState = remember { mutableStateOf(DraggedPath(path = Path(), width = 150f)) }
    var scratchedAreaPercentage by remember { mutableFloatStateOf(0f) }
    var hasCalledCompletion by remember { mutableStateOf(false) }
    val canvasSizePx = with(LocalDensity.current) { 300.dp.toPx() }

    // Animation state for hero reveal
    var showHeroReveal by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (showHeroReveal) 1.2f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "heroRevealScale"
    )

    // Trigger the completion callback when scratch reaches 95%
    LaunchedEffect(scratchedAreaPercentage) {
        if (scratchedAreaPercentage >= 95f && !hasCalledCompletion) {
            hasCalledCompletion = true
            onScratchCompleted()
            showHeroReveal = true // Trigger hero reveal animation
        }
    }

// Modify ScratchCardScreen's LaunchedEffect to be less computationally intensive
    LaunchedEffect(Unit) {
        val stepSize = canvasSizePx / 5  // Increased step size
        var scratchedArea: Float
        val totalArea = canvasSizePx * canvasSizePx

        // Simplified pattern with fewer operations
        for (y in 0..canvasSizePx.toInt() step stepSize.toInt()) {
            val xRange = if (y % (stepSize.toInt() * 2) == 0) {
                (0..canvasSizePx.toInt() step stepSize.toInt())
            } else {
                (canvasSizePx.toInt() downTo 0 step stepSize.toInt())
            }
            for (x in xRange) {
                currentPathState.value.path.addOval(
                    androidx.compose.ui.geometry.Rect(
                        center = Offset(x.toFloat(), y.toFloat()),
                        radius = stepSize * 1.5f  // Larger radius to scratch more with fewer operations
                    )
                )
                scratchedArea = (y * canvasSizePx + x) / totalArea * 100f
                scratchedAreaPercentage = scratchedArea.coerceAtMost(100f)
                delay(50)  // Slightly longer delay
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Scratch Canvas (Visible only while scratching)
        if (scratchedAreaPercentage < 95f) {
            ScratchCanvas(
                overlayImage = overlayImage,
                baseImage = baseImage,
                currentPath = currentPathState.value.path,
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center)
            )
        } else {
            // Reveal Hero Image with Animation
            Image(
                painter = painterResource(
                    id = when (heroName) {
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
                        else -> R.drawable.inner // Default fallback image
                    }
                ),
                contentDescription = "Revealed Hero",
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .align(Alignment.Center)
            )
        }

        // Completion Indicator
        AnimatedVisibility(
            visible = scratchedAreaPercentage >= 95f,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "Hero collected!",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(
                        Color(0x99000000),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ScratchCanvas(
    overlayImage: ImageBitmap,
    baseImage: ImageBitmap,
    currentPath: Path,
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

