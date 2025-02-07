package com.example.ble_jetpackcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.navigation.NavController
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
@Composable
fun ChartScreen2(navController: NavController, title: String?, value: String?) {
    val actualTitle = title ?: "Unknown Title"
    val actualValue = value ?: "Unknown Value"

    val sensorData = listOf(
        "Bluetooth Sensor 1" to "Advertising type: Bluetooth 5\nData status: Complete\nPrimary PHY: LE 1M",
        "Bluetooth Sensor 2" to "Advertising type: Bluetooth 5\nData status: Complete\nPrimary PHY: LE 1M",
        "Bluetooth Sensor 3" to "Advertising type: Bluetooth 4.2\nData status: Partial\nPrimary PHY: LE 1M",
        "Bluetooth Sensor 4" to "Advertising type: Bluetooth 5\nData status: Complete\nPrimary PHY: LE Coded",
        "Bluetooth Sensor 1" to "Advertising type: Bluetooth 5\nData status: Complete\nPrimary PHY: LE 1M",
        "Bluetooth Sensor 2" to "Advertising type: Bluetooth 5\nData status: Complete\nPrimary PHY: LE 1M",
        "Bluetooth Sensor 3" to "Advertising type: Bluetooth 4.2\nData status: Partial\nPrimary PHY: LE 1M",
    )

    val listState = rememberLazyListState()
    var isSmallSize by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isSmallSize) 0.5f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    // Adjust the scale of the chart dynamically based on the scroll offset
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemScrollOffset > 0) {
            isSmallSize = true
        } else {
            isSmallSize = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(actualTitle, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color.White,
                elevation = 4.dp
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(Color.White, Color.LightGray))
                )
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((200 * scale).dp) // Dynamically adjust height with scale
                    .scale(scale) // Scale the entire container
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                DynamicChart(
                    dataPoints = listOf(10f, 15f, 20f, 25f, 18f, 12f, 5f),
                    modifier = Modifier.fillMaxSize()
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(sensorData) { index, (itemTitle, itemValue) ->
                    RoundedSensorCard(itemTitle, itemValue)
                }
            }
        }

    }
}



@Composable
fun SensorCardWithGraph(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            // Optional: Add any visual component, like a small chart or graph, below this.
        }
    }
}

@Composable
fun RoundedSensorCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DynamicChart(dataPoints: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stepX = size.width / (dataPoints.size - 1)
        val maxY = dataPoints.maxOrNull() ?: 1f
        val minY = dataPoints.minOrNull() ?: maxY - 1f
        val rangeY = if (maxY - minY == 0f) 1f else maxY - minY

        for (i in 0 until dataPoints.size - 1) {
            val x1 = i * stepX
            val y1 = size.height - ((dataPoints[i] - minY) / rangeY) * size.height
            val x2 = (i + 1) * stepX
            val y2 = size.height - ((dataPoints[i + 1] - minY) / rangeY) * size.height

            drawLine(
                color = Color.Blue,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 4f,
                pathEffect = PathEffect.cornerPathEffect(10f)
            )
        }
    }
}