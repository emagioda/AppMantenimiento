package com.emagioda.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emagioda.myapp.presentation.navigation.AppNavHost
import com.emagioda.myapp.presentation.viewmodel.ThemeViewModel
import com.emagioda.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        val themeVm = ViewModelProvider(
            this,
            ThemeViewModel.Factory(applicationContext)
        )[ThemeViewModel::class.java]
        splash.setKeepOnScreenCondition { !themeVm.isReady.value }

        setContent {
            val isDarkNullable = themeVm.isDark.collectAsStateWithLifecycle().value
            val isDark = isDarkNullable ?: false

            MyAppTheme(useDarkTheme = isDark) {
                AppNavHost(themeVm = themeVm)
            }
        }
    }
}
