package com.mikrochek.screens.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.repository.employee.EmployeeRepository
import com.mikrochek.theme.AppColors
import org.koin.core.context.GlobalContext
import kotlin.getValue

@Composable
fun EmployeeListScreen(
    onNavigate: (NavDestination) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var employees by remember { mutableStateOf(emptyList<Employee>()) }
    var filteredEmployees by remember { mutableStateOf(emptyList<Employee>()) }
    var isLoading by remember { mutableStateOf(true) }
    val employeeRepository: EmployeeRepository by GlobalContext.get().inject()

    // Sort options
    var sortByName by remember { mutableStateOf(true) }
    var sortAscending by remember { mutableStateOf(true) }
    
    // Load employees
    LaunchedEffect(Unit) {
        isLoading = true
        employees = employeeRepository.getAllEmployees()
        filteredEmployees = employees
        isLoading = false
    }
    
    // Filter employees when search query changes
    LaunchedEffect(searchQuery, employees) {
        filteredEmployees = if (searchQuery.isBlank()) {
            employees
        } else {
            employeeRepository.searchEmployees(searchQuery)
        }
        
        // Apply sorting
        filteredEmployees = if (sortByName) {
            if (sortAscending) {
                filteredEmployees.sortedBy { "${it.firstName} ${it.lastName}" }
            } else {
                filteredEmployees.sortedByDescending { "${it.firstName} ${it.lastName}" }
            }
        } else {
            if (sortAscending) {
                filteredEmployees.sortedBy { it.department }
            } else {
                filteredEmployees.sortedByDescending { it.department }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        PageHeader(
            title = "Employee Management",
            subtitle = "Manage your company's employees",
            icon = Icons.Default.Person
        )
        
        // Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            
            // Sort controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sort by:", style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { 
                        sortByName = true
                        sortAscending = !sortAscending 
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (sortByName) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Name ${if (sortByName && sortAscending) "↑" else if (sortByName) "↓" else ""}")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { 
                        sortByName = false
                        sortAscending = !sortAscending 
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (!sortByName) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Department ${if (!sortByName && sortAscending) "↑" else if (!sortByName) "↓" else ""}")
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Add Employee Button
            Box(
                modifier = Modifier.width(180.dp)
            ){
                ActionButton(
                    onClick = { onNavigate(NavDestination.EmployeeCreate) },
                    text = "Add Employee",
                    icon = Icons.Default.Add
                )
            }
        }
        
        // Employees Table
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredEmployees.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No employees found", style = MaterialTheme.typography.h6)
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
                    text = "Employee ID",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(0.15f)
                )
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(0.25f)
                )
                Text(
                    text = "Department",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(0.15f)
                )
                Text(
                    text = "Designation",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(0.2f)
                )
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(0.1f)
                )
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(0.15f)
                )
            }
            
            // Table Content
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(filteredEmployees) { employee ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Employee ID
                        Text(
                            text = employee.employeeId,
                            style = MaterialTheme.typography.body2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.15f)
                        )
                        
                        // Name
                        Text(
                            text = "${employee.firstName} ${employee.lastName}",
                            style = MaterialTheme.typography.body2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.25f)
                        )
                        
                        // Department
                        Text(
                            text = employee.department,
                            style = MaterialTheme.typography.body2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.15f)
                        )
                        
                        // Designation
                        Text(
                            text = employee.designation,
                            style = MaterialTheme.typography.body2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.2f)
                        )
                        
                        // Status
                        Box(
                            modifier = Modifier
                                .weight(0.1f)
                                .padding(end = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (employee.isActive) AppColors.Success.copy(alpha = 0.2f)
                                        else AppColors.Error.copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (employee.isActive) "Active" else "Inactive",
                                    style = MaterialTheme.typography.caption,
                                    color = if (employee.isActive) AppColors.Success else AppColors.Error
                                )
                            }
                        }
                        
                        // Actions
                        Row(
                            modifier = Modifier.weight(0.15f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onNavigate(NavDestination.EmployeeEdit(employee.id)) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit employee",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                            
                            IconButton(
                                onClick = { onNavigate(NavDestination.EmployeeDetails(employee.id)) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View employee details",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                    }
                    
                    Divider()
                }
            }
        }
    }
} 