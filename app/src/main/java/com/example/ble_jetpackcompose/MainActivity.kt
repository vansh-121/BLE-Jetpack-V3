package com.example.ble_jetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        Firebase.initialize(this)
        setContent {
            val navController = rememberNavController()
            AppNavigation(navController)
//                ModernSettingsScreen()

//            SplashScreen(onNavigateToLogin = {})
        }
    }
}