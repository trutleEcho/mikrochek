package com.mikrochek.screens.quotation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.ContentCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.*
import com.mikrochek.server.repository.quotation.QuotationRepository
import com.mikrochek.theme.AppColors
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun QuotationFormScreen(
    onClose: () -> Unit,
    quotationToEdit: Quotation? = null,
    onNavigate: (NavDestination) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var quotationItems by remember { mutableStateOf<List<QuotationItem>>(emptyList()) }
    var notes by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }
    var validityDays by remember { mutableStateOf(30) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    
    // Calculate totals
    val subtotal = quotationItems.sumOf { it.quantity * it.unitPrice }
    val taxTotal = quotationItems.sumOf { it.taxAmount }
    val total = quotationItems.sumOf { it.total }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = quotationToEdit?.let { "Edit Quotation" } ?: "New Quotation",
                        style = MaterialTheme.typography.h5
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close")
                            Spacer(Modifier.width(4.dp))
                            Text("Cancel")
                        }
                        Button(
                            onClick = { /* Save quotation logic */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Icon(Icons.Default.Save, "Save")
                            Spacer(Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Customer Selection
                OutlinedButton(onClick = { /* Show customer selection dialog */ }) {
                    Icon(Icons.Default.Person, "Select Customer")
                    Spacer(Modifier.width(8.dp))
                    Text(selectedCustomer?.companyName ?: "Select Customer")
                }

                Spacer(Modifier.height(16.dp))

                // Validity
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Valid for")
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = validityDays.toString(),
                        onValueChange = { validityDays = it.toIntOrNull() ?: 30 },
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("days")
                }

                Spacer(Modifier.height(24.dp))

                // Items List
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Items", style = MaterialTheme.typography.h6)
                            ActionButton(
                                icon = Icons.Default.Add,
                                text = "Add Item",
                                onClick = { showAddItemDialog = true }
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quotationItems) { item ->
                                QuotationItemRow(
                                    item = item,
                                    currencyFormatter = currencyFormatter,
                                    onDelete = {
                                        quotationItems = quotationItems.filter { it != item }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Totals
                Column(
                    modifier = Modifier.align(Alignment.End),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "Subtotal: ${currencyFormatter.format(subtotal)}",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        "Tax: ${currencyFormatter.format(taxTotal)}",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        "Total: ${currencyFormatter.format(total)}",
                        style = MaterialTheme.typography.h6
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Notes and Terms
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Terms & Conditions") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Add Item Dialog
            if (showAddItemDialog) {
                AddItemDialog(
                    onDismiss = { showAddItemDialog = false },
                    onAddItem = { newItem ->
                        quotationItems = quotationItems + newItem
                        showAddItemDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun QuotationItemRow(
    item: QuotationItem,
    currencyFormatter: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.caption
                )
            }
            Text(
                text = "${item.quantity} ${item.unit}",
                modifier = Modifier.width(100.dp)
            )
            Text(
                text = currencyFormatter.format(item.unitPrice),
                modifier = Modifier.width(100.dp)
            )
            Text(
                text = "${item.tax}%",
                modifier = Modifier.width(80.dp)
            )
            Text(
                text = currencyFormatter.format(item.total),
                modifier = Modifier.width(120.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete item")
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onAddItem: (QuotationItem) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var unitPrice by remember { mutableStateOf(0.0) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Product selection button
                OutlinedButton(onClick = { /* Show product selection */ }) {
                    Text(selectedProduct?.name ?: "Select Product")
                }

                // Quantity
                TextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = it.toIntOrNull() ?: 1 },
                    label = { Text("Quantity") }
                )

                // Unit Price
                TextField(
                    value = unitPrice.toString(),
                    onValueChange = { unitPrice = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Unit Price") }
                )

                // Description
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProduct?.let { product ->
                        val taxAmount = quantity * unitPrice * (product.tax / 100)
                        val total = quantity * unitPrice + taxAmount
                        onAddItem(
                            QuotationItem(
                                productId = product.id,
                                productCode = product.code,
                                productName = product.name,
                                description = description.ifEmpty { product.description },
                                quantity = quantity,
                                unit = product.unit,
                                unitPrice = unitPrice,
                                tax = product.tax,
                                taxAmount = taxAmount,
                                total = total,
                                id = product.id,
                                discount = TODO(),
                                discountAmount = TODO(),
                                subtotal = TODO()
                            )
                        )
                    }
                },
                enabled = selectedProduct != null && quantity > 0 && unitPrice > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 