package com.example.ble_jetpackcompose

import android.app.Activity
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavigation(navController: NavHostController) {
    // Initialize AuthViewModel
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val activity = context as ComponentActivity
    val bluetoothViewModel: BluetoothScanViewModel<Any?> by activity.viewModels { BluetoothScanViewModelFactory(application)}

    // Check authentication state when the app starts
    LaunchedEffect(Unit) {
        if (authViewModel.isUserAuthenticated()) {
            navController.navigate("home_screen") {
                popUpTo("splash_screen") { inclusive = true }
            }
        }
    }

    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                // Navigate to home screen when authentication is successful
                navController.navigate("home_screen") {
                    popUpTo("first_screen") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                // Handle error if needed
                // You might want to show a toast or error message
            }
            is AuthState.Idle -> {
                // Handle sign out - only navigate if we're not already on first_screen or splash_screen
                if (navController.currentDestination?.route !in listOf("first_screen", "splash_screen")) {
                    navController.navigate("first_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {} // Handle other states if needed
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isUserAuthenticated()) "home_screen" else "splash_screen"
    ) {
        composable("splash_screen") {
            SplashScreen(
                onNavigateToLogin = {
                    // Only navigate if user is not authenticated
                    if (!authViewModel.isUserAuthenticated()) {
                        navController.navigate("first_screen") {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("first_screen") {
            AnimatedFirstScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToSignup = {
                    navController.navigate("register")
                },
                onGuestSignIn = {
                    authViewModel.signInAsGuest()
                }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToHome = {
                    navController.navigate("home_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("game_loading") {
            BLEGamesScreen(navController = navController)
        }

        composable("game_screen") {
            val activity = LocalContext.current as Activity  // ✅ Get current activity
            GameActivityScreen(activity = activity)  // ✅ Pass activity correctly
        }

        composable("settings_screen") {
            ModernSettingsScreen(
                viewModel = authViewModel,
                onSignOut = {
                    navController.navigate("first_screen") {
                        popUpTo("home_screen") { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(
            route = "advertising/{deviceName}/{deviceAddress}/{sensorType}/{deviceId}",
            arguments = listOf(
                navArgument("deviceName") { type = NavType.StringType },
                navArgument("deviceAddress") { type = NavType.StringType },
                navArgument("sensorType") { type = NavType.StringType },
                navArgument("deviceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: ""
            val deviceAddress = backStackEntry.arguments?.getString("deviceAddress") ?: ""
            backStackEntry.arguments?.getString("sensorType") ?: ""
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            // Get the device from the ViewModel using the address
            val viewModel: BluetoothScanViewModel<Any?> = viewModel(factory = BluetoothScanViewModelFactory(application))
            val devices by viewModel.devices.collectAsState()
            devices.find { it.address == deviceAddress }

            AdvertisingDataScreen(
                deviceAddress = deviceAddress,
                deviceName = deviceName,
                navController = navController,
                deviceId = deviceId
            )
        }


        composable(
            route = "chart_screen/{deviceAddress}",
            arguments = listOf(
                navArgument("deviceAddress") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            ChartScreen(
                navController = navController,
                deviceAddress = backStackEntry.arguments?.getString("deviceAddress")
            )
        }


        composable("chart_screen_2/{title}/{value}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title")
            val value = backStackEntry.arguments?.getString("value")

            ChartScreen2(navController = navController, title = title, value = value)
        }


        composable("home_screen") {
//      val bluetoothViewModel = BluetoothScanViewModel(context = LocalContext.current)
            MainScreen(
                navController = navController,
                bluetoothViewModel = bluetoothViewModel
            )
        }
    }
}