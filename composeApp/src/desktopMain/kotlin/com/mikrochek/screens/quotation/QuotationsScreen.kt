package com.mikrochek.screens.quotation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.ContentCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.components.layout.Section
import com.mikrochek.components.QuotationCard
import com.mikrochek.components.SearchBar
import com.mikrochek.components.common.AlertDialog
import com.mikrochek.components.common.AlertDialogType
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.DocumentType
import com.mikrochek.server.database.models.Quotation
import com.mikrochek.server.database.models.QuotationStatus
import com.mikrochek.server.repository.quotation.QuotationRepository
import com.mikrochek.server.service.DocumentService
import com.mikrochek.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun QuotationsScreen(
    documentService: DocumentService,
    onCreateNew: () -> Unit,
    onEditQuotation: (String) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var quotations by remember { mutableStateOf<List<Quotation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<QuotationStatus?>(null) }
    var dateRange by remember { mutableStateOf<Pair<Long?, Long?>>(null to null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<Quotation?>(null) }
    
    val scope = rememberCoroutineScope()

    // Load quotations
    LaunchedEffect(searchQuery, selectedStatus, dateRange) {
        try {
            isLoading = true
            val docs = documentService.searchDocuments(
                type = DocumentType.QUOTATION,
                query = searchQuery.takeIf { it.isNotBlank() }
            )
            quotations = docs.mapNotNull { doc ->
                try {
                    Json.decodeFromString<Quotation>(doc.content)
                } catch (e: Exception) {
                    null
                }
            }.filter { quotation ->
                val matchesStatus = selectedStatus?.let { it == quotation.status } ?: true
                val matchesDateRange = dateRange.let { (start, end) ->
                    val afterStart = start?.let { quotation.date >= it } ?: true
                    val beforeEnd = end?.let { quotation.date <= it } ?: true
                    afterStart && beforeEnd
                }
                matchesStatus && matchesDateRange
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load quotations: ${e.message}"
            showError = true
        } finally {
            isLoading = false
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Content goes directly here without SideBar
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                PageHeader(
                    title = "Quotations",
                    subtitle = "${quotations.size} quotations total",
                    actions = {
                        Box(modifier = Modifier.width(180.dp)){
                            ActionButton(
                                icon = Icons.Default.Add,
                                text = "New Quotation",
                                onClick = onCreateNew
                            )
                        }
                    }
                )

                // Search and filters
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    selectedStatus = selectedStatus,
                    onStatusChange = { selectedStatus = it },
                    dateRange = dateRange,
                    onDateRangeChange = { dateRange = it }
                )

                // Quotations list
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    elevation = 1.dp
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (quotations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "No quotations found",
                                    style = MaterialTheme.typography.h6,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                )
                                OutlinedButton(onClick = onCreateNew) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Create New Quotation")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quotations) { quotation ->
                                QuotationCard(
                                    quotation = quotation,
                                    onEdit = { onEditQuotation(quotation.id) },
                                    onView = { /* TODO: Implement view action */ },
                                    onDelete = { showDeleteConfirmation = quotation }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Error dialog
    if (showError) {
        AlertDialog(
            title = "Error",
            message = errorMessage,
            onDismiss = { showError = false },
            onConfirm = { showError = false },
            type = AlertDialogType.Error,
            confirmText = "OK"
        )
    }

    // Delete confirmation dialog
    showDeleteConfirmation?.let { quotation ->
        AlertDialog(
            title = "Delete Quotation",
            message = "Are you sure you want to delete quotation ${quotation.quotationNumber}? This action cannot be undone.",
            onDismiss = { showDeleteConfirmation = null },
            onConfirm = {
                scope.launch {
                    try {
                        documentService.deleteDocument(quotation.id)
                        showDeleteConfirmation = null
                    } catch (e: Exception) {
                        errorMessage = "Failed to delete quotation: ${e.message}"
                        showError = true
                    }
                }
            },
            type = AlertDialogType.Warning,
            confirmText = "Delete",
            dismissText = "Cancel"
        )
    }
} 