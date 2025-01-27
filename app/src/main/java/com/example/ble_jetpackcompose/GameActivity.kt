package com.example.ble_jetpackcompose

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
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.colorResource


fun dpToPx(dp: Dp): Float {
    return dp.value
}

@Composable
fun GameActivityScreen() {
    var expandedImage by remember { mutableStateOf<Int?>(null) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

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
                            showPopup = !showPopup // Toggle popup visibility
                        }
                    )
                }


                // Search button overlay
                if (expandedImage == R.drawable.hunt_the_heroes) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (searchX.value + screenWidth / 2 - 50.dp.toPx()).toInt(),  // Subtract half of button size (50dp/2)
                                    (searchY.value + screenHeight / 2 - 180.dp.toPx()).toInt()  // Subtract half of button size (50dp/2)
                                )
                            }
                            .zIndex(7f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search Button",
                            modifier = Modifier.size(50.dp),
                            contentScale = ContentScale.Crop
                        )
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
                        androidx.compose.animation.AnimatedVisibility (
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 150.dp)
                            .zIndex(7f),
                        contentAlignment = Center  // This ensures the radar is centered
                    ) {
                        RadarScreenWithRotatingLine()
                    }
                }


            }
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
    var alpha = transition.animateFloat(
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
        contentAlignment = Center
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

    Box(
        modifier = Modifier
            .size(200.dp) // Outer box size
            .clip(CircleShape)
            .background(Color.Transparent), // Background color for the radar
        contentAlignment = Center
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
    }
}

@Preview(showBackground = true)
@Composable
fun BleGamesScreenPreview() {
    GameActivityScreen()
}