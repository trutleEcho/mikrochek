package com.mikrochek.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,

    backgroundColor: Color = MaterialTheme.colors.primary,
    description: String? = null,
    outlined: Boolean = false
) {
    Surface(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (outlined) Color.Transparent else backgroundColor,
        border = if (outlined) BorderStroke(1.dp, MaterialTheme.colors.primary) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.button
                )
                if (description != null) {
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
} 