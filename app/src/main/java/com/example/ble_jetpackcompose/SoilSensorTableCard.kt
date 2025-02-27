package com.example.ble_jetpackcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Soil Sensor Data Table",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isReceivingData && timestamps.isNotEmpty()) {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        "Time",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        "Moisture (%)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Temp (°C)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Nitrogen (ppm)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Phosphorus (ppm)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Potassium (ppm)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "EC (µS/cm)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "pH",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Divider()

                // Scrollable table content
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(timestamps.indices.reversed().toList()) { index ->
                        val rowIndex = timestamps.size - 1 - index

                        if (rowIndex >= 0 && rowIndex < timestamps.size) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = timestamps.getOrNull(rowIndex) ?: "--",
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = soilMoistureHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = soilTemperatureHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = soilNitrogenHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = soilPhosphorusHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = soilPotassiumHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = soilEcHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = soilPhHistory.getOrNull(rowIndex)?.toString() ?: "--",
                                        modifier = Modifier.weight(0.8f)
                                    )
                                }

                                Divider(color = Color.LightGray)
                            }
                        }
                    }
                }

                // Current values summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD))
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Text(
                        "Current",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = soilMoistureHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = soilTemperatureHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = soilNitrogenHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = soilPhosphorusHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = soilPotassiumHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = soilEcHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = soilPhHistory.lastOrNull()?.toString() ?: "--",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            } else {
                // Show waiting message if no data is available
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Waiting for soil sensor data...",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Make sure the soil sensor is connected and sending data",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}