package com.mikrochek.screens.base

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.QuickStatCard
import com.mikrochek.theme.AppColors
import com.mikrochek.navigation.NavDestination
import com.mikrochek.data.UserState
import com.mikrochek.server.database.models.QuotationStatus
import com.mikrochek.server.repository.po.PurchaseOrderRepository
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.server.repository.customer.CustomerRepository
import com.mikrochek.server.repository.quotation.QuotationRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MainDashboard(
    onNavigate: (NavDestination) -> Unit,
    purchaseOrderRepository: PurchaseOrderRepository,
    productRepository: ProductRepository,
    customerRepository: CustomerRepository,
    quotationRepository: QuotationRepository
) {
    val currentDate = remember {
        LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        )
    }
    
    // Stats state
    var purchaseOrderCount by remember { mutableStateOf(0) }
    var totalSales by remember { mutableStateOf(0.0) }
    var productCount by remember { mutableStateOf(0) }
    var activeQuotationsCount by remember { mutableStateOf(0) }
    
    // Kanban state
    var draftPOs by remember { mutableStateOf(emptyList<KanbanItem>()) }
    var pendingPOs by remember { mutableStateOf(emptyList<KanbanItem>()) }
    var approvedPOs by remember { mutableStateOf(emptyList<KanbanItem>()) }
    var completedPOs by remember { mutableStateOf(emptyList<KanbanItem>()) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!UserState.isUserValid()) {
            onNavigate(NavDestination.Login)
            return@LaunchedEffect
        }
        
        // Load stats
        coroutineScope.launch {
            val allPOs = purchaseOrderRepository.getAllPurchaseOrders()
            purchaseOrderCount = allPOs.size
            totalSales = allPOs.sumOf { it.total }
            
            val products = productRepository.getAllProducts()
            productCount = products.size
            
            val quotations = quotationRepository.getAllQuotations()
            activeQuotationsCount = quotations.count { it.status == QuotationStatus.APPROVED || it.status == QuotationStatus.IN_PRODUCTION || it.status == QuotationStatus.READY_FOR_DISPATCH || it.status == QuotationStatus.DISPATCHED }
            
            // Populate Kanban data
            draftPOs = allPOs.filter { it.status == "DRAFT" }
                .take(5)
                .map { KanbanItem(it.id, it.poNumber, it.vendorName, it.status) }
                
            pendingPOs = allPOs.filter { it.status == "PENDING" }
                .take(5)
                .map { KanbanItem(it.id, it.poNumber, it.vendorName, it.status) }
                
            approvedPOs = allPOs.filter { it.status == "APPROVED" }
                .take(5)
                .map { KanbanItem(it.id, it.poNumber, it.vendorName, it.status) }
                
            completedPOs = allPOs.filter { it.status == "COMPLETED" }
                .take(5)
                .map { KanbanItem(it.id, it.poNumber, it.vendorName, it.status) }
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        // Remove SideBar
        Box(modifier = Modifier.weight(1f)) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
                Divider()
                // Stats section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickStatCard(
                        icon = Icons.Default.ShoppingCart,
                        title = "Total Sales",
                        value = "₹${String.format("%.2f", totalSales)}",
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        name = "sales"
                    )
                    QuickStatCard(
                        icon = Icons.Default.LocalShipping,
                        title = "Purchase Orders",
                        value = purchaseOrderCount.toString(),
                        backgroundColor = MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onSecondary,
                        name = "orders"
                    )
                    QuickStatCard(
                        icon = Icons.Default.Inventory,
                        title = "Products",
                        value = productCount.toString(),
                        backgroundColor = AppColors.Success,
                        contentColor = AppColors.SuccessLight,
                        name = "products"
                    )
                    QuickStatCard(
                        icon = Icons.Default.Description,
                        title = "Active Quotations",
                        value = activeQuotationsCount.toString(),
                        backgroundColor = AppColors.Accent1,
                        contentColor = Color.White,
                        name = "quotations"
                    )
                }

                // Content section
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Mini Kanban Board
                    Surface(
                        modifier = Modifier
                            .weight(3f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Purchase Orders Board",
                                    style = MaterialTheme.typography.h6,
                                    color = MaterialTheme.colors.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { onNavigate(NavDestination.KanbanBoard) }
                                ) {
                                    Text("View All")
                                }
                            }
                            
                            // Kanban columns
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Draft column
                                KanbanColumn(
                                    title = "Draft",
                                    items = draftPOs,
                                    color = AppColors.Gray400,
                                    onItemClick = { onNavigate(NavDestination.PurchaseOrderEdit(it.number)) },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Pending column
                                KanbanColumn(
                                    title = "Pending",
                                    items = pendingPOs,
                                    color = AppColors.Warning,
                                    onItemClick = { onNavigate(NavDestination.PurchaseOrderEdit(it.id)) },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Approved column
                                KanbanColumn(
                                    title = "Approved",
                                    items = approvedPOs,
                                    color = AppColors.Info,
                                    onItemClick = { onNavigate(NavDestination.PurchaseOrderEdit(it.id)) },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Completed column
                                KanbanColumn(
                                    title = "Completed",
                                    items = completedPOs,
                                    color = AppColors.Success,
                                    onItemClick = { onNavigate(NavDestination.PurchaseOrderEdit(it.id)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Quick Actions
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            ActionButton(
                                icon = Icons.Default.Add,
                                text = "New Order",
                                onClick = { onNavigate(NavDestination.PurchaseOrderCreate) }
                            )
                            ActionButton(
                                icon = Icons.Default.Person,
                                text = "Add PO",
                                onClick = { onNavigate(NavDestination.PurchaseOrderCreate) }
                            )
                            ActionButton(
                                icon = Icons.Default.Inventory,
                                text = "Add Product",
                                onClick = { onNavigate(NavDestination.ProductCreate) }
                            )
                            ActionButton(
                                icon = Icons.Default.Assignment,
                                text = "Create Quotation",
                                onClick = { onNavigate(NavDestination.QuotationCreate) }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class KanbanItem(
    val id: String,
    val number: String,
    val vendor: String,
    val status: String
)

@Composable
fun KanbanColumn(
    title: String,
    items: List<KanbanItem>,
    color: Color,
    onItemClick: (KanbanItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // Column header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "$title (${items.size})",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Column items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                KanbanCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    color = color
                )
            }
        }
    }
}

@Composable
fun KanbanCard(
    item: KanbanItem,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = item.number,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.vendor,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
