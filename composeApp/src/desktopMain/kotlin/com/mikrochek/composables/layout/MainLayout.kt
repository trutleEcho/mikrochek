package com.mikrochek.composables.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikrochek.composables.tabs.TabBar
import com.mikrochek.composables.tabs.TabContent
import com.mikrochek.navigation.*
import com.mikrochek.data.UserState
import com.mikrochek.screens.auth.LoginScreen
import com.mikrochek.components.Toast
import com.mikrochek.components.ToastData
import com.mikrochek.components.ToastType
import com.mikrochek.screens.auth.SignupScreen
import com.mikrochek.server.repository.auth.AuthRepository
import com.mikrochek.server.repository.user.UserRepository
import org.koin.core.context.GlobalContext

@Composable
fun MainLayout(
    modifier: Modifier = Modifier
) {
    val authRepository: AuthRepository by GlobalContext.get().inject()
    val userRepository: UserRepository by GlobalContext.get().inject()

    var toast by remember { mutableStateOf<ToastData?>(null) }
    val navController = rememberNavController(startDestination = NavDestination.Login)
    var currentRoute by remember { mutableStateOf("Main Dashboard") }

    fun showToast(message: String, type: ToastType = ToastType.INFO) {
        toast = ToastData(message, type)
    }

    fun handleLogout() {
        UserState.clearUserSession()
        TabManager.clear()
        showToast("Logged out successfully", ToastType.SUCCESS)
        navController.clearBackStackAndNavigate(NavDestination.Login)
    }

    fun handleAuthError() {
        UserState.clearUserSession()
        TabManager.clear()
        showToast("Session expired. Please login again.", ToastType.ERROR)
        navController.clearBackStackAndNavigate(NavDestination.Login)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (navController.currentDestination) {
            NavDestination.Login -> {
                // Login screen with subtle background 
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LoginScreen(
                        authRepository = authRepository,
                        onNavigateToSignup = { navController.navigate(NavDestination.Signup) },
                        onUserAuthenticated = { user ->
                            UserState.setUser(user)
                            TabManager.initialize()
                            showToast("Login successful", ToastType.SUCCESS)
                            navController.clearBackStackAndNavigate(NavDestination.MainDashboard)
                        }
                    )
                }
            }

            NavDestination.Signup -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SignupScreen(
                        authRepository = authRepository,
                        userRepository = userRepository,
                        onSignupSuccess = { navController.clearBackStackAndNavigate(NavDestination.Login) },
                        onNavigateToLogin = { navController.clearBackStackAndNavigate(NavDestination.Login) },
                    )
                }
            }

            else -> {
                // Show authenticated UI with improved layout
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Sidebar with elevation
                        if (UserState.isUserValid()) {
                            Sidebar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    if (!UserState.isUserValid() && route != "Login") {
                                        handleAuthError()
                                        return@Sidebar
                                    }

                                    currentRoute = route
                                    val destination = when (route) {
                                        // Dashboard
                                        "Main Dashboard" -> NavDestination.MainDashboard
                                        
                                        // Quotation Management
                                        "Quotations" -> NavDestination.QuotationsList
                                        "Create Quotation" -> NavDestination.QuotationCreate
                                        
                                        // Product Management
                                        "Products" -> NavDestination.ProductsList
                                        "Create Product" -> NavDestination.ProductCreate
                                        
                                        // Purchase Orders
                                        "Purchase Orders" -> NavDestination.PurchaseOrdersList
                                        "Create PO" -> NavDestination.PurchaseOrderCreate
                                        
                                        // Employee Management
                                        "Employees" -> NavDestination.EmployeesList
                                        "Add Employee" -> NavDestination.EmployeeCreate
                                        
                                        // Payroll
                                        "Payroll Dashboard" -> NavDestination.PayrollDashboard
                                        "Process Payroll" -> NavDestination.PayrollProcessing
                                        
                                        // Workflow Management
                                        "Kanban Board" -> NavDestination.KanbanBoard
                                        
                                        else -> NavDestination.MainDashboard
                                    }

                                    // Update the current tab instead of creating a new one
                                    TabManager.getCurrentTab()?.let { currentTab ->
                                        TabManager.updateTabDestination(currentTab.id, destination)
                                    } ?: run {
                                        // If no tab exists yet, create one
                                        TabManager.addTab(
                                            Tab(
                                                title = getTabTitle(destination),
                                                destination = destination
                                            )
                                        )
                                    }
                                },
                                onLogout = { handleLogout() }
                            )
                        }

                        // Main content area with enhanced styling
                        Surface(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            color = MaterialTheme.colors.background,
                            elevation = 8.dp
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Show tabs only for authenticated users
                                if (UserState.isUserValid()) {
                                    TabBar(
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Content area with subtle background
                                    Surface(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        color = MaterialTheme.colors.background
                                    ) {
                                        // Current tab content
                                        TabManager.getCurrentTab()?.let { currentTab ->
                                            TabContent(
                                                tab = currentTab,
                                                onAuthError = { handleAuthError() },
                                                showToast = ::showToast,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                } else {
                                    // If we get here without being authenticated, redirect to login
                                    LaunchedEffect(Unit) {
                                        handleAuthError()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Toast overlay
        Toast(
            toast = toast,
            onDismiss = { toast = null }
        )
    }
}

private fun getTabTitle(destination: NavDestination): String {
    return when (destination) {
        NavDestination.MainDashboard -> "Main Dashboard"
        NavDestination.ProductsList -> "Products"
        NavDestination.ProductCreate -> "New Product"
        is NavDestination.ProductEdit -> "Edit Product"
        is NavDestination.ProductDetails -> "Product Details"
        NavDestination.PurchaseOrdersList -> "Purchase Orders"
        NavDestination.PurchaseOrderCreate -> "New PO"
        is NavDestination.PurchaseOrderEdit -> "Edit PO"
        is NavDestination.PurchaseOrderDetails -> "PO Details"
        NavDestination.QuotationsList -> "Quotations"
        NavDestination.QuotationCreate -> "New Quotation"
        is NavDestination.QuotationEdit -> "Edit Quotation"
        is NavDestination.QuotationDetails -> "Quotation Details"
        NavDestination.ContactSupport -> "Support"
        else -> "New Tab"
    }
} 