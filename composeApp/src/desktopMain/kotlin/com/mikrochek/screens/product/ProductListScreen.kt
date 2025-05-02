package com.mikrochek.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
fun ProductListScreen(
    productRepository: ProductRepository,
    onNavigateToEdit: (String?) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showLowStock by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showStockUpdateDialog by remember { mutableStateOf<Product?>(null) }
    var currentDestination by remember { mutableStateOf(NavDestination.ProductsList) }
    
    val scope = rememberCoroutineScope()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    // Load products
    LaunchedEffect(searchQuery, selectedCategory, showLowStock) {
        isLoading = true
        products = when {
            searchQuery.isNotEmpty() -> productRepository.searchProducts(searchQuery)
            selectedCategory != null -> productRepository.getProductsByCategory(selectedCategory!!)
            showLowStock -> productRepository.getLowStockProducts()
            else -> productRepository.getAllProducts(isActive = true)
        }
        // Extract unique categories
        categories = products.map { it.category }.distinct()
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
                title = "Products",
                subtitle = "${products.size} products in inventory",
                actions = {
                    Box(
                        modifier = Modifier.width(180.dp)
                    ){
                        ActionButton(
                            icon = Icons.Default.Add,
                            text = "Add Product",
                            onClick = { onNavigateToEdit(null) }
                        )
                    }
                }
            )

            // Filters section
            Section(
                title = "Filters",
                collapsible = true,
                defaultExpanded = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search products...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.weight(1f)
                    )

                    // Category filter
                    OutlinedButton(
                        onClick = { showCategoryDialog = true }
                    ) {
                        Icon(Icons.Default.Category, null)
                        Spacer(Modifier.width(8.dp))
                        Text(selectedCategory ?: "All Categories")
                    }

                    // Low stock filter
                    OutlinedButton(
                        onClick = { showLowStock = !showLowStock },
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = if (showLowStock) AppColors.Warning.copy(alpha = 0.1f)
                            else MaterialTheme.colors.surface
                        )
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = if (showLowStock) AppColors.Warning else MaterialTheme.colors.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Low Stock")
                    }
                }
            }

            // Products list section
            Section(
                title = "Product List",
                collapsible = true,
                defaultExpanded = true
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (products.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                currencyFormatter = currencyFormatter,
                                onEdit = { onNavigateToEdit(product.id) },
                                onQuickStockUpdate = { showStockUpdateDialog = it }
                            )
                            }
                        }
                    }
                }
            }
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add "All Categories" option
                    TextButton(
                        onClick = {
                            selectedCategory = null
                            showCategoryDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("All Categories")
                    }
                    
                    // List all available categories
                    categories.forEach { category ->
                        TextButton(
                            onClick = {
                                selectedCategory = category
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (category == selectedCategory)
                                    MaterialTheme.colors.primary
                                else MaterialTheme.colors.onSurface
                            )
                        ) {
                            Text(category)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Quick Stock Update Dialog
    showStockUpdateDialog?.let { product ->
        QuickStockUpdateDialog(
            product = product,
            onDismiss = { showStockUpdateDialog = null },
            onUpdate = { quantity ->
                scope.launch {
                    productRepository.updateStock(product.id, quantity).onSuccess {
                        // Refresh the products list
                        products = when {
                            searchQuery.isNotEmpty() -> productRepository.searchProducts(searchQuery)
                            selectedCategory != null -> productRepository.getProductsByCategory(selectedCategory!!)
                            showLowStock -> productRepository.getLowStockProducts()
                            else -> productRepository.getAllProducts(isActive = true)
                        }
                    }
                }
                showStockUpdateDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProductCard(
    product: Product,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onQuickStockUpdate: (Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = if (product.currentStock <= product.minStock)
            AppColors.ErrorLight else MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.h6
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.code,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                }
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }

            // Stock and pricing info
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(160.dp)
            ) {
                // Stock status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.currentStock <= product.minStock) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Low stock warning",
                            tint = AppColors.Error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${product.currentStock} ${product.unit}",
                        style = MaterialTheme.typography.body1,
                        color = if (product.currentStock <= product.minStock)
                            AppColors.Error else MaterialTheme.colors.onSurface
                    )
                }
                
                // Price
                Text(
                    text = currencyFormatter.format(product.sellingPrice),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
                
                // Cost and margin
                val margin = ((product.sellingPrice - product.costPrice) / product.costPrice * 100)
                Text(
                    text = "Margin: ${String.format("%.1f", margin)}%",
                    style = MaterialTheme.typography.caption,
                    color = if (margin > 0) AppColors.Success else AppColors.Error
                )
            }

            Spacer(Modifier.width(16.dp))

            // Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit product",
                        tint = MaterialTheme.colors.primary
                    )
                }
                IconButton(
                    onClick = { onQuickStockUpdate(product) }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Update stock",
                        tint = MaterialTheme.colors.secondary
                    )
                }
            }
        }
    }
} 