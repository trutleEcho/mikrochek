package com.mikrochek.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.ContentCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.Product
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.theme.AppColors
import com.mikrochek.components.dialogs.QuickStockUpdateDialog
import java.text.NumberFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductDetailsScreen(
    onNavigate: (NavDestination) -> Unit,
    productRepository: ProductRepository,
    productId: String
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var currentDestination by remember { mutableStateOf(NavDestination.ProductDetails(productId)) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showStockUpdateDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    LaunchedEffect(productId) {
        product = productRepository.getProductById(productId)
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
                title = "Product Details",
                subtitle = product?.code ?: "",
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionButton(
                            text = "Update Stock",
                            icon = Icons.Default.Add,
                            onClick = { showStockUpdateDialog = true }
                        )
                        ActionButton(
                            text = "Edit",
                            icon = Icons.Default.Edit,
                            onClick = { onNavigate(NavDestination.ProductEdit(productId)) }
                        )
                        ActionButton(
                            text = "Delete",
                            icon = Icons.Default.Delete,
                            onClick = { showDeleteConfirmation = true },
                        )
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (product == null) {
                Text(
                    text = "Product not found",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left column - Basic info and stock
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
                                DetailRow("Product Code", product?.code ?: "")
                                DetailRow("Name", product?.name ?: "")
                                DetailRow("Category", product?.category ?: "")
                                DetailRow("Unit", product?.unit ?: "")
                                DetailRow("Description", product?.description ?: "")
                            }
                        }

                        // Stock Information Section
                        Section(
                            title = "Stock Information",
                            collapsible = true,
                            defaultExpanded = true
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DetailRow(
                                    label = "Current Stock",
                                    value = "${product?.currentStock} ${product?.unit}",
                                    valueColor = if (product?.currentStock ?: 0 <= product?.minStock ?: 0)
                                        AppColors.Error else AppColors.Success
                                )
                                DetailRow("Minimum Stock", "${product?.minStock} ${product?.unit}")
                                if (product?.currentStock ?: 0 <= product?.minStock ?: 0) {
                                    Text(
                                        text = "Low stock warning! Current stock is below minimum level.",
                                        style = MaterialTheme.typography.caption,
                                        color = AppColors.Error
                                    )
                                }
                            }
                        }
                    }

                    // Right column - Pricing and metadata
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Pricing Information Section
                        Section(
                            title = "Pricing Information",
                            collapsible = true,
                            defaultExpanded = true
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DetailRow(
                                    "Selling Price",
                                    currencyFormatter.format(product?.sellingPrice ?: 0.0)
                                )
                                DetailRow(
                                    "Cost Price",
                                    currencyFormatter.format(product?.costPrice ?: 0.0)
                                )
                                DetailRow("Tax Rate", "${product?.tax}%")
                                
                                val margin = ((product?.sellingPrice ?: 0.0) - (product?.costPrice ?: 0.0)) /
                                        (product?.costPrice ?: 1.0) * 100
                                DetailRow(
                                    label = "Margin",
                                    value = "${String.format("%.1f", margin)}%",
                                    valueColor = if (margin > 0) AppColors.Success else AppColors.Error
                                )
                            }
                        }

                        // Metadata Section
                        Section(
                            title = "Metadata",
                            collapsible = true,
                            defaultExpanded = true
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DetailRow(
                                    "Status",
                                    if (product?.isActive == true) "Active" else "Inactive",
                                    valueColor = if (product?.isActive == true)
                                        AppColors.Success else AppColors.Error
                                )
                                DetailRow(
                                    "Created",
                                    formatDateTime(product?.createdAt ?: 0)
                                )
                                DetailRow(
                                    "Last Updated",
                                    formatDateTime(product?.updatedAt ?: 0)
                                )
                                DetailRow("Created By", product?.createdBy ?: "")
                                DetailRow("Updated By", product?.updatedBy ?: "")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Are you sure you want to delete this product?")
                    Text(
                        "This action cannot be undone.",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            product?.id?.let { id ->
                                productRepository.deleteProduct(id).onSuccess {
                                    onNavigate(NavDestination.ProductsList)
                                }
                            }
                        }
                        showDeleteConfirmation = false
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

    // Quick Stock Update Dialog
    if (showStockUpdateDialog && product != null) {
        QuickStockUpdateDialog(
            product = product!!,
            onDismiss = { showStockUpdateDialog = false },
            onUpdate = { quantity ->
                scope.launch {
                    productRepository.updateStock(product!!.id, quantity).onSuccess {
                        // Refresh product data
                        product = productRepository.getProductById(product!!.id)
                    }
                }
                showStockUpdateDialog = false
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colors.onSurface
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
            color = valueColor,
            modifier = Modifier.weight(2f)
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    return java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        .format(java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
        )
} 