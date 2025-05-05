package com.mikrochek.screens.po

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.ContentCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.PurchaseOrder
import com.mikrochek.server.repository.po.PurchaseOrderRepository
import com.mikrochek.theme.AppColors
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.mikrochek.components.Toast
import com.mikrochek.components.ToastData
import com.mikrochek.components.ToastType
import com.mikrochek.screens.base.LoadingScreen
import com.mikrochek.screens.base.ErrorScreen

@Composable
fun PurchaseOrderListScreen(
    onNavigate: (NavDestination) -> Unit,
    purchaseOrderRepository: PurchaseOrderRepository
) {
    var searchQuery by remember { mutableStateOf("") }
    var purchaseOrders by remember { mutableStateOf<List<PurchaseOrder>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Newest") }
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var toast by remember { mutableStateOf<ToastData?>(null) }

    val filters = listOf("All", "DRAFT", "PENDING", "APPROVED", "COMPLETED", "CANCELLED")
    val sortOptions = listOf("Newest", "Oldest", "Highest Amount", "Lowest Amount")

    LaunchedEffect(searchQuery, selectedFilter, sortOrder) {
        try {
            isLoading = true
            showError = false
            val allOrders = purchaseOrderRepository.getAllPurchaseOrders()
            
            // Apply filters
            val filtered = allOrders.filter { po ->
                (selectedFilter == "All" || po.status == selectedFilter) &&
                (searchQuery.isEmpty() || 
                 po.poNumber.contains(searchQuery, ignoreCase = true) ||
                 po.vendorName.contains(searchQuery, ignoreCase = true))
            }

            // Apply sorting
            purchaseOrders = when (sortOrder) {
                "Newest" -> filtered.sortedByDescending { it.issueDate }
                "Oldest" -> filtered.sortedBy { it.issueDate }
                "Highest Amount" -> filtered.sortedByDescending { it.total }
                "Lowest Amount" -> filtered.sortedBy { it.total }
                else -> filtered
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load purchase orders: ${e.message}"
            showError = true
            toast = ToastData("Failed to load purchase orders", ToastType.ERROR)
        } finally {
            isLoading = false
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                PageHeader(
                    title = "Purchase Orders",
                    subtitle = "${purchaseOrders.size} orders found",
                    actions = {
                       Box(
                           modifier = Modifier.width(260.dp)
                       ){
                           ActionButton(
                               text = "Create New",
                               icon = Icons.Default.Add,
                               onClick = { onNavigate(NavDestination.PurchaseOrderCreate) },
                               description = "create a new Purchase Order."
                           )
                       }
                    }
                )

                // Filters and Search - Always visible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Filter Dropdown
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.width(160.dp)
                        ) {
                            Text(selectedFilter)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Filter"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filters.forEach { filter ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedFilter = filter
                                        expanded = false
                                    }
                                ) {
                                    Text(filter)
                                }
                            }
                        }
                    }

                    // Sort Dropdown
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.width(160.dp)
                        ) {
                            Text(sortOrder)
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sortOptions.forEach { option ->
                                DropdownMenuItem(
                                    onClick = {
                                        sortOrder = option
                                        expanded = false
                                    }
                                ) {
                                    Text(option)
                                }
                            }
                        }
                    }
                }

                // Content area
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isLoading -> {
                            LoadingScreen()
                        }
                        showError -> {
                            ErrorScreen(message = errorMessage)
                        }
                        purchaseOrders.isEmpty() -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "No Orders",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "No purchase orders found",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                                if (selectedFilter != "All" || searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "Try changing your filters or search query",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(purchaseOrders) { po ->
                                    PurchaseOrderListItem(
                                        purchaseOrder = po,
                                        onClick = { onNavigate(NavDestination.PurchaseOrderDetails(po.poNumber)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add toast notification
    Toast(
        toast = toast,
        onDismiss = { toast = null }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PurchaseOrderListItem(
    purchaseOrder: PurchaseOrder,
    onClick: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Surface(
                modifier = Modifier.size(8.dp),
                shape = MaterialTheme.shapes.small,
                color = when (purchaseOrder.status.lowercase()) {
                    "draft" -> AppColors.Gray400
                    "pending" -> AppColors.Warning
                    "approved" -> AppColors.Success
                    "completed" -> AppColors.Primary
                    "cancelled" -> AppColors.Error
                    else -> AppColors.Gray400
                }
            ) { }

            // PO Number and Date
            Column(
                modifier = Modifier.width(120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = purchaseOrder.poNumber,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(purchaseOrder.issueDate),
                        ZoneId.systemDefault()
                    ).format(dateFormatter),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            // Vendor Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = purchaseOrder.vendorName,
                    style = MaterialTheme.typography.subtitle1
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Items",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${purchaseOrder.items.size} items",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Amount and Status
            Column(
                modifier = Modifier.width(160.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currencyFormatter.format(purchaseOrder.total),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    color = when (purchaseOrder.status.lowercase()) {
                        "draft" -> AppColors.Gray200
                        "pending" -> AppColors.WarningLight
                        "approved" -> AppColors.SuccessLight
                        "completed" -> AppColors.PrimaryLight
                        "cancelled" -> AppColors.ErrorLight
                        else -> AppColors.Gray200
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = purchaseOrder.status,
                        style = MaterialTheme.typography.caption,
                        color = when (purchaseOrder.status.lowercase()) {
                            "draft" -> AppColors.Gray700
                            "pending" -> AppColors.Warning
                            "approved" -> AppColors.Success
                            "completed" -> AppColors.Primary
                            "cancelled" -> AppColors.Error
                            else -> AppColors.Gray700
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // View Details Button
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
} 