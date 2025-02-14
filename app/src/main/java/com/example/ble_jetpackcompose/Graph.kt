package com.example.ble_jetpackcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun ChartScreen(navController: NavController) {
    val sensorData = listOf(
        "Temperature" to "38°C",
        "Calculated lux" to "1349.4 lux",
        "Humidity" to "67.39%",
        "Pressure" to "1013 hPa",
        "Altitude" to "500m",
        "CO2 Levels" to "400 ppm"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chart", fontSize = 25.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Excel Export */ }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export")
                    }
                    IconButton(onClick = { /* Handle Other Action */ }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Options")
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(Color.White, Color.LightGray))
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sensorData) { (title, value) ->
                    SensorCard(
                        title = title,
                        value = value,
                        dataPoints = listOf(38f, 36f, 39f, 37f, 40f),
                        onClick = {
                            // Navigate to ChartScreen2 with title and value
                            navController.navigate("chart_screen_2/$title/$value")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SensorCard(
    title: String,
    value: String,
    dataPoints: List<Float>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            // 📊 Draw Simple Line Graph
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val stepX = size.width / (dataPoints.size - 1)
                val maxY = dataPoints.maxOrNull() ?: 1f
                val minY = dataPoints.minOrNull() ?: 0f
                val rangeY = maxY - minY

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
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewChartScreen() {
//    ChartScreen()
//}