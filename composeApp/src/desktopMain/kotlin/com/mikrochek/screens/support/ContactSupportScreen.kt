package com.hermesys.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ContactSupportScreen(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.error
            )
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
            
            Text(
                text = "If the problem persists, please contact support at support@streamline.com",
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
} 