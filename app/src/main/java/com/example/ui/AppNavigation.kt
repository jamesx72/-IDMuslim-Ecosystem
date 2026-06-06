package com.example.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CreateEventScreen
import com.example.ui.screens.DigitalCardScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodels.EventViewModel
import androidx.lifecycle.ViewModelProvider

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Card : Screen("card", "Carte", Icons.Default.Home)
    object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    object Events : Screen("events", "Événements", Icons.Default.Event)
    object CreateEvent : Screen("create_event", "Créer un événement", null)
    object EventDetail : Screen("event_detail/{eventId}", "Détails", null) {
        fun createRoute(eventId: Int) = "event_detail/$eventId"
    }
}

val bottomNavItems = listOf(
    Screen.Card,
    Screen.Scanner,
    Screen.Events
)

@Composable
fun IDMuslimApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomNav = currentDestination?.route in bottomNavItems.map { it.route }
    val context = LocalContext.current
    val eventViewModel: EventViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                                unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            ),
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onSplashFinished = {
                    navController.navigate(Screen.Card.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Card.route) {
                DigitalCardScreen()
            }
            composable(Screen.Scanner.route) {
                ScannerScreen()
            }
            composable(Screen.Events.route) {
                EventsScreen(
                    viewModel = eventViewModel,
                    onNavigateToCreate = { navController.navigate(Screen.CreateEvent.route) },
                    onNavigateToDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) }
                )
            }
            composable(Screen.CreateEvent.route) {
                CreateEventScreen(
                    viewModel = eventViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
                EventDetailScreen(
                    eventId = eventId,
                    viewModel = eventViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
