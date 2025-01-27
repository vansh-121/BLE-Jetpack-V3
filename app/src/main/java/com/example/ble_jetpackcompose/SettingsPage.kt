package com.example.ble_jetpackcompose

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernSettingsScreen() {
    val backgroundColor = Color(0xFFF2F2F7)
    val cardBackground = Color.White

    Scaffold(
        backgroundColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                },
                backgroundColor = backgroundColor,
                elevation = 0.dp
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // User Profile Section
            UserProfileCard(cardBackground)

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Options
            SettingsOptionsList(cardBackground)
        }
    }
}

@Composable
fun UserProfileCard(backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Profile Image Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "John Doe",
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "johndoe@example.com",
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { /* Logout action */ }) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFF007AFF)
                )
            }
        }
    }
}

@Composable
fun SettingsOptionsList(backgroundColor: Color) {
    val settingsOptions = listOf(
        SettingsItem(Icons.Outlined.DarkMode, "Dark Mode", SettingsItemType.SWITCH),
        SettingsItem(Icons.Outlined.Language, "Language", SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.Help, "Help", SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.AccountCircle, "Accounts", SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.Info, "About BLE", SettingsItemType.DETAIL)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            settingsOptions.forEachIndexed { index, item ->
                SettingsItemRow(item)

                // Divider between items
                if (index < settingsOptions.size - 1) {
                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItem) {
    var switchState by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = Color(0xFF007AFF),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Text(
            text = item.title,
            fontFamily = helveticaFont,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f)
        )

        // Right side content based on item type
        when (item.type) {
            SettingsItemType.SWITCH -> {
                Switch(
                    checked = switchState,
                    onCheckedChange = { switchState = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF007AFF)
                    )
                )
            }
            SettingsItemType.DETAIL -> {
                Text(
                    text = when(item.title) {
                        "Language" -> "English"
                        else -> ""
                    },
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Navigate",
                    tint = Color.Gray
                )
            }
        }
    }
}

// Data classes for settings
data class SettingsItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val type: SettingsItemType
)

enum class SettingsItemType {
    SWITCH,
    DETAIL
}

@Preview(showBackground = true)
@Composable
fun ModernSettingsScreenPreview() {
    ModernSettingsScreen()
}