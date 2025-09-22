package com.emagioda.myapp.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emagioda.myapp.presentation.screen.contacts.ContactsScreen
import com.emagioda.myapp.presentation.screen.diagnostic.DiagnosticScreen
import com.emagioda.myapp.presentation.screen.home.HomeScreen
import com.emagioda.myapp.presentation.screen.settings.SettingsScreen
import com.emagioda.myapp.presentation.viewmodel.ThemeViewModel
import com.emagioda.myapp.ui.scanner.ScannerScreen

// Rutas type-safe
sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Scanner : Route("scanner")
    data object Diagnostic : Route("diagnostic/{machineId}") {
        fun createRoute(machineId: String) = "diagnostic/$machineId"
    }

    data object Contacts : Route("contacts")
    data object Settings : Route("settings")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Home.route,
    themeVm: ThemeViewModel? = null // opcional para no romper tu firma previa
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToScanner = { navController.navigate(Route.Scanner.route) },
                onNavigateToContacts = { navController.navigate(Route.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        // Escáner QR
        composable(Route.Scanner.route) {
            ScannerScreen(
                onScanned = { machineId ->
                    navController.navigate(Route.Diagnostic.createRoute(machineId))
                }
            )
        }

        // Diagnóstico
        composable(Route.Diagnostic.route) { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: "N/A"
            DiagnosticScreen(
                machineId = machineId,
                onRestartToHome = {
                    navController.popBackStack(Route.Home.route, false)
                },
                onOpenContacts = {
                    navController.navigate(Route.Contacts.route)
                }
            )
        }

        // Contactos
        composable(Route.Contacts.route) {
            ContactsScreen()
        }

        // Ajustes (tema claro/oscuro)
        composable(Route.Settings.route) {
            val vm = themeVm
            if (vm != null) {
                val isDark by vm.isDark.collectAsState()
                SettingsScreen(
                    isDark = isDark,
                    onDarkChanged = { vm.setDark(it) },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("Configuración de tema no disponible (ThemeViewModel no inyectado).")
            }
        }
    }
}