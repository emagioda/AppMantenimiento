package com.emagioda.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.presentation.navigation.AppNavHost
import com.emagioda.myapp.presentation.viewmodel.ThemeViewModel
import com.emagioda.myapp.ui.theme.MyAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeVm: ThemeViewModel =
                viewModel(factory = ThemeViewModel.Factory(applicationContext))

            val isDark by themeVm.isDark.collectAsState()

            MyAppTheme(useDarkTheme = isDark) {
                AppNavHost(themeVm = themeVm)
            }
        }
    }
}
