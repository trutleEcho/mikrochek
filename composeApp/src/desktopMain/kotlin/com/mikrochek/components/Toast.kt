package com.mikrochek.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS,
    ERROR, INFO
}

data class ToastData(
    val message: String,
    val type: ToastType = ToastType.SUCCESS,
    val duration: Long = 3000L // Duration in milliseconds
)

@Composable
fun Toast(
    toast: ToastData?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        toast?.let {
            LaunchedEffect(toast) {
                delay(it.duration)
                onDismiss()
            }

            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            color = when (it.type) {
                                ToastType.SUCCESS -> Color(0xFF4CAF50)
                                ToastType.ERROR -> Color(0xFFE53935)
                                ToastType.INFO -> Color(0xFFCBD5E1)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (it.type) {
                            ToastType.SUCCESS -> Icons.Default.Check
                            ToastType.ERROR -> Icons.Default.Warning
                            ToastType.INFO -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = it.message,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
} 