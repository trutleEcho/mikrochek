package com.mikrochek.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colors.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colors.primary,
            disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color = MaterialTheme.colors.primary
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colors.primary,
    contentDescription: String? = null
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) color else MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
        )
    }
} 