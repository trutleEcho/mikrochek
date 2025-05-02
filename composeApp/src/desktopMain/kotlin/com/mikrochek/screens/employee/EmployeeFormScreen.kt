package com.mikrochek.screens.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikrochek.components.ActionButton
import com.mikrochek.components.ToastType
import com.mikrochek.components.layout.PageHeader
import com.mikrochek.navigation.NavDestination
import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.repository.employee.EmployeeRepository
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.*

@Composable
fun EmployeeFormScreen(
    employeeId: String?,
    onNavigate: (NavDestination) -> Unit,
    showToast: (String, ToastType) -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEditMode = employeeId != null
    var isLoading by remember { mutableStateOf(isEditMode) }
    var isSaving by remember { mutableStateOf(false) }
    val employeeRepository: EmployeeRepository by GlobalContext.get().inject()

    // Form state
    var empId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var joiningDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var basicSalary by remember { mutableStateOf("0.0") }
    var allowances by remember { mutableStateOf("0.0") }
    var deductions by remember { mutableStateOf("0.0") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    // Form validation
    val formErrors = remember { mutableStateMapOf<String, String>() }

    // Validation functions
    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email)
    }

    fun validatePhone(phone: String): Boolean {
        val phoneRegex = Regex("^[0-9+()-]{10,15}$")
        return phoneRegex.matches(phone)
    }

    fun validateNumber(value: String): Boolean {
        return try {
            value.toDoubleOrNull()?.let { it >= 0 } ?: false
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateForm(): Boolean {
        formErrors.clear()

        if (firstName.isBlank()) formErrors["firstName"] = "First name is required"
        if (lastName.isBlank()) formErrors["lastName"] = "Last name is required"
        if (email.isBlank()) formErrors["email"] = "Email is required"
        else if (!validateEmail(email)) formErrors["email"] = "Invalid email format"
        if (phone.isBlank()) formErrors["phone"] = "Phone is required"
        else if (!validatePhone(phone)) formErrors["phone"] = "Invalid phone number"
        if (department.isBlank()) formErrors["department"] = "Department is required"
        if (designation.isBlank()) formErrors["designation"] = "Designation is required"
        if (!validateNumber(basicSalary)) formErrors["basicSalary"] = "Invalid basic salary"
        if (!validateNumber(allowances)) formErrors["allowances"] = "Invalid allowances"
        if (!validateNumber(deductions)) formErrors["deductions"] = "Invalid deductions"
        if (address.isBlank()) formErrors["address"] = "Address is required"
        if (city.isBlank()) formErrors["city"] = "City is required"
        if (state.isBlank()) formErrors["state"] = "State is required"
        if (postalCode.isBlank()) formErrors["postalCode"] = "Postal code is required"

        return formErrors.isEmpty()
    }

    // Load employee data if in edit mode
    LaunchedEffect(employeeId) {
        if (isEditMode && employeeId != null) {
            isLoading = true
            employeeRepository.getEmployeeById(employeeId)?.let { employee ->
                empId = employee.employeeId
                firstName = employee.firstName
                lastName = employee.lastName
                email = employee.email
                phone = employee.phone
                department = employee.department
                designation = employee.designation
                joiningDate = employee.joiningDate
                basicSalary = employee.basicSalary.toString()
                allowances = employee.allowances.toString()
                deductions = employee.deductions.toString()
                address = employee.address
                city = employee.city
                state = employee.state
                postalCode = employee.postalCode
                isActive = employee.isActive
            }
            isLoading = false
        } else {
            empId = "EMP-${System.currentTimeMillis().toString().takeLast(6)}"
        }
    }

    // Save employee function
    fun saveEmployee() {
        if (!validateForm()) {
            showToast("Please fix the form errors", ToastType.ERROR)
            return
        }

        scope.launch {
            isSaving = true
            val employee = Employee(
                id = employeeId ?: UUID.randomUUID().toString(),
                employeeId = empId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                department = department,
                designation = designation,
                joiningDate = joiningDate,
                basicSalary = basicSalary.toDoubleOrNull() ?: 0.0,
                allowances = allowances.toDoubleOrNull() ?: 0.0,
                deductions = deductions.toDoubleOrNull() ?: 0.0,
                address = address,
                city = city,
                state = state,
                postalCode = postalCode,
                isActive = isActive,
                createdAt = if (isEditMode) employeeRepository.getEmployeeById(employeeId!!)?.createdAt 
                    ?: System.currentTimeMillis() else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                createdBy = if (isEditMode) employeeRepository.getEmployeeById(employeeId!!)?.createdBy 
                    ?: "system" else "system",
                updatedBy = "system"
            )

            val result = if (isEditMode) {
                employeeRepository.updateEmployee(employee)
            } else {
                employeeRepository.createEmployee(employee)
            }

            isSaving = false

            if (result.isSuccess) {
                showToast(
                    if (isEditMode) "Employee updated successfully" else "Employee created successfully",
                    ToastType.SUCCESS
                )
                onNavigate(NavDestination.EmployeesList)
            } else {
                showToast(
                    result.exceptionOrNull()?.message ?: "Failed to save employee",
                    ToastType.ERROR
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PageHeader(
            title = if (isEditMode) "Edit Employee" else "New Employee",
            subtitle = if (isEditMode) "Update employee information" else "Create a new employee record",
            icon = if (isEditMode) Icons.Default.Edit else Icons.Default.PersonAdd
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .verticalScroll(rememberScrollState())
//                    .padding(vertical = 16.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Basic Information Card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    elevation = 4.dp
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Text("Basic Information", style = MaterialTheme.typography.h6)
//
//                        OutlinedTextField(
//                            value = empId,
//                            onValueChange = {},
//                            label = { Text("Employee ID") },
//                            modifier = Modifier.fillMaxWidth(),
//                            readOnly = true,
//                            singleLine = true
//                        )
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            OutlinedTextField(
//                                value = firstName,
//                                onValueChange = { firstName = it },
//                                label = { Text("First Name") },
//                                modifier = Modifier.weight(1f),
//                                isError = "firstName" in formErrors,
//                                supportingText = formErrors["firstName"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//
//                            OutlinedTextField(
//                                value = lastName,
//                                onValueChange = { lastName = it },
//                                label = { Text("Last Name") },
//                                modifier = Modifier.weight(1f),
//                                isError = "lastName" in formErrors,
//                                supportingText = formErrors["lastName"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//                        }
//
//                        OutlinedTextField(
//                            value = email,
//                            onValueChange = { email = it },
//                            label = { Text("Email") },
//                            modifier = Modifier.fillMaxWidth(),
//                            isError = "email" in formErrors,
//                            supportingText = formErrors["email"]?.let { { Text(it) } },
//                            singleLine = true
//                        )
//
//                        OutlinedTextField(
//                            value = phone,
//                            onValueChange = { phone = it },
//                            label = { Text("Phone") },
//                            modifier = Modifier.fillMaxWidth(),
//                            isError = "phone" in formErrors,
//                            supportingText = formErrors["phone"]?.let { { Text(it) } },
//                            singleLine = true
//                        )
//                    }
//                }
//
//                // Employment Details Card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    elevation = 4.dp
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Text("Employment Details", style = MaterialTheme.typography.h6)
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            OutlinedTextField(
//                                value = department,
//                                onValueChange = { department = it },
//                                label = { Text("Department") },
//                                modifier = Modifier.weight(1f),
//                                isError = "department" in formErrors,
//                                supportingText = formErrors["department"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//
//                            OutlinedTextField(
//                                value = designation,
//                                onValueChange = { designation = it },
//                                label = { Text("Designation") },
//                                modifier = Modifier.weight(1f),
//                                isError = "designation" in formErrors,
//                                supportingText = formErrors["designation"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//                        }
//
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            Checkbox(
//                                checked = isActive,
//                                onCheckedChange = { isActive = it }
//                            )
//                            Text("Active Employee")
//                        }
//                    }
//                }
//
//                // Address Card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    elevation = 4.dp
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Text("Address Information", style = MaterialTheme.typography.h6)
//
//                        OutlinedTextField(
//                            value = address,
//                            onValueChange = { address = it },
//                            label = { Text("Address") },
//                            modifier = Modifier.fillMaxWidth(),
//                            isError = "address" in formErrors,
//                            supportingText = formErrors["address"]?.let { { Text(it) } }
//                        )
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            OutlinedTextField(
//                                value = city,
//                                onValueChange = { city = it },
//                                label = { Text("City") },
//                                modifier = Modifier.weight(1f),
//                                isError = "city" in formErrors,
//                                supportingText = formErrors["city"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//
//                            OutlinedTextField(
//                                value = state,
//                                onValueChange = { state = it },
//                                label = { Text("State") },
//                                modifier = Modifier.weight(1f),
//                                isError = "state" in formErrors,
//                                supportingText = formErrors["state"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//
//                            OutlinedTextField(
//                                value = postalCode,
//                                onValueChange = { postalCode = it },
//                                label = { Text("Postal Code") },
//                                modifier = Modifier.weight(0.8f),
//                                isError = "postalCode" in formErrors,
//                                supportingText = formErrors["postalCode"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//                        }
//                    }
//                }
//
//                // Salary Card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    elevation = 4.dp
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Text("Salary Information", style = MaterialTheme.typography.h6)
//
//                        OutlinedTextField(
//                            value = basicSalary,
//                            onValueChange = { basicSalary = it },
//                            label = { Text("Basic Salary (₹)") },
//                            modifier = Modifier.fillMaxWidth(),
//                            keyboardType = KeyboardType.Number,
//                            isError = "basicSalary" in formErrors,
//                            supportingText = formErrors["basicSalary"]?.let { { Text(it) } },
//                            singleLine = true
//                        )
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            OutlinedTextField(
//                                value = allowances,
//                                onValueChange = { allowances = it },
//                                label = { Text("Allowances (₹)") },
//                                modifier = Modifier.weight(1f),
//                                keyboardType = KeyboardType.Number,
//                                isError = "allowances" in formErrors,
//                                supportingText = formErrors["allowances"]?.let { { Text(it) } },
//                                singleLine = true
//                            )
//
//                            OutlinedTextField(
//                                value = deductions,
//                                onValueChange = { deductions = it },
//                                label = { "Deductions (₹)" },
//                                modifier = Modifier.weight(1f),
//                                isError = "deductions" in formErrors,
//                                supportingText = formErrors["deductions"]?.let { errorMessage ->
//                                    { errorMessage }
//                                },
//                                singleLine = true
//                            )
//                        }
//                    }
//                }
//
//                // Action Buttons
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
//                ) {
//                    ActionButton(
//                        text = "Cancel",
//                        onClick = { onNavigate(NavDestination.EmployeesList) },
//                        icon = TODO(),
//                        backgroundColor = TODO(),
//                        description = TODO()
//                    )
//
//                    ActionButton(
//                        text = if (isEditMode) "Update Employee" else "Create Employee",
//                        onClick = { saveEmployee() },
//                        icon = TODO(),
//                        backgroundColor = TODO(),
//                        description = TODO(),
//                    )
//                }
//            }
        }
    }
} 