package com.mikrochek.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikrochek.server.database.models.Quotation
import com.mikrochek.server.database.models.QuotationStatus
import com.mikrochek.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat

@Composable
fun QuotationCard(
    quotation: Quotation,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy") }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section - Basic Info
            Column(modifier = Modifier.weight(0.3f)) {
                Text(
                    text = quotation.quotationNumber,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = quotation.customerName,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Created: ${dateFormat.format(quotation.date)}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }

            // Middle section - Status and Amount
            Column(
                modifier = Modifier.weight(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuotationStatusChip(status = quotation.status)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = currencyFormat.format(quotation.total),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = "${quotation.items.size} items",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }

            // Right section - Actions
            Row(
                modifier = Modifier.weight(0.3f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onView) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "View",
                        tint = MaterialTheme.colors.primary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colors.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
fun QuotationStatusChip(
    status: QuotationStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (status) {
        QuotationStatus.DRAFT -> AppColors.Gray200 to AppColors.Gray700
        QuotationStatus.SENT -> AppColors.InfoLight to AppColors.Info
        QuotationStatus.APPROVED -> AppColors.SuccessLight to AppColors.Success
        QuotationStatus.REJECTED -> AppColors.ErrorLight to AppColors.Error
        QuotationStatus.EXPIRED -> AppColors.WarningLight to AppColors.Warning
        QuotationStatus.COMPLETED -> AppColors.Success to Color.White
        QuotationStatus.READY_FOR_DISPATCH -> AppColors.Success to Color.White
        QuotationStatus.DISPATCHED -> AppColors.Success to Color.White
        QuotationStatus.IN_PRODUCTION -> AppColors.Warning to Color.White
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.caption,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
} 