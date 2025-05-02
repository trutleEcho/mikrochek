package com.mikrochek.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    name: String,
    contentColor: Color = MaterialTheme.colors.onSurface
) {
    Surface(
        modifier = Modifier
            .height(140.dp)
            .width(280.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon and Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = backgroundColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1,
                    color = contentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Value and Name
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.h4,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.caption,
                    color = contentColor
                )
            }
        }
    }
} 