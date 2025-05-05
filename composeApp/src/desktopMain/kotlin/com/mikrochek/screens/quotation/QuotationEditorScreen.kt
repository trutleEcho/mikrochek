package com.mikrochek.screens.quotation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.common.*
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.*
import com.mikrochek.server.repository.quotation.QuotationRepository
import com.mikrochek.server.service.ProductService
import com.mikrochek.theme.AppTheme
import com.mikrochek.utils.TimeUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import com.mikrochek.components.common.DropdownField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.mikrochek.screens.base.LoadingScreen
import com.mikrochek.components.Toast
import com.mikrochek.components.ToastData
import com.mikrochek.components.ToastType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun QuotationEditorScreen(
    quotationRepository: QuotationRepository,
    productService: ProductService,
    quotationId: String? = null,
    userId: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    AppTheme {
        var quotation by remember {
            mutableStateOf(
                Quotation(
                    id = quotationId ?: UUID.randomUUID().toString(),
                    quotationNumber = "QT-${TimeUtils.getCurrentISTTimestamp()}",
                    customerId = "",
                    customerName = "",
                    date = TimeUtils.getCurrentISTTimestamp(),
                    validUntil = TimeUtils.getCurrentISTTimestamp() + ChronoUnit.DAYS.getDuration().toMillis() * 30,
                    items = emptyList(),
                    subtotal = 0.0,
                    discountTotal = 0.0,
                    taxTotal = 0.0,
                    total = 0.0,
                    notes = null,
                    terms = null,
                    status = QuotationStatus.DRAFT,
                    createdAt = TimeUtils.getCurrentISTTimestamp(),
                    updatedAt = TimeUtils.getCurrentISTTimestamp(),
                    createdBy = userId,
                    updatedBy = userId
                )
            )
        }
        
        var isLoading by remember { mutableStateOf(quotationId != null) }
        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var showAddItemDialog by remember { mutableStateOf(false) }
        var hasUnsavedChanges by remember { mutableStateOf(false) }
        var showUnsavedChangesDialog by remember { mutableStateOf(false) }
        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var toast by remember { mutableStateOf<ToastData?>(null) }
        
        val scope = rememberCoroutineScope()
        val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

        // Load products
        LaunchedEffect(Unit) {
            try {
                products = productService.getAllProducts()
            } catch (e: Exception) {
                errorMessage = "Failed to load products: ${e.message}"
                showError = true
                toast = ToastData("Failed to load products", ToastType.ERROR)
            }
        }

        // Load existing quotation if editing
        LaunchedEffect(quotationId) {
            if (quotationId != null) {
                try {
                    val existingQuotation = quotationRepository.getQuotationById(quotationId)
                    if (existingQuotation != null) {
                        quotation = existingQuotation
                    }
                } catch (e: Exception) {
                    errorMessage = "Failed to load quotation: ${e.message}"
                    showError = true
                    toast = ToastData("Failed to load quotation", ToastType.ERROR)
                } finally {
                    isLoading = false
                }
            } else {
                isLoading = false
            }
        }

        // Calculate totals whenever items change
        LaunchedEffect(quotation.items) {
            val subtotal = quotation.items.sumOf { it.subtotal }
            val discountTotal = quotation.items.sumOf { it.discountAmount }
            val taxTotal = quotation.items.sumOf { it.taxAmount }
            val total = quotation.items.sumOf { it.total }
            
            quotation = quotation.copy(
                subtotal = subtotal,
                discountTotal = discountTotal,
                taxTotal = taxTotal,
                total = total,
                updatedAt = TimeUtils.getCurrentISTTimestamp()
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header (non-scrollable)
                    PageHeader(
                        title = if (quotationId != null) "Edit Quotation" else "New Quotation",
                        subtitle = if (quotationId != null) "Editing ${quotation.quotationNumber}" else "Create a new quotation",
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.width(120.dp)) {
                                    ActionButton(
                                        text = "Cancel",
                                        icon = Icons.Default.Close,
                                        onClick = {
                                            if (hasUnsavedChanges) {
                                                showUnsavedChangesDialog = true
                                            } else {
                                                onCancel()
                                            }
                                        }
                                    )
                                }
                                Box(modifier = Modifier.width(100.dp)) {
                                    ActionButton(
                                        text = "Save",
                                        icon = Icons.Default.Save,
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val updatedQuotation = quotation.copy(
                                                        updatedAt = TimeUtils.getCurrentISTTimestamp(),
                                                        updatedBy = userId
                                                    )
                                                    
                                                    val result = if (quotationId == null) {
                                                        quotationRepository.createQuotation(
                                                            updatedQuotation
                                                        )
                                                    } else {
                                                        quotationRepository.updateQuotation(
                                                            updatedQuotation
                                                        )
                                                    }

                                                    result.fold(
                                                        onSuccess = {
                                                            hasUnsavedChanges = false
                                                            toast = ToastData(
                                                                if (quotationId == null) "Quotation created successfully" else "Quotation updated successfully",
                                                                ToastType.SUCCESS
                                                            )
                                                            onSaved()
                                                        },
                                                        onFailure = { e ->
                                                            errorMessage = e.message ?: "Failed to save quotation"
                                                            showError = true
                                                            toast = ToastData("Failed to save quotation", ToastType.ERROR)
                                                        }
                                                    )
                                                } catch (e: Exception) {
                                                    errorMessage = e.message ?: "Unknown error occurred"
                                                    showError = true
                                                    toast = ToastData("Failed to save quotation", ToastType.ERROR)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )

                    // Scrollable content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (isLoading) {
                            LoadingScreen()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Customer Information Section
                                item {
                                    Section(
                                        title = "Customer Information",
                                        collapsible = true,
                                        defaultExpanded = true
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = quotation.customerName,
                                                onValueChange = { 
                                                    quotation = quotation.copy(customerName = it)
                                                    hasUnsavedChanges = true
                                                },
                                                label = { Text("Customer Name") },
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                // Date picker
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "Quotation Date",
                                                        style = MaterialTheme.typography.caption,
                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                    )
                                                    OutlinedTextField(
                                                        value = LocalDate.ofInstant(
                                                            Instant.ofEpochMilli(quotation.date),
                                                            ZoneId.systemDefault()
                                                        ).toString(),
                                                        onValueChange = { dateStr ->
                                                            try {
                                                                val date = LocalDate.parse(dateStr)
                                                                quotation = quotation.copy(
                                                                    date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                                                )
                                                                hasUnsavedChanges = true
                                                            } catch (e: Exception) {
                                                                // Invalid date format
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                // Valid until picker
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "Valid Until",
                                                        style = MaterialTheme.typography.caption,
                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                    )
                                                    OutlinedTextField(
                                                        value = LocalDate.ofInstant(
                                                            Instant.ofEpochMilli(quotation.validUntil),
                                                            ZoneId.systemDefault()
                                                        ).toString(),
                                                        onValueChange = { dateStr ->
                                                            try {
                                                                val date = LocalDate.parse(dateStr)
                                                                quotation = quotation.copy(
                                                                    validUntil = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                                                )
                                                                hasUnsavedChanges = true
                                                            } catch (e: Exception) {
                                                                // Invalid date format
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }

                                            // Status dropdown
                                            DropdownField(
                                                value = quotation.status,
                                                onValueChange = { newStatus ->
                                                    quotation = quotation.copy(status = newStatus)
                                                    hasUnsavedChanges = true
                                                },
                                                items = QuotationStatus.values().toList(),
                                                label = "Status",
                                                itemToString = { it.name }
                                            )
                                        }
                                    }
                                }

                                // Items Section
                                item {
                                    Section(
                                        title = "Items",
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
                                                Text(
                                                    text = "${quotation.items.size} items",
                                                    style = MaterialTheme.typography.subtitle1
                                                )
                                                
                                                Box(
                                                    modifier = Modifier.width(140.dp)
                                                ){
                                                    ActionButton(
                                                        text = "Add Item",
                                                        icon = Icons.Default.Add,
                                                        onClick = { showAddItemDialog = true }
                                                    )
                                                }
                                            }
                                            
                                            if (quotation.items.isEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No items added yet",
                                                        style = MaterialTheme.typography.body1,
                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                            } else {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    quotation.items.forEachIndexed { index, item ->
                                                        QuotationItemCard(
                                                            item = item,
                                                            onDelete = {
                                                                quotation = quotation.copy(
                                                                    items = quotation.items.filterIndexed { i, _ -> i != index }
                                                                )
                                                                hasUnsavedChanges = true
                                                            },
                                                            onEdit = { updatedItem ->
                                                                quotation = quotation.copy(
                                                                    items = quotation.items.mapIndexed { i, item ->
                                                                        if (i == index) updatedItem else item
                                                                    }
                                                                )
                                                                hasUnsavedChanges = true
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            // Summary
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                elevation = 2.dp
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    SummaryRow("Subtotal", quotation.subtotal)
                                                    if (quotation.discountTotal > 0) {
                                                        SummaryRow("Discount", -quotation.discountTotal, MaterialTheme.colors.error)
                                                    }
                                                    SummaryRow("Tax", quotation.taxTotal)
                                                    Divider()
                                                    SummaryRow(
                                                        "Total",
                                                        quotation.total,
                                                        MaterialTheme.colors.primary,
                                                        MaterialTheme.typography.h6
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Terms & Notes Section
                                item {
                                    Section(
                                        title = "Terms & Notes",
                                        collapsible = true,
                                        defaultExpanded = true
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = quotation.terms ?: "",
                                                onValueChange = { 
                                                    quotation = quotation.copy(terms = it.ifEmpty { null })
                                                    hasUnsavedChanges = true
                                                },
                                                label = { Text("Terms & Conditions") },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 3
                                            )

                                            OutlinedTextField(
                                                value = quotation.notes ?: "",
                                                onValueChange = { 
                                                    quotation = quotation.copy(notes = it.ifEmpty { null })
                                                    hasUnsavedChanges = true
                                                },
                                                label = { Text("Notes") },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 2
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

        if (showAddItemDialog) {
            var selectedProduct by remember { mutableStateOf<Product?>(null) }
            var quantity by remember { mutableStateOf(1) }
            var discount by remember { mutableStateOf(0.0) }
            
            AlertDialog(
                onDismissRequest = { showAddItemDialog = false },
                title = { Text("Add Item") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Product Dropdown
                        DropdownField(
                            value = selectedProduct,
                            onValueChange = { selectedProduct = it },
                            items = products,
                            label = "Select Product",
                            itemToString = { it.name }
                        )

                        // Quantity
                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = { newValue -> 
                                if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                    quantity = newValue.toIntOrNull() ?: 1
                                }
                            },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Discount
                        OutlinedTextField(
                            value = discount.toString(),
                            onValueChange = { newValue -> 
                                if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                    discount = newValue.toDoubleOrNull() ?: 0.0
                                }
                            },
                            label = { Text("Discount %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Preview calculations
                        selectedProduct?.let { product ->
                            val subtotal = quantity * product.sellingPrice
                            val discountAmount = subtotal * (discount / 100)
                            val taxAmount = (subtotal - discountAmount) * (product.tax / 100)
                            val total = subtotal - discountAmount + taxAmount

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Preview:",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Bold
                                )
                                SummaryRow("Subtotal:", subtotal)
                                SummaryRow("Discount:", discountAmount)
                                SummaryRow("Tax:", taxAmount)
                                SummaryRow(
                                    "Total:",
                                    total,
                                    style = MaterialTheme.typography.subtitle1,
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedProduct?.let { product ->
                                val subtotal = quantity * product.sellingPrice
                                val discountAmount = subtotal * (discount / 100)
                                val taxAmount = (subtotal - discountAmount) * (product.tax / 100)
                                val total = subtotal - discountAmount + taxAmount

                                val newItem = QuotationItem(
                                    productId = product.id,
                                    productCode = product.code,
                                    productName = product.name,
                                    description = product.description,
                                    quantity = quantity,
                                    unit = product.unit,
                                    unitPrice = product.sellingPrice,
                                    tax = product.tax,
                                    taxAmount = taxAmount,
                                    discount = discount,
                                    discountAmount = discountAmount,
                                    subtotal = subtotal,
                                    total = total
                                )

                                quotation = quotation.copy(
                                    items = quotation.items + newItem
                                )
                                hasUnsavedChanges = true
                                showAddItemDialog = false
                            }
                        },
                        enabled = selectedProduct != null && quantity > 0
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddItemDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showError) {
            AlertDialog(
                title = "Error",
                message = errorMessage,
                onDismiss = { showError = false },
                onConfirm = { showError = false },
                type = AlertDialogType.Error,
                confirmText = "OK"
            )
        }

        if (showUnsavedChangesDialog) {
            AlertDialog(
                title = "Unsaved Changes",
                message = "You have unsaved changes. Are you sure you want to leave?",
                onDismiss = { showUnsavedChangesDialog = false },
                onConfirm = onCancel,
                type = AlertDialogType.Warning,
                confirmText = "Leave",
                dismissText = "Stay"
            )
        }

        // Add toast notification
        Toast(
            toast = toast,
            onDismiss = { toast = null }
        )
    }
}

@Composable
private fun QuotationItemCard(
    item: QuotationItem,
    onDelete: () -> Unit,
    onEdit: (QuotationItem) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity: ${item.quantity} ${item.unit}")
                    Text("Unit Price: ${currencyFormatter.format(item.unitPrice)}")
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (item.discount > 0) {
                        Text(
                            "Discount: ${currencyFormatter.format(item.discountAmount)}",
                            color = MaterialTheme.colors.error
                        )
                    }
                    Text("Tax: ${currencyFormatter.format(item.taxAmount)}")
                    Text(
                        "Total: ${currencyFormatter.format(item.total)}",
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        var quantity by remember { mutableStateOf(item.quantity) }
        var discount by remember { mutableStateOf(item.discount) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = quantity.toString(),
                        onValueChange = { value -> 
                            quantity = value.toIntOrNull() ?: item.quantity
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discount.toString(),
                        onValueChange = { value -> 
                            discount = value.toDoubleOrNull() ?: item.discount
                        },
                        label = { Text("Discount %") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Preview calculations
                    val subtotal = quantity * item.unitPrice
                    val discountAmount = subtotal * (discount / 100)
                    val taxAmount = (subtotal - discountAmount) * (item.tax / 100)
                    val total = subtotal - discountAmount + taxAmount

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Preview:", fontWeight = FontWeight.Bold)
                        Text("Subtotal: ${currencyFormatter.format(subtotal)}")
                        Text("Discount: ${currencyFormatter.format(discountAmount)}")
                        Text("Tax: ${currencyFormatter.format(taxAmount)}")
                        Text("Total: ${currencyFormatter.format(total)}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val subtotal = quantity * item.unitPrice
                        val discountAmount = subtotal * (discount / 100)
                        val taxAmount = (subtotal - discountAmount) * (item.tax / 100)
                        val total = subtotal - discountAmount + taxAmount

                        onEdit(item.copy(
                            quantity = quantity,
                            discount = discount,
                            discountAmount = discountAmount,
                            subtotal = subtotal,
                            taxAmount = taxAmount,
                            total = total
                        ))
                        showEditDialog = false
                    },
                    enabled = quantity > 0
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    color: Color = MaterialTheme.colors.onSurface,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.body1
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = style)
        Text(
            currencyFormatter.format(amount),
            style = style,
            color = color
        )
    }
} 