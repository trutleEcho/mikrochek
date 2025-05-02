package com.mikrochek.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikrochek.server.database.models.Product
import com.mikrochek.theme.AppColors

@Composable
fun QuickStockUpdateDialog(
    product: Product,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf("0") }
    var isAddition by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Update Stock")
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current stock info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Stock:")
                    Text(
                        text = "${product.currentStock} ${product.unit}",
                        style = MaterialTheme.typography.body1,
                        color = if (product.currentStock <= product.minStock)
                            AppColors.Error else MaterialTheme.colors.onSurface
                    )
                }

                // Operation selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isAddition = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = if (isAddition)
                                AppColors.Success.copy(alpha = 0.1f)
                            else MaterialTheme.colors.surface
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add stock",
                            tint = if (isAddition)
                                AppColors.Success
                            else MaterialTheme.colors.onSurface
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                    OutlinedButton(
                        onClick = { isAddition = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = if (!isAddition)
                                AppColors.Error.copy(alpha = 0.1f)
                            else MaterialTheme.colors.surface
                        )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove stock",
                            tint = if (!isAddition)
                                AppColors.Error
                            else MaterialTheme.colors.onSurface
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Remove")
                    }
                }

                // Quantity input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                            quantity = value
                            error = null
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null
                )

                // Error message
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }

                // New stock preview
                val quantityNum = quantity.toIntOrNull() ?: 0
                val newStock = if (isAddition)
                    product.currentStock + quantityNum
                else
                    product.currentStock - quantityNum

                Text(
                    text = "New Stock: $newStock ${product.unit}",
                    style = MaterialTheme.typography.body1,
                    color = when {
                        newStock < 0 -> AppColors.Error
                        newStock <= product.minStock -> AppColors.Warning
                        else -> AppColors.Success
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantityNum = quantity.toIntOrNull()
                    if (quantityNum == null) {
                        error = "Please enter a valid number"
                        return@TextButton
                    }
                    
                    if (!isAddition && quantityNum > product.currentStock) {
                        error = "Cannot remove more than current stock"
                        return@TextButton
                    }
                    
                    val finalQuantity = if (isAddition) quantityNum else -quantityNum
                    onUpdate(finalQuantity)
                },
                enabled = quantity.isNotEmpty() && quantity != "0" && error == null
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 