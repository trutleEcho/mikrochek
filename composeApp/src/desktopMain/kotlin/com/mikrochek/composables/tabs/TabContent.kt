package com.mikrochek.composables.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.hermesys.screens.support.ContactSupportScreen
import com.mikrochek.components.ToastType
import com.mikrochek.navigation.*
import com.mikrochek.screens.base.MainDashboard
import com.mikrochek.screens.employee.EmployeeFormScreen
import com.mikrochek.screens.employee.EmployeeListScreen
import com.mikrochek.screens.payroll.PayrollDashboard
import com.mikrochek.screens.payroll.PayrollProcessingScreen
import com.mikrochek.screens.po.PurchaseOrderDetailsScreen
import com.mikrochek.screens.po.PurchaseOrderEditorScreen
import com.mikrochek.screens.po.PurchaseOrderListScreen
import com.mikrochek.screens.product.ProductDetailsScreen
import com.mikrochek.screens.product.ProductEditorScreen
import com.mikrochek.screens.product.ProductListScreen
import com.mikrochek.screens.quotation.QuotationEditorScreen
import com.mikrochek.screens.quotation.QuotationsScreen
import com.mikrochek.screens.workflow.KanbanBoardScreen
import com.mikrochek.server.repository.employee.EmployeeRepository
import com.mikrochek.server.repository.po.PurchaseOrderRepository
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.server.repository.quotation.QuotationRepository
import com.mikrochek.server.service.DocumentService
import com.mikrochek.server.service.ProductService
import com.mikrochek.data.UserState
import org.koin.core.context.GlobalContext

@Composable
fun TabContent(
    tab: Tab,
    onAuthError: () -> Unit,
    showToast: (String, ToastType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Get repositories
    val productRepository: ProductRepository by GlobalContext.get().inject()
    val purchaseOrderRepository: PurchaseOrderRepository by GlobalContext.get().inject()
    val documentService: DocumentService by GlobalContext.get().inject()
    val quotationRepository: QuotationRepository by GlobalContext.get().inject()
    val productService: ProductService by GlobalContext.get().inject()
    val employeeRepository: EmployeeRepository by GlobalContext.get().inject()

    val tabNavController = rememberNavController(startDestination = tab.destination)

    LaunchedEffect(tab.destination) {
        // Update navigation when tab destination changes
        if (tabNavController.currentDestination != tab.destination) {
            tabNavController.navigate(tab.destination)
        }
    }

    fun handleScreenNavigation(newDest: NavDestination) {
        when {
            newDest == NavDestination.Login -> onAuthError()
            else -> TabManager.updateTabDestination(tab.id, newDest)
        }
    }

    Box(modifier = modifier) {
        NavHost(tabNavController) { destination ->
            when (destination) {
                // Dashboard
                NavDestination.MainDashboard -> MainDashboard(
                    onNavigate = { newDest: NavDestination -> handleScreenNavigation(newDest) }
                )

                // Products
                NavDestination.ProductsList -> ProductListScreen(
                    productRepository = productRepository,
                    onNavigateToEdit = { productId ->
                        if (productId != null) {
                            handleScreenNavigation(NavDestination.ProductEdit(productId))
                        } else {
                            handleScreenNavigation(NavDestination.ProductCreate)
                        }
                    },
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                is NavDestination.ProductDetails -> ProductDetailsScreen(
                    productRepository = productRepository,
                    productId = destination.productId,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                is NavDestination.ProductEdit -> ProductEditorScreen(
                    productRepository = productRepository,
                    productId = destination.productId,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                NavDestination.ProductCreate -> ProductEditorScreen(
                    productRepository = productRepository,
                    productId = null,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                // Purchase Orders
                NavDestination.PurchaseOrdersList -> PurchaseOrderListScreen(
                    purchaseOrderRepository = purchaseOrderRepository,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                is NavDestination.PurchaseOrderDetails -> PurchaseOrderDetailsScreen(
                    purchaseOrderRepository = purchaseOrderRepository,
                    poId = destination.poId,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                is NavDestination.PurchaseOrderEdit -> PurchaseOrderEditorScreen(
                    purchaseOrderRepository = purchaseOrderRepository,
                    productRepository = productRepository,
                    poId = destination.poId,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                NavDestination.PurchaseOrderCreate -> PurchaseOrderEditorScreen(
                    purchaseOrderRepository = purchaseOrderRepository,
                    productRepository = productRepository,
                    poId = null,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                // Quotations
                NavDestination.QuotationsList -> QuotationsScreen(
                    documentService = documentService,
                    onCreateNew = { handleScreenNavigation(NavDestination.QuotationCreate) },
                    onEditQuotation = { quotationId -> 
                        handleScreenNavigation(NavDestination.QuotationEdit(quotationId))
                    },
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                is NavDestination.QuotationEdit -> QuotationEditorScreen(
                    documentService = documentService,
                    quotationId = destination.quotationId,
                    userId = UserState.getCurrentUser()?.id ?: "",
                    onSaved = { 
                        showToast("Quotation updated successfully", ToastType.SUCCESS)
                        tabNavController.navigateBack()
                    },
                    onCancel = { tabNavController.navigateBack() },
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                    productService = productService
                )

                NavDestination.QuotationCreate -> QuotationEditorScreen(
                    documentService = documentService,
                    quotationId = null,
                    userId = UserState.getCurrentUser()?.id ?: "",
                    onSaved = { 
                        showToast("Quotation created successfully", ToastType.SUCCESS)
                        tabNavController.navigateBack()
                    },
                    onCancel = { tabNavController.navigateBack() },
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                    productService = productService
                )

                is NavDestination.QuotationDetails -> QuotationsScreen(
                    documentService = documentService,
                    onCreateNew = { handleScreenNavigation(NavDestination.QuotationCreate) },
                    onEditQuotation = { quotationId -> 
                        handleScreenNavigation(NavDestination.QuotationEdit(quotationId))
                    },
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )
                
                // Employee Management
                NavDestination.EmployeesList -> EmployeeListScreen(
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                )
                
                NavDestination.EmployeeCreate -> EmployeeFormScreen(
                    employeeId = null,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                    showToast = showToast,
                )
                
                is NavDestination.EmployeeEdit -> EmployeeFormScreen(
                    employeeId = destination.employeeId,
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                    showToast = showToast,
                )
                
                is NavDestination.EmployeeDetails -> Text("Employee Details - Coming Soon")
                
                // Payroll Management
                NavDestination.PayrollDashboard -> PayrollDashboard(
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                )
                
                NavDestination.PayrollProcessing -> PayrollProcessingScreen(
                    onNavigate = { newDest -> handleScreenNavigation(newDest) },
                    showToast = showToast,
                )
                
                is NavDestination.PayrollEmployee -> Text("Employee Payroll - Coming Soon")
                
                is NavDestination.PayrollDetails -> Text("Payroll Details - Coming Soon")
                
                // Workflow Management
                NavDestination.KanbanBoard -> KanbanBoardScreen(
                    onNavigate = { newDest -> handleScreenNavigation(newDest) }
                )

                // Support
                NavDestination.ContactSupport -> ContactSupportScreen(
                    errorMessage = "",
                    onRetry = {}
                )
                
                else -> {
                    Text("Screen not implemented: $destination")
                }
            }
        }
    }
} 