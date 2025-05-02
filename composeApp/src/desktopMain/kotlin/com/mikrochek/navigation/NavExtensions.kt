package com.mikrochek.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberNavigator(
    navController: NavController = LocalNavController.current,
    scope: CoroutineScope = rememberCoroutineScope()
): Navigator {
    return remember(navController, scope) {
        Navigator(navController, scope)
    }
}

class Navigator(
    private val navController: NavController,
    private val scope: CoroutineScope
) {
    fun navigate(destination: NavDestination) {
        navController.navigate(destination)
    }

    fun navigateBack() {
        navController.navigateBack()
    }

    fun navigateAndExecute(destination: NavDestination, action: suspend () -> Unit) {
        scope.launch {
            action()
            navController.navigate(destination)
        }
    }

    fun clearBackStackAndNavigate(destination: NavDestination) {
        navController.clearBackStackAndNavigate(destination)
    }

    fun popUpTo(destination: NavDestination, inclusive: Boolean = false) {
        navController.popUpTo(destination, inclusive)
    }
} 