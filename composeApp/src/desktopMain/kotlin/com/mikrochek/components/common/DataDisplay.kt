package com.mikrochek.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikrochek.theme.AppColors

@Composable
fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun Badge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colors.primary,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusIndicator(
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    color: Color = MaterialTheme.colors.primary
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = color,
                backgroundColor = color.copy(alpha = 0.1f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun InfoBadge(
    text: String,
    type: InfoBadgeType = InfoBadgeType.Info,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (type) {
        InfoBadgeType.Success -> AppColors.SuccessLight to AppColors.Success
        InfoBadgeType.Warning -> AppColors.WarningLight to AppColors.Warning
        InfoBadgeType.Error -> AppColors.ErrorLight to AppColors.Error
        InfoBadgeType.Info -> AppColors.InfoLight to AppColors.Info
    }
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.caption,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class InfoBadgeType {
    Success, Warning, Error, Info
}

@Composable
fun KeyValueTable(
    items: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (key, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
        }
    }
} 