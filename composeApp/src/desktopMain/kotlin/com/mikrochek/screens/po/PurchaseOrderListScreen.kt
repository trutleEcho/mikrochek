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

@Composable
fun PurchaseOrderListScreen(
    onNavigate: (NavDestination) -> Unit,
    purchaseOrderRepository: PurchaseOrderRepository
) {
    var searchQuery by remember { mutableStateOf("") }
    var purchaseOrders by remember { mutableStateOf<List<PurchaseOrder>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Newest") }
    val filters = listOf("All", "Draft", "Pending", "Approved", "Completed", "Cancelled")
    val sortOptions = listOf("Newest", "Oldest", "Highest Amount", "Lowest Amount")
    var currentDestination by remember { mutableStateOf(NavDestination.PurchaseOrdersList) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery, selectedFilter, sortOrder) {
        isLoading = true
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
        
        isLoading = false
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Content goes directly here without SideBar
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PageHeader(
                    title = "Purchase Orders",
                    subtitle = "Manage and track your purchase orders",
                    actions = {
                        Box(
                            modifier = Modifier.width(180.dp)
                        ){
                            ActionButton(
                                text = "Create PO",
                                icon = Icons.Default.Add,
                                onClick = { onNavigate(NavDestination.PurchaseOrderCreate) }
                            )
                        }
                    }
                )

                // Search and Filter Section
                Section(
                    title = "Search and Filter",
                    collapsible = true,
                    defaultExpanded = true
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search by PO number or vendor...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Filter dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                var expanded by remember { mutableStateOf(false) }
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Status: $selectedFilter")
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

                            // Sort dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                var expanded by remember { mutableStateOf(false) }
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sort: $sortOrder")
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
                    }
                }

                // Purchase Orders List
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Purchase Orders",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${purchaseOrders.size} orders found",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (purchaseOrders.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isEmpty() && selectedFilter == "All")
                                        "No purchase orders found. Create your first one!"
                                    else
                                        "No purchase orders match your search criteria.",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(purchaseOrders) { po ->
                                    PurchaseOrderListItem(
                                        purchaseOrder = po,
                                        onClick = { onNavigate(NavDestination.PurchaseOrderDetails(po.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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

            // PO Icon and Number
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

            // Actions
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