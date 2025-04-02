//package com.example.ble_jetpackcompose
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clipToBounds
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.drawscope.clipPath
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.imageResource
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.delay
//
//@Composable
//fun ScratchCardScreen(
//    heroName: String,
//    modifier: Modifier = Modifier,
//    onScratchCompleted: () -> Unit = {}
//) {
//    val overlayImage = ImageBitmap.imageResource(id = R.drawable.scratch)
//    val heroImageResId = remember(heroName) {
//        when (heroName) {
//            "Iron_Man" -> R.drawable.iron_man
//            "Hulk" -> R.drawable.hulk_
//            "Captain Marvel" -> R.drawable.captain_marvel
//            "Captain America" -> R.drawable.captain_america
//            "Scarlet Witch" -> R.drawable.scarlet_witch_
//            "Black Widow" -> R.drawable.black_widow_
//            "Wasp" -> R.drawable.wasp_
//            "Hela" -> R.drawable.hela_
//            "Thor" -> R.drawable.thor_
//            "Spider Man" -> R.drawable.spider_man_
//            else -> R.drawable.inner
//        }
//    }
//    val baseImage = ImageBitmap.imageResource(id = heroImageResId)
//    val currentPathState = remember { mutableStateOf(DraggedPath(path = Path(), width = 150f)) }
//    var scratchedAreaPercentage by remember { mutableFloatStateOf(0f) }
//    var hasCalledCompletion by remember { mutableStateOf(false) }
//    val canvasSizePx = with(LocalDensity.current) { 300.dp.toPx() }
//
//    var showHeroReveal by remember { mutableStateOf(false) }
//    val animatedScale by animateFloatAsState(
//        targetValue = if (showHeroReveal) 1.2f else 1f,
//        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
//        label = "heroRevealScale"
//    )
//
//    LaunchedEffect(scratchedAreaPercentage) {
//        if (scratchedAreaPercentage >= 95f && !hasCalledCompletion) {
//            hasCalledCompletion = true
//            onScratchCompleted()
//            showHeroReveal = true
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val stepSize = canvasSizePx / 2
//        val totalArea = canvasSizePx * canvasSizePx
//        val scratchPath = currentPathState.value.path
//
//        for (y in 0..canvasSizePx.toInt() step stepSize.toInt()) {
//            for (x in 0..canvasSizePx.toInt() step stepSize.toInt()) {
//                scratchPath.addOval(
//                    androidx.compose.ui.geometry.Rect(
//                        center = Offset(x.toFloat(), y.toFloat()),
//                        radius = stepSize * 0.8f
//                    )
//                )
//                scratchedAreaPercentage = ((y * canvasSizePx + x) / totalArea * 100f).coerceAtMost(100f)
//                delay(80)
//            }
//        }
//    }
//
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = modifier
//    ) {
//        if (scratchedAreaPercentage < 95f) {
//            OptimizedScratchCanvas(
//                overlayImage = overlayImage,
//                baseImage = baseImage,
//                currentPath = currentPathState.value.path,
//                modifier = Modifier
//                    .size(300.dp)
//                    .align(Alignment.Center)
//            )
//        } else {
//            Image(
//                painter = painterResource(
//                    id = when (heroName) {
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
//                        else -> R.drawable.inner
//                    }
//                ),
//                contentDescription = "Revealed Hero",
//                modifier = Modifier
//                    .size(300.dp)
//                    .graphicsLayer {
//                        scaleX = animatedScale
//                        scaleY = animatedScale
//                    }
//                    .align(Alignment.Center)
//            )
//        }
//
//        AnimatedVisibility(
//            visible = scratchedAreaPercentage >= 95f,
//            enter = fadeIn(),
//            exit = fadeOut()
//        ) {
//            Text(
//                text = "Hero collected!",
//                style = MaterialTheme.typography.titleLarge,
//                color = Color.White,
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 16.dp)
//                    .background(
//                        Color(0x99000000),
//                        shape = RoundedCornerShape(4.dp)
//                    )
//                    .padding(horizontal = 12.dp, vertical = 4.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun OptimizedScratchCanvas(
//    overlayImage: ImageBitmap,
//    baseImage: ImageBitmap,
//    currentPath: Path,
//    modifier: Modifier = Modifier
//) {
//    Canvas(
//        modifier = modifier
//            .clipToBounds()
//            .background(Color(0xFFF7DCA7))
//    ) {
//        val canvasWidth = size.width
//        val canvasHeight = size.height
//
//        drawImage(
//            image = overlayImage,
//            dstSize = IntSize(
//                canvasWidth.toInt(),
//                canvasHeight.toInt()
//            )
//        )
//
//        clipPath(path = currentPath) {
//            val baseImageScaleFactor = 0.8f
//            val baseImageWidth = (canvasWidth * baseImageScaleFactor).toInt()
//            val baseImageHeight = (canvasHeight * baseImageScaleFactor).toInt()
//            val baseImageOffsetX = (canvasWidth - baseImageWidth) / 2
//            val baseImageOffsetY = (canvasHeight - baseImageHeight) / 2
//
//            drawImage(
//                image = baseImage,
//                dstSize = IntSize(baseImageWidth, baseImageHeight),
//                dstOffset = IntOffset(
//                    x = baseImageOffsetX.toInt(),
//                    y = baseImageOffsetY.toInt()
//                )
//            )
//        }
//    }
//}
//
//data class DraggedPath(
//    val path: Path,
//    val width: Float = 50f
//)