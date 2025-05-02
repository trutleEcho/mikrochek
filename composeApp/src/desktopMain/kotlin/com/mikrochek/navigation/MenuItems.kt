package com.mikrochek.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.mikrochek.server.database.models.User

/** Defines a menu item with unique id, title, destination screen, category, and optional icon. */
data class AppMenuItem(
    val id: String,
    val title: String,
    val screen: NavDestination,
    val category: String,
    val icon: ImageVector,
    val permissions: List<String> = emptyList()
)

// Service Categories with their icons
val categoryIcons = mapOf(
    "Dashboard" to Icons.Filled.Dashboard,
    "Quotation Management" to Icons.Filled.Description,
    "Purchase Orders" to Icons.Filled.ShoppingCart,
    "Product Management" to Icons.Filled.Category,
    "Employee Management" to Icons.Filled.People,
    "Payroll" to Icons.Filled.Payments,
    "Workflow Management" to Icons.Filled.ViewKanban
)

// Permission constants
object Permissions {
    // Product permissions
    const val VIEW_PRODUCTS = "product:view"
    const val CREATE_PRODUCT = "product:create"
    const val UPDATE_PRODUCT = "product:update"
    const val DELETE_PRODUCT = "product:delete"
    const val MANAGE_PRODUCTS = "product:manage"

    // Purchase Order permissions
    const val VIEW_PURCHASE_ORDERS = "po:view"
    const val CREATE_PURCHASE_ORDER = "po:create"
    const val UPDATE_PURCHASE_ORDER = "po:update"
    const val DELETE_PURCHASE_ORDER = "po:delete"
    const val MANAGE_PURCHASE_ORDERS = "po:manage"

    // Quotation permissions
    const val VIEW_QUOTATIONS = "quotation:view"
    const val CREATE_QUOTATION = "quotation:create"
    const val UPDATE_QUOTATION = "quotation:update"
    const val DELETE_QUOTATION = "quotation:delete"
    const val MANAGE_QUOTATIONS = "quotation:manage"

    // Employee permissions
    const val VIEW_EMPLOYEES = "employee:view"
    const val CREATE_EMPLOYEE = "employee:create"
    const val UPDATE_EMPLOYEE = "employee:update"
    const val DELETE_EMPLOYEE = "employee:delete"
    const val MANAGE_EMPLOYEES = "employee:manage"
    
    // Payroll permissions
    const val VIEW_PAYROLL = "payroll:view"
    const val PROCESS_PAYROLL = "payroll:process"
    const val MANAGE_PAYROLL = "payroll:manage"
    
    // Workflow permissions
    const val VIEW_WORKFLOW = "workflow:view"
    const val MANAGE_WORKFLOW = "workflow:manage"

    // System permissions
}

object AppMenuItems {
    private val allItems = listOf(
        // Dashboard
        AppMenuItem(
            "dashboard.main", "Main Dashboard", NavDestination.MainDashboard,
            "Dashboard", Icons.Filled.Dashboard,
            listOf()
        ),

        // Quotation Management
        AppMenuItem(
            "quotation.list", "Quotations", NavDestination.QuotationsList,
            "Quotation Management", Icons.Filled.List,
            listOf(Permissions.VIEW_QUOTATIONS)
        ),
        AppMenuItem(
            "quotation.create", "Create Quotation", NavDestination.QuotationCreate,
            "Quotation Management", Icons.Filled.Add,
            listOf(Permissions.CREATE_QUOTATION)
        ),

        // Product Management
        AppMenuItem(
            "product.list", "Products", NavDestination.ProductsList,
            "Product Management", Icons.Filled.Category,
            listOf(Permissions.VIEW_PRODUCTS)
        ),
        AppMenuItem(
            "product.create", "Create Product", NavDestination.ProductCreate,
            "Product Management", Icons.Filled.Add,
            listOf(Permissions.CREATE_PRODUCT)
        ),

        // Purchase Orders
        AppMenuItem(
            "po.list", "Purchase Orders", NavDestination.PurchaseOrdersList,
            "Purchase Orders", Icons.Filled.ShoppingCart,
            listOf(Permissions.VIEW_PURCHASE_ORDERS)
        ),
        AppMenuItem(
            "po.create", "Create PO", NavDestination.PurchaseOrderCreate,
            "Purchase Orders", Icons.Filled.Add,
            listOf(Permissions.CREATE_PURCHASE_ORDER)
        ),
        
        // Employee Management
        AppMenuItem(
            "employee.list", "Employees", NavDestination.EmployeesList,
            "Employee Management", Icons.Filled.People,
            listOf(Permissions.VIEW_EMPLOYEES)
        ),
        AppMenuItem(
            "employee.create", "Add Employee", NavDestination.EmployeeCreate,
            "Employee Management", Icons.Filled.PersonAdd,
            listOf(Permissions.CREATE_EMPLOYEE)
        ),
        
        // Payroll Management
        AppMenuItem(
            "payroll.dashboard", "Payroll Dashboard", NavDestination.PayrollDashboard,
            "Payroll", Icons.Filled.Payments,
            listOf(Permissions.VIEW_PAYROLL)
        ),
        AppMenuItem(
            "payroll.processing", "Process Payroll", NavDestination.PayrollProcessing,
            "Payroll", Icons.Filled.Payment,
            listOf(Permissions.PROCESS_PAYROLL)
        ),
        
        // Workflow Management
        AppMenuItem(
            "workflow.kanban", "Kanban Board", NavDestination.KanbanBoard,
            "Workflow Management", Icons.Filled.ViewKanban,
            listOf(Permissions.VIEW_WORKFLOW)
        )
    )

    fun getMenuItems(currentUser: User?): List<AppMenuItem> {
        return allItems
    }

    fun getMenuItemsByCategory(currentUser: User?): Map<String, List<AppMenuItem>> {
        return getMenuItems(currentUser).groupBy { it.category }
    }

    fun getRequiredPermissions(menuItemId: String): List<String> {
        return allItems.find { it.id == menuItemId }?.permissions ?: emptyList()
    }
} 