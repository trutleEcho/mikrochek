package com.mikrochek.navigation

sealed class NavDestination(val route: String) {
    // Auth Screens
    data object Login : NavDestination("login")
    data object Signup : NavDestination("signup")
    data object ForgotPassword : NavDestination("forgot-password")

    // Core Screens
    data object MainDashboard : NavDestination("main-dashboard")

    // Product Management
    data object ProductsList : NavDestination("products")
    data object ProductCreate : NavDestination("products/create")
    data class ProductEdit(val productId: String) : NavDestination("products/edit/$productId")
    data class ProductDetails(val productId: String) : NavDestination("products/details/$productId")

    // Quotation Management
    data object QuotationsList : NavDestination("quotations")
    data object QuotationCreate : NavDestination("quotations/create")
    data class QuotationEdit(val quotationId: String) : NavDestination("quotations/edit/$quotationId")
    data class QuotationDetails(val quotationId: String) : NavDestination("quotations/details/$quotationId")

    // Purchase Order Management
    data object PurchaseOrdersList : NavDestination("purchase-orders")
    data object PurchaseOrderCreate : NavDestination("purchase-orders/create")
    data class PurchaseOrderEdit(val poId: String) : NavDestination("purchase-orders/edit/$poId")
    data class PurchaseOrderDetails(val poId: String) : NavDestination("purchase-orders/details/$poId")
    
    // Employee Management
    data object EmployeesList : NavDestination("employees")
    data object EmployeeCreate : NavDestination("employees/create")
    data class EmployeeEdit(val employeeId: String) : NavDestination("employees/edit/$employeeId")
    data class EmployeeDetails(val employeeId: String) : NavDestination("employees/details/$employeeId")
    
    // Payroll Management
    data object PayrollDashboard : NavDestination("payroll")
    data object PayrollProcessing : NavDestination("payroll/processing")
    data class PayrollEmployee(val employeeId: String) : NavDestination("payroll/employee/$employeeId")
    data class PayrollDetails(val payrollId: String) : NavDestination("payroll/details/$payrollId")

    // Workflow Management
    data object KanbanBoard : NavDestination("workflow/kanban")

    // Support
    data object ContactSupport : NavDestination("contact-support")
    data object Loading : NavDestination("loading")
} 