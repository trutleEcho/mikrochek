package com.mikrochek.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

data class Tab(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val destination: NavDestination,
    val canClose: Boolean = true
)

object TabManager {
    private var _tabs by mutableStateOf(listOf<Tab>())
    val tabs: List<Tab> get() = _tabs
    
    private var _selectedTabId by mutableStateOf<String?>(null)
    val selectedTabId: String? get() = _selectedTabId

    fun initialize() {
        clear()
        // Create initial dashboard tab
        addTab(
            Tab(
                title = "Main Dashboard",
                destination = NavDestination.MainDashboard,
                canClose = false
            )
        )
    }
    
    fun addTab(tab: Tab) {
        _tabs = _tabs + tab
        selectTab(tab.id)
    }
    
    fun removeTab(id: String) {
        val index = _tabs.indexOfFirst { it.id == id }
        if (index != -1 && _tabs[index].canClose) {
            val remainingTabs = _tabs.filterNot { it.id == id }
            
            // If we're removing the selected tab, select an adjacent tab
            if (_selectedTabId == id) {
                _selectedTabId = remainingTabs.getOrNull(index.coerceAtMost(remainingTabs.size - 1))?.id
                    ?: remainingTabs.lastOrNull()?.id
            }
            
            _tabs = remainingTabs
        }
    }
    
    fun selectTab(id: String) {
        if (_tabs.any { it.id == id }) {
            _selectedTabId = id
        }
    }

    fun getCurrentTab(): Tab? {
        return _tabs.find { it.id == _selectedTabId }
    }

    fun updateTabDestination(tabId: String, destination: NavDestination) {
        val index = _tabs.indexOfFirst { it.id == tabId }
        if (index != -1) {
            _tabs = _tabs.toMutableList().apply {
                this[index] = this[index].copy(
                    destination = destination,
                    title = getTabTitle(destination)
                )
            }
        }
    }
    
    fun clear() {
        _tabs = emptyList()
        _selectedTabId = null
    }

    private fun getTabTitle(destination: NavDestination): String {
        return when (destination) {
            NavDestination.MainDashboard -> "Main Dashboard"
            
            // Product Management
            NavDestination.ProductsList -> "Products"
            NavDestination.ProductCreate -> "New Product"
            is NavDestination.ProductEdit -> "Edit Product"
            is NavDestination.ProductDetails -> "Product Details"
            
            // Purchase Orders
            NavDestination.PurchaseOrdersList -> "Purchase Orders"
            NavDestination.PurchaseOrderCreate -> "New PO"
            is NavDestination.PurchaseOrderEdit -> "Edit PO"
            is NavDestination.PurchaseOrderDetails -> "PO Details"
            
            // Quotation Management
            NavDestination.QuotationsList -> "Quotations"
            NavDestination.QuotationCreate -> "New Quotation"
            is NavDestination.QuotationEdit -> "Edit Quotation"
            is NavDestination.QuotationDetails -> "Quotation Details"
            
            // Employee Management
            NavDestination.EmployeesList -> "Employees"
            NavDestination.EmployeeCreate -> "New Employee"
            is NavDestination.EmployeeEdit -> "Edit Employee"
            is NavDestination.EmployeeDetails -> "Employee Details"
            
            // Payroll Management
            NavDestination.PayrollDashboard -> "Payroll Dashboard"
            NavDestination.PayrollProcessing -> "Process Payroll"
            is NavDestination.PayrollEmployee -> "Employee Payroll"
            is NavDestination.PayrollDetails -> "Payroll Details"
            
            // Workflow Management
            NavDestination.KanbanBoard -> "Kanban Board"
            
            // Support
            NavDestination.ContactSupport -> "Support"
            
            else -> "New Tab"
        }
    }
} 