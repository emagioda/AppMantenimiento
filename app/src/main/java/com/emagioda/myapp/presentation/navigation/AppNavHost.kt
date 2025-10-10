package com.emagioda.myapp.presentation.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emagioda.myapp.presentation.screen.contacts.ContactsScreen
import com.emagioda.myapp.presentation.screen.diagnostic.DiagnosticScreen
import com.emagioda.myapp.presentation.screen.home.HomeScreen
import com.emagioda.myapp.presentation.screen.settings.SettingsScreen
import com.emagioda.myapp.presentation.screen.scanner.ScannerScreen

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Scanner : Route("scanner")
    data object Diagnostic : Route("diagnostic/{machineId}") {
        fun createRoute(machineId: String) = "diagnostic/$machineId"
    }
    data object Contacts : Route("contacts")
    data object ContactsTechnicians : Route("contacts/technicians")
    data object ContactsProviders : Route("contacts/providers")
    data object Settings : Route("settings")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Home.route
) {
    val slideSpec: TweenSpec<IntOffset> = tween(durationMillis = 240)

    val slideInLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = slideSpec)
    }
    val slideOutLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = slideSpec)
    }
    val slideInRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = slideSpec)
    }
    val slideOutRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = slideSpec)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // HOME
        composable(
            route = Route.Home.route,
            enterTransition = slideInRight,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            HomeScreen(
                onNavigateToScanner = { navController.navigate(Route.Scanner.route) },
                onNavigateToContacts = { navController.navigate(Route.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        // SCANNER
        composable(
            route = Route.Scanner.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ScannerScreen(
                onScanned = { machineId ->
                    val safe = Uri.encode(machineId)
                    navController.navigate(Route.Diagnostic.createRoute(safe))
                }
            )
        }

        // DIAGNÓSTICO
        composable(
            route = Route.Diagnostic.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) { backStackEntry ->
            val machineId = Uri.decode(backStackEntry.arguments?.getString("machineId") ?: "N/A")
            DiagnosticScreen(
                machineId = machineId,
                onRestartToHome = { navController.popBackStack(Route.Home.route, false) },
                onOpenContacts = { navController.navigate(Route.Contacts.route) },
                onOpenTechnicians = { navController.navigate(Route.ContactsTechnicians.route) },
                onOpenProviders = { navController.navigate(Route.ContactsProviders.route) }
            )
        }

        // CONTACTOS
        composable(
            route = Route.Contacts.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ContactsScreen(onBack = { navController.popBackStack() }, initialTab = 0)
        }

        // CONTACTOS → Técnicos
        composable(
            route = Route.ContactsTechnicians.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ContactsScreen(onBack = { navController.popBackStack() }, initialTab = 0)
        }

        // CONTACTOS → Proveedores
        composable(
            route = Route.ContactsProviders.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ContactsScreen(onBack = { navController.popBackStack() }, initialTab = 1)
        }

        // AJUSTES
        composable(
            route = Route.Settings.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
