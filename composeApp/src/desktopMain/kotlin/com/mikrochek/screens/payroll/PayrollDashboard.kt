package com.mikrochek.screens.payroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mikrochek.components.QuickStatCard
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.PaymentStatus
import com.mikrochek.server.repository.employee.EmployeeRepository
import com.mikrochek.theme.AppColors
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.getValue

@Composable
fun PayrollDashboard(
    onNavigate: (NavDestination) -> Unit,
) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember { currentDate.monthValue }
    val currentYear = remember { currentDate.year }
    val employeeRepository: EmployeeRepository by GlobalContext.get().inject()

    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Payroll stats
    var totalPayroll by remember { mutableStateOf(0.0) }
    var processedCount by remember { mutableStateOf(0) }
    var pendingCount by remember { mutableStateOf(0) }
    var failedCount by remember { mutableStateOf(0) }
    var employeeCount by remember { mutableStateOf(0) }
    var departmentStats by remember { mutableStateOf(mapOf<String, Double>()) }
    
    // Load payroll data
    LaunchedEffect(selectedMonth, selectedYear) {
        isLoading = true
        
        // Get all active employees
        val employees = employeeRepository.getAllEmployees(true)
        employeeCount = employees.size
        
        // Get payroll summary for selected month
        val payrolls = employeeRepository.getPayrollsByMonth(selectedMonth, selectedYear)
        
        // Calculate statistics
        totalPayroll = employeeRepository.getTotalPayrollCost(selectedMonth, selectedYear)
        
        val statusSummary = employeeRepository.getPayrollStatusSummary(selectedMonth, selectedYear)
        processedCount = statusSummary[PaymentStatus.PAID] ?: 0
        pendingCount = statusSummary[PaymentStatus.PENDING] ?: 0
        failedCount = statusSummary[PaymentStatus.FAILED] ?: 0
        
        // Department salary distribution
        departmentStats = employeeRepository.getAverageSalaryByDepartment()
        
        isLoading = false
    }
    
    fun processPayroll() {
        scope.launch {
            isProcessing = true
            val result = employeeRepository.bulkProcessPayroll(selectedMonth, selectedYear)
            if (result.isSuccess) {
                // Refresh stats
                val payrolls = employeeRepository.getPayrollsByMonth(selectedMonth, selectedYear)
                val statusSummary = employeeRepository.getPayrollStatusSummary(selectedMonth, selectedYear)
                processedCount = statusSummary[PaymentStatus.PAID] ?: 0
                pendingCount = statusSummary[PaymentStatus.PENDING] ?: 0
                failedCount = statusSummary[PaymentStatus.FAILED] ?: 0
                totalPayroll = employeeRepository.getTotalPayrollCost(selectedMonth, selectedYear)
            }
            isProcessing = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        PageHeader(
            title = "Payroll Dashboard",
            subtitle = "Manage and process employee payrolls",
            icon = Icons.Default.Payments
        )
        
        // Month and year selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month/Year selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payroll Period:", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.width(16.dp))
                
                // Month dropdown
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Year dropdown
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
                        for (year in currentYear-2..currentYear+1) {
                            DropdownMenuItem(onClick = {
                                selectedYear = year
                                yearExpanded = false
                            }) {
                                Text(year.toString())
                            }
                        }
                    }
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionButton(
                    onClick = { onNavigate(NavDestination.PayrollProcessing) },
                    text = "Process Payroll",
                    icon = Icons.Default.CreditCard
                )
                
                if (failedCount > 0) {
                    ActionButton(
                        onClick = {
                            scope.launch {
                                employeeRepository.retryFailedPayments()
                                // Refresh data
                                val statusSummary = employeeRepository.getPayrollStatusSummary(selectedMonth, selectedYear)
                                processedCount = statusSummary[PaymentStatus.PAID] ?: 0
                                pendingCount = statusSummary[PaymentStatus.PENDING] ?: 0
                                failedCount = statusSummary[PaymentStatus.FAILED] ?: 0
                            }
                        },
                        text = "Retry Failed Payments",
                        icon = Icons.Default.Refresh,
                    )
                }
            }
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Dashboard content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickStatCard(
                        title = "Total Payroll",
                        value = "₹${String.format("%,.2f", totalPayroll)}",
                        icon = Icons.Default.AccountBalance,
                        backgroundColor = AppColors.Primary,
                        name = "Pay"
                    )
                    
                    QuickStatCard(
                        title = "Active Employees",
                        value = employeeCount.toString(),
                        icon = Icons.Default.People,
                        backgroundColor = AppColors.Accent1,
                        name = "Active Employees"
                    )
                    
                    QuickStatCard(
                        title = "Average Salary",
                        value = if (employeeCount > 0) "₹${String.format("%,.2f", totalPayroll / employeeCount)}" else "₹0.00",
                        icon = Icons.Default.Person,
                        backgroundColor = AppColors.Secondary,
                        name = "Avg"
                    )
                }
                
                // Payroll status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Payroll Status - ${Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.getDefault())} $selectedYear",
                            style = MaterialTheme.typography.h6
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatusCard(
                                title = "Processed",
                                count = processedCount,
                                color = AppColors.Success,
                                modifier = Modifier.weight(1f)
                            )
                            
                            StatusCard(
                                title = "Pending",
                                count = pendingCount,
                                color = AppColors.Warning,
                                modifier = Modifier.weight(1f)
                            )
                            
                            StatusCard(
                                title = "Failed",
                                count = failedCount,
                                color = AppColors.Error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = if (employeeCount > 0) processedCount.toFloat() / employeeCount else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AppColors.Success
                        )
                        
                        Text(
                            "${processedCount * 100 / if (employeeCount > 0) employeeCount else 1}% Complete",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
                
                // Department breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Department Salary Breakdown",
                            style = MaterialTheme.typography.h6
                        )
                        
                        // Department list
                        departmentStats.forEach { (department, avgSalary) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                color = getRandomColor(department),
                                                shape = RoundedCornerShape(50)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(department, style = MaterialTheme.typography.body1)
                                }
                                
                                Text(
                                    "₹${String.format("%,.2f", avgSalary)}",
                                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Divider()
                        }
                    }
                }
                
                // Bottom actions
                if (selectedMonth == currentMonth && selectedYear == currentYear) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        backgroundColor = AppColors.InfoLight
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AppColors.Info
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Process payroll for current month?",
                                    style = MaterialTheme.typography.body1,
                                    color = AppColors.Info
                                )
                            }
                            
                            Button(
                                onClick = { processPayroll() },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Info)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colors.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Process Now")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 2.dp,
        backgroundColor = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.subtitle2,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.h4,
                color = color
            )
        }
    }
}

// Helper function to generate consistent colors based on string input
fun getRandomColor(input: String): Color {
    val hue = (input.hashCode() % 360).toFloat()
    return Color.hsv(hue, 0.7f, 0.9f)
} 