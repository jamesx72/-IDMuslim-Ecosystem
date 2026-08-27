package com.example.ui

import android.app.Application
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import com.example.ui.screens.DocumentScannerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.viewmodels.EventViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material.icons.filled.AdminPanelSettings

import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.People
import com.example.ui.screens.ForumScreen
import com.example.ui.screens.QAScreen
import com.example.ui.locales.Translations

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Auth : Screen("auth", "Authentification", null)
    object Profile : Screen("profile", "Profil", Icons.Default.Home)
    object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    object Events : Screen("events", "Événements", Icons.Default.Event)
    object Forum : Screen("forum", "Communauté", Icons.Default.People)
    object QA : Screen("qa", "Questions", Icons.AutoMirrored.Filled.Chat)
    object Admin : Screen("admin", "Admin", Icons.Default.AdminPanelSettings)
    object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)
    object CreateEvent : Screen("create_event", "Créer un événement", null)
    object EditProfile : Screen("edit_profile", "Modifier le Profil", null)
    object DocumentScanner : Screen("document_scanner", "Scanner un document", null)
    object EventDetail : Screen("event_detail/{eventId}", "Détails", null) {
        fun createRoute(eventId: Int) = "event_detail/$eventId"
    }
}

val bottomNavItems = listOf(
    Screen.Profile,
    Screen.Scanner,
    Screen.Events,
    Screen.Forum,
    Screen.Admin
)

@Composable
fun IDMuslimApp(startRoute: String = "auth") {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomNav = currentDestination?.route in bottomNavItems.map { it.route }
    val context = LocalContext.current
    val eventViewModel: EventViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    val language by eventViewModel.language.collectAsState()
    val darkThemePref by eventViewModel.darkTheme.collectAsState(initial = "system")
    val isSolarAdaptive by eventViewModel.isSolarAdaptiveTheme.collectAsState()
    val solarState by eventViewModel.solarState.collectAsState()

    val useDarkTheme = if (isSolarAdaptive) {
        when (solarState.phase) {
            com.example.utils.SolarPhase.NIGHT -> true
            com.example.utils.SolarPhase.SUNSET_EVENING -> true
            com.example.utils.SolarPhase.DAWN -> false
            com.example.utils.SolarPhase.DAY -> false
        }
    } else {
        when (darkThemePref) {
            "dark" -> true
            "light" -> false
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }
    }

    val syncConflict by eventViewModel.syncConflict.collectAsState()
    val syncStatusMessage by eventViewModel.syncStatusMessage.collectAsState()

    syncStatusMessage?.let { msg ->
        androidx.compose.runtime.LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            eventViewModel.clearSyncStatusMessage()
        }
    }

    syncConflict?.let { conflict ->
        com.example.ui.components.SyncConflictDialog(
            conflict = conflict,
            onUseLocal = { eventViewModel.resolveConflictUseLocal(it) },
            onUseCloud = { eventViewModel.resolveConflictUseCloud(it) },
            onDismiss = { eventViewModel.dismissConflict() }
        )
    }

    val themeAnimationKey = if (isSolarAdaptive) "${solarState.phase.name}_$useDarkTheme" else "$useDarkTheme"

    Crossfade(
        targetState = themeAnimationKey,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "theme_crossfade_root"
    ) { _ ->
        com.example.ui.theme.IDMuslimTheme(
            darkTheme = useDarkTheme,
            solarAdaptive = isSolarAdaptive,
            solarState = solarState
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomNav) {
                        NavigationBar(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
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
                                    icon = { Icon(screen.icon!!, contentDescription = Translations.get(language, "nav_" + screen.route)) },
                                    label = { Text(Translations.get(language, "nav_" + screen.route)) },
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
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                    exitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                    popExitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) }
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(onSplashFinished = {
                            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                                val activity = context as? androidx.fragment.app.FragmentActivity
                                if (activity != null && com.example.security.BiometricHelper.canAuthenticate(context)) {
                                    com.example.security.BiometricHelper.authenticate(
                                        activity = activity,
                                        title = "IDMuslim",
                                        subtitle = "Authenticate to open",
                                        onSuccess = {
                                            navController.navigate(Screen.Profile.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        },
                                        onError = { error ->
                                            android.widget.Toast.makeText(context, "Authentication canceled.", android.widget.Toast.LENGTH_SHORT).show()
                                            // Fallback to Auth on cancellation or error
                                            navController.navigate(Screen.Auth.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        }
                                    )
                                } else {
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            } else {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        })
                    }
                    composable(Screen.Auth.route) {
                        AuthScreen(language = language, onAuthSuccess = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        })
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            viewModel = eventViewModel,
                            onLogout = {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onNavigateToEditProfile = {
                                navController.navigate(Screen.EditProfile.route)
                            },
                            onNavigateToDocumentScanner = {
                                navController.navigate(Screen.DocumentScanner.route)
                            }
                        )
                    }
                    composable(Screen.EditProfile.route) {
                        com.example.ui.screens.EditProfileScreen(
                            viewModel = eventViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Scanner.route) {
                        ScannerScreen(viewModel = eventViewModel)
                    }
                    composable(Screen.DocumentScanner.route) {
                        DocumentScannerScreen(
                            viewModel = eventViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Forum.route) {
                        ForumScreen(viewModel = eventViewModel)
                    }
                    composable(Screen.QA.route) {
                        QAScreen()
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = eventViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) }
                        )
                    }
                    composable(Screen.Events.route) {
                        EventsScreen(
                            viewModel = eventViewModel,
                            onNavigateToCreate = { navController.navigate(Screen.CreateEvent.route) },
                            onNavigateToDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) }
                        )
                    }
                    composable(Screen.Admin.route) {
                        AdminDashboardScreen(viewModel = eventViewModel)
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
    }
}
