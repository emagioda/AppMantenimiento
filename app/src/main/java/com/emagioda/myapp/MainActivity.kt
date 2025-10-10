package com.emagioda.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.emagioda.myapp.presentation.navigation.AppNavHost
import com.emagioda.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MyAppTheme {
                AppNavHost()
            }
        }
    }
}
