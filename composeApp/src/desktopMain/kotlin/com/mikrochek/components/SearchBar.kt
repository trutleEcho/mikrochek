package com.mikrochek.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mikrochek.server.database.models.QuotationStatus
import com.mikrochek.theme.AppColors

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedStatus: QuotationStatus?,
    onStatusChange: (QuotationStatus?) -> Unit,
    dateRange: Pair<Long?, Long?>,
    onDateRangeChange: (Pair<Long?, Long?>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Search bar with filter toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text("Search quotations...") }
                )
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        if (showFilters) Icons.Default.FilterList else Icons.Default.FilterListOff,
                        contentDescription = "Toggle Filters",
                        tint = if (selectedStatus != null || dateRange.first != null || dateRange.second != null)
                            MaterialTheme.colors.primary
                        else
                            MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Filters section
        AnimatedVisibility(visible = showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Status filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuotationStatus.values().forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { onStatusChange(if (selectedStatus == status) null else status) },
                            status = status
                        )
                    }
                }

                // Date range filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date range implementation would go here
                    // For now, we'll just show a placeholder
                    OutlinedButton(
                        onClick = { /* Show date picker */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DateRange, "Date Range")
                        Spacer(Modifier.width(8.dp))
                        Text(dateRange.first?.toString() ?: "Start Date")
                    }
                    OutlinedButton(
                        onClick = { /* Show date picker */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DateRange, "Date Range")
                        Spacer(Modifier.width(8.dp))
                        Text(dateRange.second?.toString() ?: "End Date")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    status: QuotationStatus
) {
    val (backgroundColor, contentColor) = if (selected) {
        when (status) {
            QuotationStatus.DRAFT -> AppColors.Gray700 to Color.White
            QuotationStatus.SENT -> AppColors.Info to Color.White
            QuotationStatus.APPROVED -> AppColors.Success to Color.White
            QuotationStatus.REJECTED -> AppColors.Error to Color.White
            QuotationStatus.EXPIRED -> AppColors.Warning to Color.White
            QuotationStatus.IN_PRODUCTION -> AppColors.Warning to Color.White
            QuotationStatus.COMPLETED -> AppColors.Success to Color.White
            QuotationStatus.READY_FOR_DISPATCH -> AppColors.Success to Color.White
            QuotationStatus.DISPATCHED -> AppColors.Success to Color.White
        }
    } else {
        when (status) {
            QuotationStatus.DRAFT -> AppColors.Gray200 to AppColors.Gray700
            QuotationStatus.SENT -> AppColors.InfoLight to AppColors.Info
            QuotationStatus.APPROVED -> AppColors.SuccessLight to AppColors.Success
            QuotationStatus.REJECTED -> AppColors.ErrorLight to AppColors.Error
            QuotationStatus.EXPIRED -> AppColors.WarningLight to AppColors.Warning
            QuotationStatus.IN_PRODUCTION -> AppColors.Warning to AppColors.Secondary
            QuotationStatus.COMPLETED -> AppColors.Success to Color.White
            QuotationStatus.READY_FOR_DISPATCH -> AppColors.Success to Color.White
            QuotationStatus.DISPATCHED -> AppColors.Success to Color.White
        }
    }

    Surface(
        modifier = Modifier
            .height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        elevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.name,
                style = MaterialTheme.typography.caption,
                color = contentColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
} 