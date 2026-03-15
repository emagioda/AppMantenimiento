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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emagioda.myapp.presentation.screen.contacts.ContactsScreen
import com.emagioda.myapp.presentation.screen.diagnostic.DiagnosticScreen
import com.emagioda.myapp.presentation.screen.home.HomeScreen
import com.emagioda.myapp.presentation.screen.machine.MachineDetailScreen
import com.emagioda.myapp.presentation.screen.scanner.ScannerScreen

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Scanner : Route("scanner")

    // NUOVA ROTTA: dettaglio macchina
    data object MachineDetail : Route("machineDetail/{machineId}") {
        fun createRoute(machineId: String) = "machineDetail/$machineId"
    }

    data object Diagnostic : Route("diagnostic/{machineId}") {
        fun createRoute(machineId: String) = "diagnostic/$machineId"
    }

    data object Contacts : Route("contacts?providerIds={providerIds}&technicianIds={technicianIds}") {
        fun createRoute(providerIds: String? = null, technicianIds: String? = null): String {
            val safeProviders = providerIds
                ?.takeIf { it.isNotBlank() }
                ?.let(Uri::encode)
                .orEmpty()
            val safeTechnicians = technicianIds
                ?.takeIf { it.isNotBlank() }
                ?.let(Uri::encode)
                .orEmpty()
            return "contacts?providerIds=$safeProviders&technicianIds=$safeTechnicians"
        }
    }
    data object ContactsTechnicians : Route("contacts/technicians")
    data object ContactsProviders : Route("contacts/providers")
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                onNavigateToScanner = { navController.navigate(Route.Scanner.route) }
            )
        }

        // SCANNER → ora naviga a MachineDetail
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
                    navController.navigate(Route.MachineDetail.createRoute(safe))
                }
            )
        }

        // NUOVO: DETTAGLIO MACCHINA
        composable(
            route = Route.MachineDetail.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) { backStackEntry ->
            val machineId = Uri.decode(backStackEntry.arguments?.getString("machineId") ?: "N/A")
            MachineDetailScreen(
                machineId = machineId,
                onBack = { navController.popBackStack() },
                onStartDiagnostic = { id ->
                    val safe = Uri.encode(id)
                    navController.navigate(Route.Diagnostic.createRoute(safe))
                }
            )
        }

        // DIAGNOSTICA (come prima)
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
                onOpenContacts = { navController.navigate(Route.Contacts.createRoute()) },
                onOpenTechnicians = { navController.navigate(Route.ContactsTechnicians.route) },
                onOpenProviders = { navController.navigate(Route.ContactsProviders.route) },
                onOpenFilteredContacts = { providerIds, technicianIds ->
                    navController.navigate(
                        Route.Contacts.createRoute(
                            providerIds = providerIds,
                            technicianIds = technicianIds
                        )
                    )
                }
            )
        }

        // CONTATTI
        composable(
            route = Route.Contacts.route,
            arguments = listOf(
                navArgument("providerIds") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("technicianIds") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) { backStackEntry ->
            val providerIds = backStackEntry.arguments?.getString("providerIds")?.let(Uri::decode)
            val technicianIds = backStackEntry.arguments?.getString("technicianIds")?.let(Uri::decode)

            ContactsScreen(
                onBack = { navController.popBackStack() },
                initialTab = if (technicianIds.isNullOrBlank() && !providerIds.isNullOrBlank()) 1 else 0,
                providerIds = providerIds,
                technicianIds = technicianIds
            )
        }

        // CONTATTI → Tecnici
        composable(
            route = Route.ContactsTechnicians.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ContactsScreen(onBack = { navController.popBackStack() }, initialTab = 0)
        }

        // CONTATTI → Fornitori
        composable(
            route = Route.ContactsProviders.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ContactsScreen(onBack = { navController.popBackStack() }, initialTab = 1)
        }

    }
}
