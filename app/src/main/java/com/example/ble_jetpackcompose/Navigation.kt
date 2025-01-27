package com.example.ble_jetpackcompose

import BluetoothScanViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavigation(navController: NavHostController) {
    // Initialize AuthViewModel
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("first_screen") {
                        popUpTo("splash_screen") { inclusive = true }
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
                        popUpTo("login") { inclusive = true }
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
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("game_loading") {
            BLEGamesScreen(navController = navController)
        }


        composable("game_screen") {
            GameActivityScreen()
        }

        composable("settings_screen") {
            ModernSettingsScreen()
        }


        composable("home_screen") {
            val bluetoothViewModel = BluetoothScanViewModel(context = LocalContext.current)
            MainScreen(
                viewModel = authViewModel,
                navController = navController,
                bluetoothViewModel = bluetoothViewModel,
                onSignOut = {
                    navController.navigate("first_screen") {
                        popUpTo("home_screen") { inclusive = true }
                    }
                },
//                goToSettings = {
//                    navController.navigate("settings_screen") {
//                        popUpTo("home_screen") { inclusive = true }
//                    }
//                }
            )
        }
    }
}



//@Composable
//fun HomeScreen(
//    viewModel: AuthViewModel,
//    onSignOut: () -> Unit
//) {
//    val authState by viewModel.authState.collectAsState()
//
//    // Add your home screen UI here
//    // You can access the current user via viewModel.checkCurrentUser()
//
//    // Handle sign out
//    LaunchedEffect(authState) {
//        if (authState is AuthState.Idle) {
//            onSignOut()
//        }
//    }
//}