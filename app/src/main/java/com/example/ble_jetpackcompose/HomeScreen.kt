package com.example.ble_jetpackcompose

import BluetoothScanViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@Composable
fun MainScreen(
    viewModel: AuthViewModel,
    onSignOut: () -> Unit,
    navController: NavHostController,
    bluetoothViewModel: BluetoothScanViewModel,
//    goToSettings : () -> Unit
) {
    val bluetoothDevices by bluetoothViewModel.devices.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val isPermissionGranted = remember { mutableStateOf(false) }

    BluetoothPermissionHandler(
        onPermissionsGranted = {
            isPermissionGranted.value = true
            bluetoothViewModel.startScan()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "BLE",
                        fontSize = 24.sp,
                        modifier = Modifier.wrapContentSize()
                    )
                },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 4.dp,
                actions = {
                    Button(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colors.error
                        ),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .height(36.dp)
                    ) {
                        Text("Sign Out", color = Color.White)
                    }
                }
            )

            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    // Nearby Devices Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nearby devices (${bluetoothDevices.size})",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    modifier = Modifier
                                        .clickable { bluetoothViewModel.startScan() }
                                        .padding(end = 8.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    modifier = Modifier.clickable { /* More options logic */ }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (bluetoothDevices.isEmpty()) {
                                Text(
                                    text = "No devices found",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                LazyColumn {
                                    items(bluetoothDevices) { device ->
                                        BluetoothDeviceItem(device)
                                        Divider()
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Game Devices Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Game Devices",
                                    style = MaterialTheme.typography.h6
                                )
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    modifier = Modifier.clickable { /* More options logic */ }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(3) {
                                    GameSignalItem()
                                }
                            }
                        }
                    }
                }
            }
            Box{
            // Bottom Navigation
            CustomBottomNavigation(
                modifier = Modifier.align(Alignment.BottomCenter),
                navController = navController
            )
        }
            }
    }
}
    @Composable
    fun BluetoothDeviceItem(device: BLEDevice) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = device.name.toString(),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Address: ${device.address}",
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "Signal Strength: ${device.rssi} dBm",
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }

@Composable
private fun DeviceItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun GameSignalItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.gradient1),
                                colorResource(id = R.color.gradient2)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun CustomBottomNavigation(modifier: Modifier = Modifier,navController: NavHostController) {
    BottomNavigation(
        modifier = modifier,
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.bluetooth),
                    contentDescription = "Bluetooth",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = true,
            onClick = { /* Navigate to Bluetooth */ }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.gamepad),
                    contentDescription = "Gameplay",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = {
                navController.navigate("game_loading")
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = {  navController.navigate("settings_screen") }
        )
    }
}
