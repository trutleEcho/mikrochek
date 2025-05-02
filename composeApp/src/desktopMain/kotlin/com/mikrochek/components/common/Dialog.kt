package com.mikrochek.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mikrochek.theme.AppColors

@Composable
fun AlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    type: AlertDialogType = AlertDialogType.Info
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        tint = type.color,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(dismissText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = type.color,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

enum class AlertDialogType(
    val icon: ImageVector,
    val color: Color
) {
    Info(Icons.Default.Info, AppColors.Info),
    Success(Icons.Default.CheckCircle, AppColors.Success),
    Warning(Icons.Default.Warning, AppColors.Warning),
    Error(Icons.Default.Error, AppColors.Error)
}

@Composable
fun LoadingDialog(
    message: String = "Loading...",
    onDismiss: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .width(300.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector? = null,
    destructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = if (destructive) AppColors.Error else MaterialTheme.colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(dismissText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (destructive) AppColors.Error else MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    submitText: String = "Submit",
    dismissText: String = "Cancel",
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                content()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(dismissText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text(submitText)
                    }
                }
            }
        }
    }
} 