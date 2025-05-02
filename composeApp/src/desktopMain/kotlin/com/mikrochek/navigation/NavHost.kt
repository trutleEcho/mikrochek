package com.mikrochek.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No NavController provided")
}

@Composable
fun NavHost(
    navController: NavController,
    content: @Composable (NavDestination) -> Unit
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        content(navController.currentDestination)
    }
}

@Composable
fun rememberNavDestination(): NavDestination {
    return LocalNavController.current.currentDestination
}

@Composable
fun NavigationHandler(
    navController: NavController = LocalNavController.current,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        content()
    }
} 