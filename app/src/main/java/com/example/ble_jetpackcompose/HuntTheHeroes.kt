//package com.example.ble_jetpackcompose
//
//import android.app.Activity
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.zIndex
//import kotlinx.coroutines.delay
//
//@Composable
//fun HuntTheHeroesScreen(
//    activity: Activity,
//    bluetoothViewModel: BluetoothScanViewModel<Any>,
//    screenWidth: Float,
//    screenHeight: Float,
//    translatedText: TranslatedGameScreenText,
//    allowedHeroes: List<String>,
//    textColor: Color,
//    secondaryTextColor: Color,
//    overlayColor: Color
//) {
//    var showRadar by remember { mutableStateOf(false) }
//    var showCharacterReveal by remember { mutableStateOf(false) }
//    var detectedCharacters by remember { mutableStateOf<List<String>>(emptyList()) }
//
//    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
//    val nearbyHeroes = bluetoothDevices.filter { it.name in allowedHeroes }
//    val rssiValues = remember(bluetoothDevices) {
//        allowedHeroes.associateWith { heroName ->
//            nearbyHeroes.find { it.name == heroName }?.rssi?.toInt()
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        bluetoothViewModel.setGameMode(BluetoothScanViewModel.GameMode.HUNT_THE_HEROES)
//        delay(100)
//        showRadar = true
//        bluetoothViewModel.startScan(activity)
//    }
//
//    LaunchedEffect(nearbyHeroes) {
//        if (nearbyHeroes.isNotEmpty()) {
//            delay(1000)
//            detectedCharacters = nearbyHeroes.filter { it.rssi.toInt() in -40..0 }.map { it.name }
//            showCharacterReveal = true
//        } else {
//            showCharacterReveal = false
//            detectedCharacters = emptyList()
//        }
//    }
//
//    val heroImages = produceState<Map<String, Int>>(initialValue = emptyMap()) {
//        value = mapOf(
//            "Iron_Man" to R.drawable.iron_man,
//            "Hulk" to R.drawable.hulk_,
//            "Captain Marvel" to R.drawable.captain_marvel,
//            "Captain America" to R.drawable.captain_america,
//            "Scarlet Witch" to R.drawable.scarlet_witch_,
//            "Black Widow" to R.drawable.black_widow_,
//            "Wasp" to R.drawable.wasp_,
//            "Hela" to R.drawable.hela_,
//            "Thor" to R.drawable.thor_,
//            "Spider Man" to R.drawable.spider_man_,
//            "Default" to R.drawable.search
//        )
//    }.value
//
//    val allHeroesDeviceList = remember {
//        val positions = generateMultiplePositions(allowedHeroes.size, 600f)
//        allowedHeroes.mapIndexed { index, heroName ->
//            val imageRes = heroImages[heroName] ?: R.drawable.search
//            Pair(imageRes, positions.getOrElse(index) { Offset.Zero })
//        }
//    }
//
//    if (showRadar) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .zIndex(7f)
//                .offset(y = 50.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            RadarScreenWithAllCharacters(
//                activatedDevices = detectedCharacters,
//                deviceList = allHeroesDeviceList,
//                rssiValues = rssiValues
//            )
//        }
//    }
//
//    if (detectedCharacters.isNotEmpty()) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 16.dp)
//                .zIndex(8f),
//            contentAlignment = Alignment.BottomCenter
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier
//                    .background(overlayColor, RoundedCornerShape(8.dp))
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = translatedText.heroesDetected,
//                    color = textColor
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                detectedCharacters.forEach { heroName ->
//                    Text(
//                        text = heroName,
//                        color = textColor
//                    )
//                }
//            }
//        }
//    }
//}