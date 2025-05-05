package com.mikrochek.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.common.AlertDialog
import com.mikrochek.components.layout.ContentCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.data.UserState
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.Product
import com.mikrochek.server.database.models.ProductCategory
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.theme.AppColors
import com.mikrochek.utils.TimeUtils
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductEditorScreen(
    productRepository: ProductRepository,
    productId: String?,
    onNavigate: (NavDestination) -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var categories by remember { mutableStateOf<List<ProductCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf<NavDestination>(
        if (productId != null) NavDestination.ProductEdit(productId) 
        else NavDestination.ProductsList
    ) }
    
    // Form fields
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("0.00") }
    var costPrice by remember { mutableStateOf("0.00") }
    var tax by remember { mutableStateOf("0.00") }
    var minStock by remember { mutableStateOf("0") }
    var currentStock by remember { mutableStateOf("0") }
    var isActive by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load product and categories
    LaunchedEffect(productId) {
        isLoading = true

        if (productId != null) {
            productRepository.getProductById(productId)?.let {
                product = it
                code = it.code
                name = it.name
                description = it.description
                category = it.category
                unit = it.unit
                sellingPrice = String.format("%.2f", it.sellingPrice)
                costPrice = String.format("%.2f", it.costPrice)
                tax = String.format("%.2f", it.tax)
                minStock = it.minStock.toString()
                currentStock = it.currentStock.toString()
                isActive = it.isActive
            }
        }
        isLoading = false
    }

    // Validation
    val isValid = code.isNotEmpty() && name.isNotEmpty() && category.isNotEmpty() &&
            unit.isNotEmpty() && sellingPrice.toDoubleOrNull() != null &&
            costPrice.toDoubleOrNull() != null && tax.toDoubleOrNull() != null &&
            minStock.toIntOrNull() != null && currentStock.toIntOrNull() != null

    Row(modifier = Modifier.fillMaxSize()) {
        // Content goes directly here without SideBar
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PageHeader(
                    title = if (productId != null) "Edit Product" else "New Product",
                    subtitle = if (productId != null) "Editing $code" else "Create a new product",
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.width(120.dp)){
                                ActionButton(
                                    text = "Cancel",
                                    icon = Icons.Default.Close,
                                    onClick = {
                                        if (hasUnsavedChanges) {
                                            showUnsavedChangesDialog = true
                                        } else {
                                            onNavigate(NavDestination.ProductsList)
                                        }
                                    }
                                )
                            }
                            Box(
                                modifier = Modifier.width(100.dp)
                            ){
                                ActionButton(
                                    onClick = {
                                        scope.launch {
                                            val now = TimeUtils.getCurrentISTTimestamp()
                                            val userId = UserState.currentUser.value?.id ?: "system"

                                            val updatedProduct = Product(
                                                id = product?.id ?: UUID.randomUUID().toString(),
                                                code = code.trim(),
                                                name = name.trim(),
                                                description = description.trim(),
                                                category = category.trim(),
                                                unit = unit.trim(),
                                                sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                                costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                                                tax = tax.toDoubleOrNull() ?: 0.0,
                                                minStock = minStock.toIntOrNull() ?: 0,
                                                currentStock = currentStock.toIntOrNull() ?: 0,
                                                isActive = isActive,
                                                createdAt = product?.createdAt ?: now,
                                                updatedAt = now,
                                                createdBy = product?.createdBy ?: userId,
                                                updatedBy = userId
                                            )

                                            val result = if (productId == null) {
                                                productRepository.createProduct(updatedProduct)
                                            } else {
                                                productRepository.updateProduct(updatedProduct)
                                            }

                                            result.onSuccess {
                                                hasUnsavedChanges = false
                                                onNavigate(NavDestination.ProductsList)
                                            }
                                        }
                                    },
                                    text = "Save",
                                    icon = Icons.Default.Save,
                                )
                            }
                        }
                    }
                )

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = code,
                                        onValueChange = {
                                            code = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Product Code") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = {
                                            name = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Product Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = category,
                                        onValueChange = {
                                            category = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Catagory") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = unit,
                                        onValueChange = {
                                            unit = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Unit") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = description,
                                        onValueChange = {
                                            description = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Description") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                }
                            }

                            // Stock Management Section
                            Section(
                                title = "Stock Management",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = currentStock,
                                        onValueChange = { value ->
                                            if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                                                currentStock = value
                                                hasUnsavedChanges = true
                                            }
                                        },
                                        label = { Text("Current Stock") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = minStock,
                                        onValueChange = { value ->
                                            if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                                                minStock = value
                                                hasUnsavedChanges = true
                                            }
                                        },
                                        label = { Text("Minimum Stock") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Right column - Pricing and settings
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Pricing Section
                            Section(
                                title = "Pricing Information",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = sellingPrice,
                                        onValueChange = { value ->
                                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                sellingPrice = value
                                                hasUnsavedChanges = true
                                            }
                                        },
                                        label = { Text("Selling Price") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = costPrice,
                                        onValueChange = { value ->
                                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                costPrice = value
                                                hasUnsavedChanges = true
                                            }
                                        },
                                        label = { Text("Cost Price") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = tax,
                                        onValueChange = { value ->
                                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                tax = value
                                                hasUnsavedChanges = true
                                            }
                                        },
                                        label = { Text("Tax %") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Show margin preview
                                    val margin = try {
                                        val sp = sellingPrice.toDoubleOrNull() ?: 0.0
                                        val cp = costPrice.toDoubleOrNull() ?: 0.0
                                        ((sp - cp) / cp * 100)
                                    } catch (e: Exception) {
                                        0.0
                                    }
                                    
                                    Text(
                                        text = "Margin: ${String.format("%.1f", margin)}%",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = when {
                                            margin > 0 -> AppColors.Success
                                            margin < 0 -> AppColors.Error
                                            else -> MaterialTheme.colors.onSurface
                                        }
                                    )
                                }
                            }

                            // Settings Section
                            Section(
                                title = "Settings",
                                collapsible = true,
                                defaultExpanded = true
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Product Status")
                                        Switch(
                                            checked = isActive,
                                            onCheckedChange = {
                                                isActive = it
                                                hasUnsavedChanges = true
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = AppColors.Success,
                                                checkedTrackColor = AppColors.Success.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    Text(
                                        text = if (isActive) "Product is active" else "Product is inactive",
                                        style = MaterialTheme.typography.caption,
                                        color = if (isActive)
                                            AppColors.Success else MaterialTheme.colors.error
                                    )
                                }
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
                    categories.forEach { cat ->
                        TextButton(
                            onClick = {
                                category = cat.name
                                hasUnsavedChanges = true
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (cat.name == category)
                                    MaterialTheme.colors.primary
                                else MaterialTheme.colors.onSurface
                            )
                        ) {
                            Text(cat.name)
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

    // Unsaved Changes Dialog
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to leave?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigate(NavDestination.ProductsList)
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }
} 