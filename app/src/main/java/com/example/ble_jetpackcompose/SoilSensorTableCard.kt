package com.example.ble_jetpackcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SoilSensorDataTable(
    soilMoistureHistory: List<Float>,
    soilTemperatureHistory: List<Float>,
    soilNitrogenHistory: List<Float>,
    soilPhosphorusHistory: List<Float>,
    soilPotassiumHistory: List<Float>,
    soilEcHistory: List<Float>,
    soilPhHistory: List<Float>,
    timestamps: List<String>,
    isReceivingData: Boolean
) {
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Soil Sensor Data Table",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isReceivingData && timestamps.isNotEmpty()) {
                // *Wrap Table in Horizontal Scrolling*
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState) // Allow horizontal scrolling
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE0E0E0))
                                .padding(vertical = 14.dp, horizontal = 12.dp)
                        ) {
                            listOf(
                                "Time", "Moisture (%)", "Temp (°C)", "Nitrogen (ppm)", "Phosphorus (ppm)",
                                "Potassium (ppm)", "EC (µS/cm)", "pH"
                            ).forEach { title ->
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(120.dp) // Fixed width for better scrolling
                                )
                            }
                        }

                        Divider()

                        // *Scrollable Table Content (Vertical Scroll)*
                        Box(
                            modifier = Modifier
                                .height(500.dp)
                                .fillMaxWidth()
                        ) {
                            LazyColumn {
                                items(timestamps.indices.reversed().toList()) { index ->
                                    val rowIndex = timestamps.size - 1 - index

                                    if (rowIndex in timestamps.indices) {
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                listOf(
                                                    timestamps.getOrNull(rowIndex),
                                                    soilMoistureHistory.getOrNull(rowIndex)?.toString(),
                                                    soilTemperatureHistory.getOrNull(rowIndex)?.toString(),
                                                    soilNitrogenHistory.getOrNull(rowIndex)?.toString(),
                                                    soilPhosphorusHistory.getOrNull(rowIndex)?.toString(),
                                                    soilPotassiumHistory.getOrNull(rowIndex)?.toString(),
                                                    soilEcHistory.getOrNull(rowIndex)?.toString(),
                                                    soilPhHistory.getOrNull(rowIndex)?.toString()
                                                ).forEach { value ->
                                                    Text(
                                                        text = value ?: "--",
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.width(120.dp) // Fixed width
                                                    )
                                                }
                                            }

                                            Divider(color = Color.LightGray)
                                        }
                                    }
                                }
                            }
                        }

                        // *Current Values Summary Row*
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD))
                                .padding(vertical = 14.dp, horizontal = 12.dp)
                        ) {
                            listOf(
                                "Current",
                                soilMoistureHistory.lastOrNull()?.toString(),
                                soilTemperatureHistory.lastOrNull()?.toString(),
                                soilNitrogenHistory.lastOrNull()?.toString(),
                                soilPhosphorusHistory.lastOrNull()?.toString(),
                                soilPotassiumHistory.lastOrNull()?.toString(),
                                soilEcHistory.lastOrNull()?.toString(),
                                soilPhHistory.lastOrNull()?.toString()
                            ).forEach { value ->
                                Text(
                                    text = value ?: "--",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Waiting for soil sensor data...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Make sure the soil sensor is connected and sending data",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}