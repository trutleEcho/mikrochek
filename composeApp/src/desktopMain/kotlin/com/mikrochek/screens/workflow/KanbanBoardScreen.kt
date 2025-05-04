package com.mikrochek.screens.workflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.QuotationStatus
import com.mikrochek.server.repository.quotation.QuotationRepository
import com.mikrochek.theme.AppColors
import org.koin.core.context.GlobalContext
import kotlin.getValue

enum class WorkflowStage(val title: String, val color: Color) {
    QUOTATION("Quotation", AppColors.Secondary),
    APPROVED("Approved", AppColors.Primary),
    IN_PRODUCTION("In Production", AppColors.Accent1),
    QUALITY_CHECK("Quality Check", AppColors.Accent3),
    READY_FOR_DISPATCH("Ready for Dispatch", AppColors.Accent5),
    DISPATCHED("Dispatched", AppColors.Success)
}

data class KanbanItem(
    val id: String,
    val title: String,
    val customer: String,
    val dateString: String,
    val amount: String,
    val stage: WorkflowStage,
    val status: String,
    val quotationId: String? = null
)

@Composable
fun KanbanBoardScreen(
    onNavigate: (NavDestination) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var boardItems by remember { mutableStateOf<List<KanbanItem>>(emptyList()) }
    var showArchived by remember { mutableStateOf(false) }
    val quotationRepository: QuotationRepository by GlobalContext.get().inject()

    // Load data
    LaunchedEffect(Unit) {
        isLoading = true
        
        // Get all quotations
        val quotations = quotationRepository.getAllQuotations()
        
        // Map to KanbanItems and group by stage
        boardItems = quotations.map { quotation ->
            val stage = when (quotation.status) {
                QuotationStatus.DRAFT, QuotationStatus.SENT -> WorkflowStage.QUOTATION
                QuotationStatus.APPROVED -> WorkflowStage.APPROVED
                QuotationStatus.IN_PRODUCTION -> WorkflowStage.IN_PRODUCTION
                QuotationStatus.COMPLETED -> WorkflowStage.QUALITY_CHECK
                QuotationStatus.READY_FOR_DISPATCH -> WorkflowStage.READY_FOR_DISPATCH
                QuotationStatus.DISPATCHED -> WorkflowStage.DISPATCHED
                else -> WorkflowStage.QUOTATION
            }
            
            KanbanItem(
                id = quotation.id,
                title = "Quotation #${quotation.quotationNumber}",
                customer = quotation.customerName,
                dateString = formatDate(quotation.date),
                amount = formatCurrency(quotation.total),
                stage = stage,
                status = quotation.status.name,
                quotationId = quotation.id
            )
        }
        
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        PageHeader(
            title = "Workflow Kanban Board",
            subtitle = "Track orders from quotation to dispatch",
            actions = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Refresh button
                    Box(
                        modifier = Modifier.width(130.dp)
                    ){
                        ActionButton(
                            text = "Refresh",
                            icon = Icons.Default.Refresh,
                            onClick = { /* Reload data */ }
                        )
                    }
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Kanban board with horizontal scroll
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(end = 16.dp)
                ) {
                    // Create a column for each workflow stage
                    WorkflowStage.values().forEach { stage ->
                        val itemsInStage = boardItems.filter { it.stage == stage }
                        
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .padding(end = 16.dp)
                                .background(
                                    color = stage.color.copy(alpha = 0.05f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .border(
                                    width = 1.dp,
                                    color = stage.color.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            // Stage header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = stage.color.copy(alpha = 0.1f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stage.title,
                                    style = MaterialTheme.typography.h6,
                                    color = stage.color
                                )
                                Surface(
                                    color = stage.color.copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "${itemsInStage.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.caption,
                                        color = stage.color
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Items list with vertical scroll
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(itemsInStage) { item ->
                                    KanbanCard(
                                        item = item,
                                        onClick = {
                                            if (item.quotationId != null) {
                                                onNavigate(NavDestination.QuotationEdit(item.quotationId))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanColumn(
    title: String,
    count: Int,
    color: Color,
    items: List<KanbanItem>,
    onItemClick: (KanbanItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
    ) {
        // Column header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.body2,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Column content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color.copy(alpha = 0.05f))
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                KanbanCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun KanbanCard(
    item: KanbanItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Customer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.customer,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.dateString,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                Text(
                    text = item.amount,
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor(item.status).copy(alpha = 0.1f))
                    .border(
                        width = 1.dp,
                        color = statusColor(item.status).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.caption,
                    color = statusColor(item.status)
                )
            }
        }
    }
}

// Helper functions
fun formatDate(timestamp: Long): String {
    val date = java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    return date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

fun formatCurrency(amount: Double): String {
    return "₹${String.format("%,.2f", amount)}"
}

fun statusColor(status: String): Color {
    return when (status) {
        "DRAFT" -> AppColors.Gray500
        "SENT" -> AppColors.Info
        "APPROVED" -> AppColors.Success
        "REJECTED" -> AppColors.Error
        "EXPIRED" -> AppColors.Gray400
        "IN_PRODUCTION" -> AppColors.Accent1
        "COMPLETED" -> AppColors.Accent3
        "READY_FOR_DISPATCH" -> AppColors.Accent5
        "DISPATCHED" -> AppColors.Success
        else -> AppColors.Gray400
    }
} 