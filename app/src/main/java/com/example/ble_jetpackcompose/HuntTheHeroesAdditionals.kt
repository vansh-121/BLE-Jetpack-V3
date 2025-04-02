//package com.example.ble_jetpackcompose
//
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.Fill
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.drawscope.rotate
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.layout.positionInParent
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlin.math.cos
//import kotlin.math.roundToInt
//import kotlin.math.sin
//
//@Composable
//fun RadarScreenWithAllCharacters(
//    modifier: Modifier = Modifier,
//    deviceList: List<Pair<Int, Offset>> = emptyList(),
//    activatedDevices: List<String> = emptyList(),
//    rssiValues: Map<String, Int?> = emptyMap()
//) {
//    val radarColor = colorResource(id = R.color.radar_color)
//    val centerCircleColor = colorResource(id = R.color.radar_color)
//
//    val characters = listOf(
//        R.drawable.iron_man to "Iron_Man",
//        R.drawable.hulk_ to "Hulk",
//        R.drawable.captain_marvel to "Captain Marvel",
//        R.drawable.captain_america to "Captain America",
//        R.drawable.scarlet_witch_ to "Scarlet Witch",
//        R.drawable.black_widow_ to "Black Widow",
//        R.drawable.wasp_ to "Wasp",
//        R.drawable.hela_ to "Hela",
//        R.drawable.spider_man_ to "Spider Man",
//        R.drawable.thor_ to "Thor"
//    )
//
//    val positions = remember { generateSymmetricalPositions(characters.size, 330f) }
//
//    val characterPositions = remember(positions) {
//        characters.zip(positions).map { (imageResId, position) ->
//            imageResId.first to position
//        }
//    }
//
//    Box(
//        modifier = modifier,
//        contentAlignment = Alignment.Center
//    ) {
//        OptimizedRadarLayout(
//            radarColor = radarColor,
//            centerCircleColor = centerCircleColor,
//            deviceList = characterPositions,
//            activatedDevices = activatedDevices,
//            rssiValues = rssiValues
//        )
//    }
//}
//
//@Composable
//fun OptimizedRadarLayout(
//    radarColor: Color,
//    centerCircleColor: Color,
//    deviceList: List<Pair<Int, Offset>> = emptyList(),
//    activatedDevices: List<String> = emptyList(),
//    rssiValues: Map<String, Int?> = emptyMap()
//) {
//    val rememberActivatedDevices = remember(activatedDevices) { activatedDevices }
//    val infiniteTransition = rememberInfiniteTransition(label = "radar_rotation")
//    val rotationAngle by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(4000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "radar_angle"
//    )
//    val blinkAlpha by infiniteTransition.animateFloat(
//        initialValue = 1f,
//        targetValue = 0.3f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(500, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ), label = "blink_alpha"
//    )
//
//    Box(
//        modifier = Modifier
//            .size(340.dp)
//            .clip(CircleShape)
//            .background(Color.Transparent),
//        contentAlignment = Alignment.Center
//    ) {
//        RadarBase(radarColor, centerCircleColor)
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val radius = size.minDimension / 2
//            rotate(degrees = rotationAngle) {
//                drawLine(
//                    color = radarColor,
//                    start = center,
//                    end = center.copy(x = center.x, y = center.y - radius),
//                    strokeWidth = 3.dp.toPx()
//                )
//                val shadowColor = Color.Black.copy(alpha = 0.2f)
//                drawArc(
//                    color = shadowColor,
//                    startAngle = 240f,
//                    sweepAngle = 30f,
//                    useCenter = true,
//                    topLeft = Offset(center.x - radius, center.y - radius),
//                    size = Size(radius * 2, radius * 2),
//                    style = Fill
//                )
//            }
//        }
//
//        deviceList.forEach { (imageResId, position) ->
//            val deviceName = getDeviceNameFromResId(imageResId)
//            val isActivated = rememberActivatedDevices.contains(deviceName)
//            val rssi = rssiValues[deviceName]
//
//            key(imageResId) {
//                DeviceIcon(
//                    imageResId = imageResId,
//                    position = position,
//                    isActivated = isActivated,
//                    blinkAlpha = if (isActivated) blinkAlpha else 1f,
//                    rssi = rssi
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun DeviceIcon(
//    imageResId: Int,
//    position: Offset,
//    isActivated: Boolean,
//    blinkAlpha: Float,
//    rssi: Int?
//) {
//    val localDensity = LocalDensity.current
//    val heroSize = 60.dp
//    val baseModifier = Modifier
//        .size(heroSize)
//        .offset(
//            x = with(localDensity) { position.x.toDp() },
//            y = with(localDensity) { position.y.toDp() }
//        )
//
//    val animatedRssi by animateFloatAsState(
//        targetValue = rssi?.toFloat() ?: -100f,
//        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
//        label = "rssiAnimation"
//    )
//
//    var heroTopY by remember { mutableStateOf(0f) }
//
//    Box(modifier = baseModifier) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            RssiIcon(
//                rssi = rssi,
//                animatedRssi = animatedRssi,
//                modifier = Modifier
//                    .size(18.dp)
//                    .offset {
//                        val rssiHeightPx = with(localDensity) { 18.dp.toPx() }
//                        IntOffset(0, (heroTopY - rssiHeightPx - 2f).toInt())
//                    }
//            )
//
//            Box(
//                modifier = Modifier
//                    .size(heroSize)
//                    .align(Alignment.CenterHorizontally)
//                    .onGloballyPositioned { coordinates ->
//                        heroTopY = coordinates.positionInParent().y
//                    }
//            ) {
//                if (isActivated) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color.White.copy(alpha = 0.3f), shape = CircleShape)
//                    )
//                }
//                val scaleAnimation by animateFloatAsState(
//                    targetValue = if (isActivated) 1.2f else 1f,
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessLow
//                    ),
//                    label = "device_scale"
//                )
//                Image(
//                    painter = painterResource(id = imageResId),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .graphicsLayer {
//                            alpha = blinkAlpha
//                            scaleX = scaleAnimation
//                            scaleY = scaleAnimation
//                        }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun RssiIcon(
//    rssi: Int?,
//    animatedRssi: Float,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier,
//        contentAlignment = Alignment.Center
//    ) {
//        Canvas(modifier = Modifier.matchParentSize()) {
//            drawCircle(
//                color = Color.Red,
//                radius = size.minDimension / 2,
//                center = center
//            )
//        }
//        Text(
//            text = if (rssi != null) "${animatedRssi.roundToInt()}" else "N/A",
//            style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
//            color = Color.White,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.align(Alignment.Center)
//        )
//    }
//}
//
//@Composable
//fun RadarBase(radarColor: Color, centerCircleColor: Color) {
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        val boxSize = size.minDimension
//        val radius = boxSize / 2
//
//        val circles = listOf(
//            radius * 0.33f to 2.dp,
//            radius * 0.66f to 4.dp,
//            radius to 6.dp
//        )
//
//        circles.forEach { (circleRadius, strokeWidth) ->
//            drawCircle(
//                color = radarColor,
//                radius = circleRadius,
//                style = Stroke(width = strokeWidth.toPx())
//            )
//        }
//
//        drawCircle(
//            color = centerCircleColor,
//            radius = radius * 0.06f,
//            alpha = 1f
//        )
//    }
//}
//
//fun generateSymmetricalPositions(count: Int, radius: Float): List<Offset> {
//    val positions = mutableListOf<Offset>()
//    for (i in 0 until count) {
//        val angle = 2 * Math.PI * i / count
//        val x = (radius * cos(angle)).toFloat()
//        val y = (radius * sin(angle)).toFloat()
//        positions.add(Offset(x, y))
//    }
//    return positions
//}
//
//fun generateMultiplePositions(count: Int, radius: Float): List<Offset> {
//    val positions = mutableListOf<Offset>()
//
//    val innerCount = count / 3
//    val middleCount = count / 3
//    val outerCount = count - innerCount - middleCount
//
//    for (i in 0 until innerCount) {
//        val angle = 2 * Math.PI * i / innerCount
//        val x = (radius * 0.33f * cos(angle)).toFloat()
//        val y = (radius * 0.33f * sin(angle)).toFloat()
//        positions.add(Offset(x, y))
//    }
//
//    for (i in 0 until middleCount) {
//        val angle = 2 * Math.PI * i / middleCount
//        val x = (radius * 0.66f * cos(angle)).toFloat()
//        val y = (radius * 0.66f * sin(angle)).toFloat()
//        positions.add(Offset(x, y))
//    }
//
//    for (i in 0 until outerCount) {
//        val angle = 2 * Math.PI * i / outerCount
//        val x = (radius * 0.95f * cos(angle)).toFloat()
//        val y = (radius * 0.95f * sin(angle)).toFloat()
//        positions.add(Offset(x, y))
//    }
//
//    return positions
//}
//
//private fun getDeviceNameFromResId(resId: Int): String {
//    return when (resId) {
//        R.drawable.iron_man -> "Iron_Man"
//        R.drawable.hulk_ -> "Hulk"
//        R.drawable.captain_marvel -> "Captain Marvel"
//        R.drawable.captain_america -> "Captain America"
//        R.drawable.scarlet_witch_ -> "Scarlet Witch"
//        R.drawable.black_widow_ -> "Black Widow"
//        R.drawable.wasp_ -> "Wasp"
//        R.drawable.hela_ -> "Hela"
//        R.drawable.thor_ -> "Thor"
//        R.drawable.spider_man_ -> "Spider Man"
//        else -> ""
//    }
//}