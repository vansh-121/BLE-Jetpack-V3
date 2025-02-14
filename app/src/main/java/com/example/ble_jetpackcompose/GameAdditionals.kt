package com.example.ble_jetpackcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopStart
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
fun ScratchCardScreen() {
    val overlayImage = ImageBitmap.imageResource(id = R.drawable.scratch)
    val baseImage = ImageBitmap.imageResource(id = R.drawable.inner)

    val currentPathState = remember { mutableStateOf(DraggedPath(path = Path(), width = 150f)) }
    var scratchedAreaPercentage by remember { mutableFloatStateOf(0f) }


    val canvasSizePx = with(LocalDensity.current) { 300.dp.toPx() }

    LaunchedEffect(Unit) {
        val stepSize = canvasSizePx / 8
        var scratchedArea: Float
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
            currentPath = currentPathState.value.path,
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
