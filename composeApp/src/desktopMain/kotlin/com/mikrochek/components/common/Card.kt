package com.mikrochek.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikrochek.theme.AppColors

@Composable
fun InfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        elevation = 1.dp,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body2,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
            if (onClick != {}) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Navigate",
                    tint = contentColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        elevation = 1.dp,
        color = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface
            )
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.caption,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    trend: Double? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        elevation = 1.dp,
        color = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                trend?.let {
                    val color = if (it >= 0) AppColors.Success else AppColors.Error
                    Text(
                        text = String.format("%+.1f%%", it),
                        style = MaterialTheme.typography.caption,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
} 