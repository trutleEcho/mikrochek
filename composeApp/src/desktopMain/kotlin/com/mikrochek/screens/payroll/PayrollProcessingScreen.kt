package com.mikrochek.screens.payroll

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikrochek.components.common.SimpleSearchBar
import com.mikrochek.components.Toast
import com.mikrochek.components.ToastData
import com.mikrochek.components.ToastType
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.navigation.NavDestination
import com.mikrochek.screens.base.LoadingScreen
import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.database.models.EmployeePayroll
import com.mikrochek.server.database.models.PaymentStatus
import com.mikrochek.server.repository.employee.EmployeeRepository
import com.mikrochek.server.repository.employee.EmployeeRepositorySQLiteImpl
import com.mikrochek.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.getValue

@Composable
fun PayrollProcessingScreen(
    onNavigate: (NavDestination) -> Unit,
    onEmployeeDetails: (NavDestination) -> Unit,
    showToast: (String, ToastType) -> Unit,
) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember { currentDate.monthValue }
    val currentYear = remember { currentDate.year }
    val employeeRepository: EmployeeRepository by GlobalContext.get().inject()
    val sqlRepository = employeeRepository as? EmployeeRepositorySQLiteImpl

    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var employees by remember { mutableStateOf(emptyList<Employee>()) }
    var payrolls by remember { mutableStateOf(emptyList<EmployeePayroll>()) }
    var processingEmployeeId by remember { mutableStateOf<String?>(null) }
    var isGeneratingPayrolls by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<ToastData?>(null) }

    val scope = rememberCoroutineScope()

    // Filter options
    var showProcessed by remember { mutableStateOf(true) }
    var showPending by remember { mutableStateOf(true) }
    var showFailed by remember { mutableStateOf(true) }

    // Function to show toast messages
    fun showToastMessage(message: String, type: ToastType) {
        toast = ToastData(message = message, type = type)
    }

    // Function to load data
    fun loadData() {
        scope.launch {
            isLoading = true

            // Get all active employees
            employees = employeeRepository.getAllEmployees(true)

            // Get payrolls for the selected month
            val allPayrolls = employeeRepository.getPayrollsByMonth(selectedMonth, selectedYear)

            // Apply filters
            payrolls = allPayrolls.filter { payroll ->
                when (payroll.paymentStatus) {
                    PaymentStatus.PAID -> showProcessed
                    PaymentStatus.PENDING -> showPending
                    else -> showFailed
                }
            }

            isLoading = false
        }
    }

    // Load data when parameters change
    LaunchedEffect(selectedMonth, selectedYear, showProcessed, showPending, showFailed) {
        loadData()
    }

    // Filter payrolls by search query
    val filteredPayrolls = remember(payrolls, searchQuery) {
        if (searchQuery.isBlank()) {
            payrolls
        } else {
            payrolls.filter { payroll ->
                val employee = employees.find { it.id == payroll.employeeId }
                "${employee?.firstName} ${employee?.lastName}".contains(searchQuery, ignoreCase = true) ||
                        employee?.employeeId?.contains(searchQuery, ignoreCase = true) == true ||
                        employee?.department?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // Process payroll for a specific employee
    fun processEmployeePayroll(employeeId: String) {
        scope.launch {
            processingEmployeeId = employeeId
            val result = employeeRepository.processPayroll(employeeId, selectedMonth, selectedYear)

            if (result.isSuccess) {
                showToastMessage("Payroll processed successfully", ToastType.SUCCESS)
                
                // Reload data to get updated status
                loadData()
            } else {
                showToastMessage(
                    result.exceptionOrNull()?.message ?: "Failed to process payroll",
                    ToastType.ERROR
                )
            }

            processingEmployeeId = null
        }
    }
    
    // Generate payrolls for all employees without processing payments
    fun generatePayrolls() {
        scope.launch {
            isGeneratingPayrolls = true
            
            if (sqlRepository != null) {
                val result = sqlRepository.createPendingPayrolls(selectedMonth, selectedYear)
                
                if (result.isSuccess) {
                    showToastMessage("Payroll entries generated for all employees", ToastType.SUCCESS)
                    // Reload data to show the new entries
                    loadData()
                } else {
                    showToastMessage(
                        result.exceptionOrNull()?.message ?: "Failed to generate payroll entries",
                        ToastType.ERROR
                    )
                }
            } else {
                showToastMessage("Repository implementation not supported", ToastType.ERROR)
            }
            
            isGeneratingPayrolls = false
        }
    }

    if (isLoading) {
        LoadingScreen()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            PageHeader(
                title = "Process Payroll",
                subtitle = "Generate and process employee payrolls",
                icon = Icons.Default.Payment
            )

            // Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month/Year selection
                Box {
                    var monthExpanded by remember { mutableStateOf(false) }

                    OutlinedButton(onClick = { monthExpanded = true }) {
                        Text(Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.getDefault()))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select month"
                        )
                    }

                    DropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        for (month in 1..12) {
                            DropdownMenuItem(onClick = {
                                selectedMonth = month
                                monthExpanded = false
                            }) {
                                Text(Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()))
                            }
                        }
                    }
                }

                Box {
                    var yearExpanded by remember { mutableStateOf(false) }

                    OutlinedButton(onClick = { yearExpanded = true }) {
                        Text(selectedYear.toString())
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select year"
                        )
                    }

                    DropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        for (year in currentYear - 2..currentYear + 1) {
                            DropdownMenuItem(onClick = {
                                selectedYear = year
                                yearExpanded = false
                            }) {
                                Text(year.toString())
                            }
                        }
                    }
                }

                // Search
                SimpleSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search employees...",
                    modifier = Modifier.weight(1f)
                )

                // Filter buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        text = "Processed",
                        selected = showProcessed,
                        onSelected = { showProcessed = it },
                        color = AppColors.Success
                    )

                    FilterChip(
                        text = "Pending",
                        selected = showPending,
                        onSelected = { showPending = it },
                        color = AppColors.Warning
                    )
                }
            }

            if (payrolls.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No payroll records found for this period",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (employees.isNotEmpty()) {
                            Button(
                                onClick = { generatePayrolls() },
                                enabled = !isGeneratingPayrolls
                            ) {
                                if (isGeneratingPayrolls) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colors.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Payroll Entries")
                            }
                        }
                    }
                }
            } else {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Employee",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(0.25f)
                    )
                    Text(
                        text = "Department",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(0.15f)
                    )
                    Text(
                        text = "Salary",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(0.15f)
                    )
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(0.15f)
                    )
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(0.15f)
                    )
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(0.15f)
                    )
                }

                // Table Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPayrolls) { payroll ->
                        val employee = employees.find { it.id == payroll.employeeId }

                        if (employee != null) {
                            PayrollRow(
                                payroll = payroll,
                                employee = employee,
                                isProcessing = processingEmployeeId == employee.id,
                                onProcess = { processEmployeePayroll(employee.id) },
                                onEmployeeDetails = { onEmployeeDetails(NavDestination.EmployeeEdit(employee.id)) },
                                onPayrollDetails = { onNavigate(NavDestination.PayrollDetails(payroll.id)) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
        
        // Show toast message
        Toast(
            toast = toast,
            onDismiss = { toast = null }
        )
    }
}

@Composable
fun PayrollRow(
    payroll: EmployeePayroll,
    employee: Employee,
    isProcessing: Boolean,
    onProcess: () -> Unit,
    onEmployeeDetails: () -> Unit,
    onPayrollDetails: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Employee
        Column(
            modifier = Modifier.weight(0.25f)
        ) {
            Text(
                text = "${employee.firstName} ${employee.lastName}",
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = employee.employeeId,
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Department
        Text(
            text = employee.department,
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.15f)
        )

        // Salary
        Text(
            text = "₹${String.format("%,.2f", payroll.netSalary)}",
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.15f)
        )

        // Status
        Box(
            modifier = Modifier
                .weight(0.15f)
                .padding(end = 8.dp)
        ) {
            val (backgroundColor, text) = when (payroll.paymentStatus) {
                PaymentStatus.PAID -> Pair(AppColors.Success, "Processed")
                PaymentStatus.PENDING -> Pair(AppColors.Warning, "Pending")
                PaymentStatus.PROCESSING -> Pair(AppColors.Error, "Failed")
                else -> Pair(AppColors.Gray400, "Unknown")
            }

            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.caption,
                    color = backgroundColor
                )
            }
        }

        // Processing Date
        Text(
            text = if (payroll.paymentDate != null) {
                val date = Instant.ofEpochMilli(payroll.paymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
                date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            } else {
                "-"
            },
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.15f)
        )

        // Actions
        Row(
            modifier = Modifier.weight(0.15f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onEmployeeDetails) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Employee details",
                    tint = MaterialTheme.colors.primary
                )
            }

            if (payroll.paymentStatus != PaymentStatus.PAID) {
                IconButton(
                    onClick = onProcess,
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Process payroll",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onSelected: (Boolean) -> Unit,
    color: Color
) {
    OutlinedButton(
        onClick = { onSelected(!selected) },
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = if (selected) color.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (selected) color else MaterialTheme.colors.onSurface
        ),
        border = ButtonDefaults.outlinedBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                if (selected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text)
    }
} 
