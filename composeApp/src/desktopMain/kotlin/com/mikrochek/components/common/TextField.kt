package com.mikrochek.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    error: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = {
                when {
                    error != null -> Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colors.error
                    )
                    trailingIcon != null && onTrailingIconClick != null -> IconButton(onClick = onTrailingIconClick) {
                        Icon(trailingIcon, contentDescription = null)
                    }
                }
            },
            isError = error != null,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                errorBorderColor = MaterialTheme.colors.error,
                focusedLabelColor = MaterialTheme.colors.primary,
                unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                errorLabelColor = MaterialTheme.colors.error
            )
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
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    error: String? = null,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        error = error,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        onTrailingIconClick = { passwordVisible = !passwordVisible },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    leadingIcon: ImageVector? = null,
    onSearch: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = MaterialTheme.colors.surface,
            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        ),
        shape = MaterialTheme.shapes.medium
    )
} 