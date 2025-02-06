package com.example.ble_jetpackcompose

    import android.content.ContentResolver
    import android.provider.Settings
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Button
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.Dp
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.text.style.TextAlign
    import kotlinx.coroutines.delay

    @Composable
    fun AdvertisingDataScreen(

        contentResolver: ContentResolver,
        deviceAddress: String,
        deviceName: String

    ) {
        var lux by remember { mutableStateOf(0f) }
        var temperature by remember { mutableStateOf(0f) }
        var humidity by remember { mutableStateOf(0f) }

        var selectedSetting by remember { mutableStateOf("LUX") }

        val luxText = when (selectedSetting) {
            "LIS2DH" -> "LIS2DH Sensor"
            "Soil" -> "Soil Sensor"
            "Weather" -> "Weather Station"
            "SHT40" -> "SHT40 Sensor"
            else -> "${lux.toInt()} LUX"
        }

        LaunchedEffect(Unit) {
            while (true) {
                val brightness = try {
                    Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                } catch (e: Exception) {
                    0
                }
                lux = brightness * 10f
                temperature = 22f + (Math.random() * 5).toFloat()
                humidity = (Math.random() * 100).toFloat()
                delay(500)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF0A74DA), Color(0xFFADD8E6))))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = "Back Arrow",
                        modifier = Modifier.size(32.dp).clickable { }
                    )
                    Text(
                        text = "Advertising Data",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    Image(
                        painter = painterResource(id = R.drawable.graph),
                        contentDescription = "Graph Icon",
                        modifier = Modifier.size(40.dp).clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.width(365.dp).height(49.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0A8AE6),
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .background(Brush.verticalGradient(colors = listOf(Color(0xFF2A9EE5), Color(0xFF076FB8))))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Device: $deviceName ($deviceAddress)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(start = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DataCard(label = "Device ID:", value = deviceAddress)
                    DataCard(label = "Temperature:", value = "${temperature.toInt()}°C")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(start = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DataCard(label = "LUX:", value = luxText)
                    DataCard(label = "Humidity:", value = "${humidity.toInt()}%")
                }

                Spacer(modifier = Modifier.height(32.dp))



                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier.size(200.dp).background(Color.Transparent, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SunWithRayAnimation(lux = lux)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "DOWNLOAD DATA", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun SunWithRayAnimation(
        lux: Float,
        rayThickness: Dp = 2.dp,
        rayCount: Int = 12,
        maxLux: Float = 255f
    ) {
        // Define lux thresholds for ray growth stages
        val luxThresholds = listOf(200f, 700f, 1500f)

        // Determine the current ray growth stage
        val rayStage = when {
            lux >= luxThresholds[2] -> 3
            lux >= luxThresholds[1] -> 2
            lux >= luxThresholds[0] -> 1
            else -> 0
        }

        // Calculate ray lengths for different stages
        val rayLengths = listOf(
            10.dp,   // Stage 0: Minimal rays
            100.dp,   // Stage 1: Short rays
            150.dp,  // Stage 2: Medium rays
            200.dp   // Stage 3: Long rays
        )

        // Select current ray length based on stage
        val currentRayLength = rayLengths[rayStage]

        // Ray opacity and color based on lux intensity
        val normalizedLux = lux.coerceIn(0f, maxLux) / maxLux
        val rayOpacity = normalizedLux.coerceIn(0.1f, 1f)
        val rayColor = Color(
            red = 1f,
            green = (0.7f + 0.3f * normalizedLux).coerceIn(0.7f, 1f),
            blue = (0.2f * normalizedLux).coerceIn(0f, 0.2f)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            // Render sunrays with stage-based length
            for (i in 0 until rayCount) {
                Box(
                    modifier = Modifier
                        .width(rayThickness)
                        .height(currentRayLength)
                        .graphicsLayer {
                            rotationZ = (i * (360f / rayCount))
                            alpha = rayOpacity
                        }
                        .background(rayColor)
                )
            }

            // Sun image
            Image(
                painter = painterResource(id = R.drawable.sun),
                contentDescription = "Sun",
                modifier = Modifier.size(100.dp)
            )
        }
    }
    @Composable
    fun DataCard(label: String, value: String) {
        Surface(
            modifier = Modifier
                .height(95.dp)
                .width(141.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2A9EE5), Color(0xFF076FB8))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AdvertisingDataScreenPreview() {
        val deviceName = ""
        AdvertisingDataScreen(
            contentResolver = LocalContext.current.contentResolver,
            deviceAddress = TODO(),
            deviceName = deviceName
        )
    }