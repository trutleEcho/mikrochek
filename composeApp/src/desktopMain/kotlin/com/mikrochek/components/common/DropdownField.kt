package com.mikrochek.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun <T> DropdownField(
    value: T?,
    onValueChange: (T) -> Unit,
    items: List<T>,
    label: String,
    itemToString: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value?.let { itemToString(it) } ?: "",
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            isError = error != null,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Select")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true }
        )
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = 8.dp
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items) { item ->
                        Text(
                            text = itemToString(item),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(item)
                                    expanded = false
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.body1
                        )
                        Divider()
                    }
                }
            }
        }
    }
} 