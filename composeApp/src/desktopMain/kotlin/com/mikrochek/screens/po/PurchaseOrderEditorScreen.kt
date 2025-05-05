package com.mikrochek.screens.po

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.PurchaseOrder
import com.mikrochek.server.database.models.PurchaseOrderItem
import com.mikrochek.server.database.models.Product
import com.mikrochek.server.repository.po.PurchaseOrderRepository
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.theme.AppColors
import com.mikrochek.utils.TimeUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.mikrochek.components.Toast
import com.mikrochek.components.ToastData
import com.mikrochek.components.ToastType
import com.mikrochek.screens.base.LoadingScreen
import kotlin.plus
import kotlin.text.format
import kotlin.toString
import com.mikrochek.components.common.DropdownField

data class FormError(
    val field: String,
    val message: String
)

@Composable
fun PurchaseOrderEditorScreen(
    onNavigate: (NavDestination) -> Unit,
    purchaseOrderRepository: PurchaseOrderRepository,
    productRepository: ProductRepository,
    poId: String? = null
) {
    var purchaseOrder by remember { mutableStateOf<PurchaseOrder?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var formErrors by remember { mutableStateOf<List<FormError>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var toast by remember { mutableStateOf<ToastData?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Form state
    var poNumber by remember { mutableStateOf("") }
    var vendorName by remember { mutableStateOf("") }
    var vendorAddress by remember { mutableStateOf("") }
    var vendorContact by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var status by remember { mutableStateOf("DRAFT") }
    var items by remember { mutableStateOf<List<PurchaseOrderItem>>(emptyList()) }
    var terms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Product selection state
    var showProductSelector by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("0.00") }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // Calculate totals
    val subtotal = items.sumOf { it.quantity * it.unitPrice }
    val tax = subtotal * 0.1 // 10% tax rate
    val total = subtotal + tax

    LaunchedEffect(poId) {
        try {
            if (poId != null) {
                println("Loading PO with ID: $poId")
                val existingPO = purchaseOrderRepository.getPurchaseOrderById(poId)
                if (existingPO != null) {
                    println("Found PO: $existingPO")
                    purchaseOrder = existingPO
                    poNumber = existingPO.poNumber
                    vendorName = existingPO.vendorName
                    vendorAddress = existingPO.vendorAddress
                    vendorContact = existingPO.vendorContact
                    deliveryDate = LocalDate.ofEpochDay(existingPO.deliveryDate ?: 0)
                    status = existingPO.status
                    items = existingPO.items
                    terms = existingPO.terms
                    notes = existingPO.notes
                } else {
                    println("No PO found with ID: $poId")
                    toast = ToastData("Purchase order not found", ToastType.ERROR)
                }
            }
            products = productRepository.getAllProducts(isActive = true)
        } catch (e: Exception) {
            e.printStackTrace()
            toast = ToastData("Failed to load purchase order: ${e.message}", ToastType.ERROR)
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): List<FormError> {
        val errors = mutableListOf<FormError>()

        if (poNumber.isBlank()) {
            errors.add(FormError("poNumber", "PO Number is required"))
        }

        if (vendorName.isBlank()) {
            errors.add(FormError("vendorName", "Vendor Name is required"))
        }

        if (items.isEmpty()) {
            errors.add(FormError("items", "At least one item is required"))
        }

        return errors
    }

    suspend fun handleSave() {
        val errors = validateForm()
        if (errors.isNotEmpty()) {
            formErrors = errors
            return
        }

        try {
            val newPO = PurchaseOrder(
                id = purchaseOrder?.id ?: UUID.randomUUID().toString(),
                poNumber = poNumber,
                vendorName = vendorName,
                vendorAddress = vendorAddress,
                vendorContact = vendorContact,
                issueDate = TimeUtils.getCurrentISTTimestamp(),
                deliveryDate = deliveryDate.toEpochDay(),
                items = items,
                terms = terms,
                notes = notes,
                status = status
            )

            if (poId != null) {
                // Update existing PO
                purchaseOrderRepository.updatePurchaseOrder(newPO)
            } else {
                // Create new PO
                purchaseOrderRepository.createPurchaseOrder(newPO)
            }

            hasUnsavedChanges = false
            showSaveConfirmation = false
            toast = ToastData(
                "Purchase order ${if (poId != null) "updated" else "created"} successfully",
                ToastType.SUCCESS
            )
            onNavigate(NavDestination.PurchaseOrdersList)
        } catch (e: Exception) {
            e.printStackTrace()
            toast = ToastData(
                "Failed to ${if (poId != null) "update" else "create"} purchase order: ${e.message}",
                ToastType.ERROR
            )
        }
    }


    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text("Save Purchase Order") },
            text = { Text("Are you sure you want to save this purchase order?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        handleSave()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showProductSelector) {
        AlertDialog(
            onDismissRequest = { showProductSelector = false },
            title = { Text("Add Item") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Product Dropdown
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "",
                            onValueChange = {},
                            label = { Text("Product") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select product")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = selectedProduct == null
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .heightIn(max = 300.dp)
                                .background(MaterialTheme.colors.surface)
                        ) {
                            products.forEach { product ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedProduct = product
                                        unitPrice = product.sellingPrice.toString()
                                        expanded = false
                                    }
                                ) {
                                    Column {
                                        Text(product.name)
                                        Text(
                                            "Price: ${currencyFormatter.format(product.sellingPrice)}",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                quantity = newValue
                            }
                        },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = quantity.isEmpty() || (quantity.toIntOrNull() ?: 0) <= 0
                    )

                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                unitPrice = newValue
                            }
                        },
                        label = { Text("Unit Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = unitPrice.isEmpty() || (unitPrice.toDoubleOrNull() ?: 0.0) <= 0.0
                    )

                    // Show total
                    if (selectedProduct != null && quantity.isNotEmpty() && unitPrice.isNotEmpty()) {
                        val qty = quantity.toIntOrNull() ?: 0
                        val price = unitPrice.toDoubleOrNull() ?: 0.0
                        Text(
                            "Total: ${currencyFormatter.format(qty * price)}",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedProduct?.let { product ->
                            val qtyValue = quantity.toIntOrNull() ?: 0
                            val priceValue = unitPrice.toDoubleOrNull() ?: product.sellingPrice
                            if (qtyValue > 0) {
                                val newItem = PurchaseOrderItem(
                                    id = UUID.randomUUID().toString(),
                                    product = product,
                                    quantity = qtyValue,
                                    unitPrice = priceValue
                                )
                                items = items + newItem
                                hasUnsavedChanges = true
                                showProductSelector = false
                                selectedProduct = null
                                quantity = "1"
                                unitPrice = "0.00"
                            }
                        }
                    },
                    enabled = selectedProduct != null &&
                            quantity.isNotEmpty() &&
                            (quantity.toIntOrNull() ?: 0) > 0 &&
                            unitPrice.isNotEmpty() &&
                            (unitPrice.toDoubleOrNull() ?: 0.0) > 0.0
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showProductSelector = false
                        selectedProduct = null
                        quantity = "1"
                        unitPrice = "0.00"
                    }
                ) {
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

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            if (isLoading) {
                LoadingScreen()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PageHeader(
                        title = if (poId == null) "Create Purchase Order" else "Edit Purchase Order",
                        subtitle = if (poId == null) "Create a new purchase order" else "Modify existing purchase order #$poNumber",
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (hasUnsavedChanges) {
                                    Text(
                                        "Unsaved changes",
                                        style = MaterialTheme.typography.caption,
                                        color = AppColors.Warning,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                }
                                Box(modifier = Modifier.width(120.dp)) {
                                    ActionButton(
                                        text = "Cancel",
                                        icon = Icons.Default.Close,
                                        onClick = {
                                            if (hasUnsavedChanges) {
                                                // Show confirmation dialog
                                            } else {
                                                onNavigate(NavDestination.PurchaseOrdersList)
                                            }
                                        }
                                    )
                                }
                                Box(modifier = Modifier.width(100.dp)) {
                                    ActionButton(
                                        text = "Save",
                                        icon = Icons.Default.Save,
                                        onClick = {
                                            showSaveConfirmation = true
                                        }
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
                                    OutlinedTextField(
                                        value = poNumber,
                                        onValueChange = {
                                            poNumber = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("PO Number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = poId == null,
                                        isError = formErrors.any { it.field == "poNumber" }
                                    )
                                    if (formErrors.any { it.field == "poNumber" }) {
                                        Text(
                                            text = formErrors.first { it.field == "poNumber" }.message,
                                            color = MaterialTheme.colors.error,
                                            style = MaterialTheme.typography.caption
                                        )
                                    }

                                    OutlinedTextField(
                                        value = vendorName,
                                        onValueChange = {
                                            vendorName = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Vendor Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = formErrors.any { it.field == "vendorName" }
                                    )
                                    if (formErrors.any { it.field == "vendorName" }) {
                                        Text(
                                            text = formErrors.first { it.field == "vendorName" }.message,
                                            color = MaterialTheme.colors.error,
                                            style = MaterialTheme.typography.caption
                                        )
                                    }

                                    OutlinedTextField(
                                        value = vendorAddress,
                                        onValueChange = {
                                            vendorAddress = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Vendor Address") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = vendorContact,
                                        onValueChange = {
                                            vendorContact = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Vendor Contact") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = deliveryDate.format(dateFormatter),
                                        onValueChange = {
                                            try {
                                                deliveryDate = LocalDate.parse(it, dateFormatter)
                                                hasUnsavedChanges = true
                                            } catch (e: Exception) {
                                                // Invalid date format
                                            }
                                        },
                                        label = { Text("Delivery Date") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Status Dropdown
                                    DropdownField(
                                        value = status,
                                        onValueChange = { newStatus ->
                                            status = newStatus
                                            hasUnsavedChanges = true
                                        },
                                        items = listOf(
                                            "DRAFT",
                                            "PENDING",
                                            "APPROVED",
                                            "COMPLETED",
                                            "CANCELLED"
                                        ),
                                        label = "Status",
                                        itemToString = { it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
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
                                    OutlinedTextField(
                                        value = terms,
                                        onValueChange = {
                                            terms = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Terms and Conditions") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5
                                    )

                                    OutlinedTextField(
                                        value = notes,
                                        onValueChange = {
                                            notes = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Additional Notes") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                }
                            }
                        }

                        // Right Column - Items and Summary
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Section(
                                title = "Items",
                                collapsible = true,
                                defaultExpanded = true,
                                actions = {
                                    Box(
                                        modifier = Modifier.width(160.dp)
                                    ) {
                                        ActionButton(
                                            text = "Add Item",
                                            icon = Icons.Default.Add,
                                            onClick = { showProductSelector = true },
                                        )
                                    }
                                }
                            ) {
                                if (formErrors.any { it.field == "items" }) {
                                    Text(
                                        text = formErrors.first { it.field == "items" }.message,
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(items) { item ->
                                        PurchaseOrderItemRow(
                                            item = item,
                                            onDelete = {
                                                items = items.filter { it != item }
                                                hasUnsavedChanges = true
                                            },
                                            onQuantityChange = { newQuantity: Int ->
                                                items = items.map {
                                                    if (it == item) it.copy(quantity = newQuantity)
                                                    else it
                                                }
                                                hasUnsavedChanges = true
                                            }
                                        )
                                    }
                                }

                                if (items.isEmpty()) {
                                    Text(
                                        text = "No items added yet",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 16.dp))

                                // Order Summary
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Subtotal")
                                        Text(currencyFormatter.format(subtotal))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tax (10%)")
                                        Text(currencyFormatter.format(tax))
                                    }
                                    Divider()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Total",
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            currencyFormatter.format(total),
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Bold
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
}

@Composable
fun PurchaseOrderItemRow(
    item: PurchaseOrderItem,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.product.sellingPrice.toString(),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Quantity
            OutlinedTextField(
                value = item.quantity.toString(),
                onValueChange = {
                    val newQuantity = it.toIntOrNull()
                    if (newQuantity != null && newQuantity > 0) {
                        onQuantityChange(newQuantity)
                    }
                },
                modifier = Modifier.width(80.dp),
                textStyle = MaterialTheme.typography.body2
            )

            // Unit price
            Text(
                text = currencyFormatter.format(item.unitPrice),
                style = MaterialTheme.typography.body2
            )

            // Total
            Text(
                text = currencyFormatter.format(item.total),
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Medium
            )

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colors.error
                )
            }
        }
    }
}