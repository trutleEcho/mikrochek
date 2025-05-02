package com.mikrochek.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class NavController(
    private val startDestination: NavDestination,
    private val onNavigate: (NavDestination) -> Unit = {}
) {
    var currentDestination by mutableStateOf(startDestination)
        private set

    private val backStack = mutableListOf(startDestination)

    fun navigate(destination: NavDestination) {
        backStack.add(destination)
        currentDestination = destination
        onNavigate(destination)
    }

    fun navigateBack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        currentDestination = backStack.last()
        onNavigate(currentDestination)
        return true
    }

    fun popUpTo(destination: NavDestination, inclusive: Boolean = false) {
        val index = backStack.indexOf(destination)
        if (index != -1) {
            if (inclusive) {
                backStack.subList(index, backStack.size).clear()
                if (backStack.isEmpty()) {
                    backStack.add(startDestination)
                }
            } else {
                backStack.subList(index + 1, backStack.size).clear()
            }
            currentDestination = backStack.last()
            onNavigate(currentDestination)
        }
    }

    fun clearBackStackAndNavigate(destination: NavDestination) {
        backStack.clear()
        backStack.add(destination)
        currentDestination = destination
        onNavigate(destination)
    }
}

@Composable
fun rememberNavController(
    startDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit = {}
): NavController {
    return remember { NavController(startDestination, onNavigate) }
} 