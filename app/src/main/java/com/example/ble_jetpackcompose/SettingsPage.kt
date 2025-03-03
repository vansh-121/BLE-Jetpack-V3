package com.example.ble_jetpackcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Create a singleton object to manage app-wide theme state
object ThemeManager {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun toggleDarkMode(value: Boolean) {
        _isDarkMode.value = value
    }
}

@Composable
fun ModernSettingsScreen(
    viewModel: AuthViewModel = viewModel(),
    onSignOut: () -> Unit,
    navController: NavHostController
) {
    // Get system dark mode as initial value
    val systemDarkMode = isSystemInDarkTheme()

    // Collect dark mode state from ThemeManager
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    // Remember if we've initialized from system theme
    val initializedFromSystem = remember { mutableStateOf(false) }

    // Initialize from system theme only once
    LaunchedEffect(Unit) {
        if (!initializedFromSystem.value) {
            ThemeManager.toggleDarkMode(systemDarkMode)
            initializedFromSystem.value = true
        }
    }

    // Define colors based on theme
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF2F2F7)
    val cardBackground = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
    val dividerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)
    val iconTint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF)

    val currentUser = viewModel.checkCurrentUser()

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
                            color = textColor
                        )
                    )
                },
                backgroundColor = backgroundColor,
                elevation = 0.dp
            )
        },
        bottomBar = {
            // Add the CustomBottomNavigation with dark mode support
            BottomNavigation(
                navController = navController,
                isDarkMode = isDarkMode
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // User Profile Section with current user info and dark mode
            UserProfileCard(
                cardBackground = cardBackground,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                iconTint = iconTint,
                userName = when {
                    currentUser?.isAnonymous == true -> "Guest User"
                    currentUser != null -> currentUser.email?.substringBefore('@') ?: "User"
                    else -> "Not Signed In"
                },
                userEmail = when {
                    currentUser?.isAnonymous == true -> "Anonymous User"
                    currentUser != null -> currentUser.email ?: ""
                    else -> ""
                },
                onLogout = {
                    viewModel.signOut()
                    onSignOut()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Options with dark mode
            SettingsOptionsList(
                cardBackground = cardBackground,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                dividerColor = dividerColor,
                iconTint = iconTint,
                isDarkMode = isDarkMode,
                onDarkModeToggle = { newValue ->
                    ThemeManager.toggleDarkMode(newValue)
                }
            )
        }
    }
}

@Composable
fun UserProfileCard(
    cardBackground: Color,
    textColor: Color,
    secondaryTextColor: Color,
    iconTint: Color,
    userName: String,
    userEmail: String,
    onLogout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardBackground,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (textColor == Color.White) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = secondaryTextColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userName,
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                Text(
                    text = userEmail,
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.body2.copy(
                        color = secondaryTextColor
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = iconTint
                )
            }
        }
    }
}

@Composable
fun SettingsOptionsList(
    cardBackground: Color,
    textColor: Color,
    secondaryTextColor: Color,
    dividerColor: Color,
    iconTint: Color,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {
    val settingsOptions = listOf(
        SettingsItem(Icons.Outlined.DarkMode, "Dark Mode", SettingsItemType.SWITCH),
        SettingsItem(Icons.Outlined.Language, "Language", SettingsItemType.DETAIL),
        SettingsItem(Icons.AutoMirrored.Outlined.Help, "Help", SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.AccountCircle, "Accounts", SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.Info, "About BLE", SettingsItemType.DETAIL)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardBackground,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            settingsOptions.forEachIndexed { index, item ->
                if (item.title == "Dark Mode") {
                    // Pass the isDarkMode value to the Dark Mode switch item
                    SettingsItemRow(
                        item = item,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        iconTint = iconTint,
                        initialSwitchState = isDarkMode,
                        onSwitchChange = onDarkModeToggle
                    )
                } else {
                    SettingsItemRow(
                        item = item,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        iconTint = iconTint
                    )
                }

                if (index < settingsOptions.size - 1) {
                    Divider(
                        color = dividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    textColor: Color,
    secondaryTextColor: Color,
    iconTint: Color,
    initialSwitchState: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null
) {
    var switchState by remember { mutableStateOf(initialSwitchState) }

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
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Text(
            text = item.title,
            fontFamily = helveticaFont,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Medium,
                color = textColor
            ),
            modifier = Modifier.weight(1f)
        )

        // Right side content based on item type
        when (item.type) {
            SettingsItemType.SWITCH -> {
                Switch(
                    checked = switchState,
                    onCheckedChange = { newValue ->
                        switchState = newValue
                        onSwitchChange?.invoke(newValue)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = iconTint,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = secondaryTextColor.copy(alpha = 0.3f)
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
                        color = secondaryTextColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Navigate",
                    tint = secondaryTextColor
                )
            }
        }
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isDarkMode: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val selectedColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF)

    BottomNavigation(
        modifier = modifier,
        backgroundColor = backgroundColor,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.bluetooth),
                    contentDescription = "Bluetooth",
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRoute == "home_screen") selectedColor else contentColor
                )
            },
            selected = currentRoute == "home_screen",
            onClick = { navController.navigate("home_screen") }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.gamepad),
                    contentDescription = "Gameplay",
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRoute == "game_loading") selectedColor else contentColor
                )
            },
            selected = currentRoute == "game_loading",
            onClick = {
                navController.navigate("game_loading")
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRoute == "settings_screen") selectedColor else contentColor
                )
            },
            selected = currentRoute == "settings_screen",
            onClick = { navController.navigate("settings_screen") }
        )
    }
}

// Data classes for settings remain the same
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
    // Mock NavController for preview
    val navController = rememberNavController()
    ModernSettingsScreen(onSignOut = {}, navController = navController)
}