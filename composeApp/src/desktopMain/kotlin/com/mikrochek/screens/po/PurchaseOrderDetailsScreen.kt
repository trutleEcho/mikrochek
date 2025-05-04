package com.mikrochek.screens.po

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.ContentCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.PurchaseOrder
import com.mikrochek.server.database.models.PurchaseOrderItem
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
import com.mikrochek.utils.TimeUtils

@Composable
fun PurchaseOrderDetailsScreen(
    onNavigate: (NavDestination) -> Unit,
    purchaseOrderRepository: PurchaseOrderRepository,
    poId: String
) {
    var purchaseOrder by remember { mutableStateOf<PurchaseOrder?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<ToastData?>(null) }
    var deletePO by remember { mutableStateOf(false) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    LaunchedEffect(poId, deletePO) {
        try {
            isLoading = true
            showError = false

            if (deletePO) {
                purchaseOrder?.let { po ->
                    try {
                        purchaseOrderRepository.deletePurchaseOrder(po.poNumber)
                        toast = ToastData("Purchase order deleted successfully", ToastType.SUCCESS)
                        onNavigate(NavDestination.PurchaseOrdersList)
                    } catch (e: Exception) {
                        toast = ToastData("Failed to delete purchase order: ${e.message}", ToastType.ERROR)
                        showError = true
                        errorMessage = "Failed to delete purchase order: ${e.message}"
                    }
                    deletePO = false
                    return@LaunchedEffect
                }
            }

            val loadedPO = purchaseOrderRepository.getPurchaseOrderById(poId)
            if (loadedPO == null) {
                errorMessage = "Purchase order not found"
                showError = true
                toast = ToastData("Purchase order not found", ToastType.ERROR)
            } else {
                purchaseOrder = loadedPO
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load purchase order: ${e.message}"
            showError = true
            toast = ToastData("Failed to load purchase order", ToastType.ERROR)
        } finally {
            isLoading = false
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingScreen()
                }
            } else if (showError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorScreen(message = errorMessage)
                }
            } else if (purchaseOrder == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Not Found",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colors.error
                        )
                        Text(
                            text = "Purchase order not found",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.error
                        )
                        Button(
                            onClick = { onNavigate(NavDestination.PurchaseOrdersList) }
                        ) {
                            Text("Back to List")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PageHeader(
                        title = "Purchase Order Details",
                        subtitle = purchaseOrder!!.poNumber,
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
//                                ActionButton(
//                                    text = "Print",
//                                    icon = Icons.Default.Print,
//                                    onClick = { /* TODO: Implement printing */ }
//                                )
                                Box(
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    ActionButton(
                                        text = "Edit",
                                        icon = Icons.Default.Edit,
                                        onClick = { onNavigate(NavDestination.PurchaseOrderEdit(poId)) }
                                    )
                                }
                                Box(
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    ActionButton(
                                        text = "Delete",
                                        icon = Icons.Default.Delete,
                                        onClick = { showDeleteConfirmation = true }
                                    )
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Column - PO Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Basic Information Section
                            Section(
                                title = "Basic Information",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    DetailRow("PO Number", purchaseOrder!!.poNumber)
                                    DetailRow("Vendor Name", purchaseOrder!!.vendorName)
                                    DetailRow("Vendor Address", purchaseOrder!!.vendorAddress)
                                    DetailRow("Vendor Contact", purchaseOrder!!.vendorContact)
                                    DetailRow(
                                        "Created Date",
                                        LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(purchaseOrder!!.issueDate),
                                            ZoneId.systemDefault()
                                        ).format(dateFormatter)
                                    )
                                    DetailRow(
                                        "Delivery Date",
                                        TimeUtils.formatTime(purchaseOrder!!.deliveryDate ?: 0L)
                                    )
                                    DetailRow("Status", purchaseOrder!!.status)
                                }
                            }

                            // Terms and Notes Section
                            Section(
                                title = "Terms and Notes",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    DetailRow("Terms and Conditions", purchaseOrder!!.terms)
                                    DetailRow("Additional Notes", purchaseOrder!!.notes)
                                }
                            }

                            // Audit Information Section
                            Section(
                                title = "Audit Information",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    DetailRow(
                                        "Created At",
                                        LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(purchaseOrder!!.issueDate),
                                            ZoneId.systemDefault()
                                        ).format(dateFormatter)
                                    )

                                    DetailRow("Created By", purchaseOrder!!.issueDate.toString())
                                }
                            }
                        }

                        // Right Column - Items
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Section(
                                title = "Items",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(purchaseOrder!!.items) { item ->
                                        PurchaseOrderItemDetailRow(item = item)
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 16.dp))

                                // Summary
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SummaryRow(
                                        label = "Subtotal",
                                        value = currencyFormatter.format(purchaseOrder!!.subtotal)
                                    )
                                    SummaryRow(
                                        label = "Tax",
                                        value = currencyFormatter.format(purchaseOrder!!.tax)
                                    )
                                    Divider()
                                    SummaryRow(
                                        label = "Total",
                                        value = currencyFormatter.format(purchaseOrder!!.total),
                                        isTotal = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Purchase Order") },
            text = { Text("Are you sure you want to delete this purchase order? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletePO = true
                        showDeleteConfirmation = false
                        toast = ToastData("Purchase order deleted successfully", ToastType.SUCCESS)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add toast notification
    Toast(
        toast = toast,
        onDismiss = { toast = null }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun PurchaseOrderItemDetailRow(
    item: PurchaseOrderItem
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 0.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.product.description,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Quantity
            Text(
                text = item.quantity.toString(),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.width(80.dp)
            )

            // Unit Price
            Text(
                text = currencyFormatter.format(item.unitPrice),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.width(100.dp)
            )

            // Total
            Text(
                text = currencyFormatter.format(item.total),
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) {
                MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
            } else {
                MaterialTheme.typography.body1
            }
        )
        Text(
            text = value,
            style = if (isTotal) {
                MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
            } else {
                MaterialTheme.typography.body1
            }
        )
    }
} 