package com.emagioda.myapp.presentation.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.emagioda.myapp.presentation.screen.history.HistoryScreen
import com.emagioda.myapp.presentation.screen.history.MaintenanceCaseDetailScreen
import com.emagioda.myapp.presentation.screen.history.MachineHistoryScreen
import com.emagioda.myapp.presentation.screen.home.HomeScreen
import com.emagioda.myapp.presentation.screen.machine.MachineDetailScreen
import com.emagioda.myapp.presentation.screen.scanner.ScannerScreen

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Scanner : Route("scanner")
    data object MachineDetail : Route("machineDetail/{machineId}") {
        fun createRoute(machineId: String) = "machineDetail/$machineId"
    }
    data object Diagnostic : Route("diagnostic/{machineId}") {
        fun createRoute(machineId: String) = "diagnostic/$machineId"
    }
    data object MachineHistory : Route("machineHistory/{machineId}") {
        fun createRoute(machineId: String) = "machineHistory/$machineId"
    }
    data object History : Route("history") {
        fun createRoute() = route
    }
    data object HistoryDetail : Route("history/{caseId}") {
        fun createRoute(caseId: Long) = "history/$caseId"
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
    val slideSpec: TweenSpec<IntOffset> = tween(durationMillis = 280)
    val fadeInSpec = tween<Float>(durationMillis = 280)
    val fadeOutSpec = tween<Float>(durationMillis = 180)

    val slideInLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = slideSpec
        ) + fadeIn(animationSpec = fadeInSpec)
    }
    val slideOutLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = slideSpec
        ) + fadeOut(animationSpec = fadeOutSpec)
    }
    val slideInRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = slideSpec
        ) + fadeIn(animationSpec = fadeInSpec)
    }
    val slideOutRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = slideSpec
        ) + fadeOut(animationSpec = fadeOutSpec)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        composable(
            route = Route.Home.route,
            enterTransition = slideInRight,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            HomeScreen(
                onNavigateToScanner = { navController.navigate(Route.Scanner.route) },
                onNavigateToHistory = { navController.navigate(Route.History.createRoute()) }
            )
        }

        composable(
            route = Route.Scanner.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ScannerScreen(
                onScanned = { machineId ->
                    navController.navigate(Route.MachineDetail.createRoute(Uri.encode(machineId)))
                }
            )
        }

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
                    navController.navigate(Route.Diagnostic.createRoute(Uri.encode(id)))
                },
                onOpenHistory = { id ->
                    navController.navigate(Route.MachineHistory.createRoute(Uri.encode(id)))
                }
            )
        }

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
                },
                onOpenHistoryCase = { caseId ->
                    navController.navigate(Route.HistoryDetail.createRoute(caseId))
                }
            )
        }

        composable(
            route = Route.MachineHistory.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) { backStackEntry ->
            val machineId = Uri.decode(backStackEntry.arguments?.getString("machineId") ?: "N/A")
            MachineHistoryScreen(
                machineId = machineId,
                onBack = { navController.popBackStack() },
                onOpenCase = { caseId ->
                    navController.navigate(Route.HistoryDetail.createRoute(caseId))
                }
            )
        }

        composable(
            route = Route.History.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenMachine = { machineId ->
                    navController.navigate(Route.MachineHistory.createRoute(Uri.encode(machineId)))
                }
            )
        }

        composable(
            route = Route.HistoryDetail.route,
            arguments = listOf(
                navArgument("caseId") { type = NavType.LongType }
            ),
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getLong("caseId") ?: 0L
            MaintenanceCaseDetailScreen(
                caseId = caseId,
                onBack = { navController.popBackStack() }
            )
        }

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

        composable(
            route = Route.ContactsTechnicians.route,
            enterTransition = slideInLeft,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInRight,
            popExitTransition = slideOutRight
        ) {
            ContactsScreen(onBack = { navController.popBackStack() }, initialTab = 0)
        }

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
