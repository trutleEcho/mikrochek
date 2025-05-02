package com.mikrochek.server.repository.employee

import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.database.models.EmployeePayroll
import com.mikrochek.server.database.models.PaymentStatus
import java.time.YearMonth
import java.util.*
import kotlin.random.Random

class EmployeeRepositorySQLiteImpl : EmployeeRepository {
    private val employees = mutableListOf<Employee>()
    private val payrolls = mutableListOf<EmployeePayroll>()

    // Mock data
    init {
        // Add sample employees
        val departments = listOf("Engineering", "Sales", "Marketing", "HR", "Finance", "Operations")
        val designations = listOf("Manager", "Senior", "Junior", "Intern", "Lead", "Director")
        
        // Add sample employees
        repeat(20) { index ->
            val id = UUID.randomUUID().toString()
            val firstName = listOf("John", "Jane", "Mike", "Sarah", "David", "Lisa", "Robert", "Emily", "Michael", "Amanda").random()
            val lastName = listOf("Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor").random()
            val department = departments.random()
            val designation = designations.random()
            val isActive = Random.nextDouble() > 0.2
            val basicSalary = Random.nextDouble(20000.0, 100000.0)
            val allowances = basicSalary * Random.nextDouble(0.1, 0.3)
            val deductions = basicSalary * Random.nextDouble(0.05, 0.15)
            
            employees.add(
                Employee(
                    id = id,
                    employeeId = "EMP-${10000 + index}",
                    firstName = firstName,
                    lastName = lastName,
                    email = "${firstName.lowercase()}.${lastName.lowercase()}@example.com",
                    phone = "+91${Random.nextLong(7000000000, 9999999999)}",
                    designation = "$department $designation",
                    department = department,
                    joiningDate = System.currentTimeMillis() - Random.nextLong(100, 1000) * 86400000,
                    basicSalary = basicSalary,
                    allowances = allowances,
                    deductions = deductions,
                    address = "${Random.nextInt(100, 999)} Main Street",
                    city = listOf("Mumbai", "Delhi", "Bangalore", "Chennai", "Kolkata").random(),
                    state = listOf("Maharashtra", "Karnataka", "Tamil Nadu", "West Bengal", "Delhi").random(),
                    postalCode = "${Random.nextInt(100000, 999999)}",
                    isActive = isActive,
                    createdAt = System.currentTimeMillis() - Random.nextLong(100, 365) * 86400000,
                    updatedAt = System.currentTimeMillis(),
                    createdBy = "admin",
                    updatedBy = "admin"
                )
            )
            
            // Generate some payrolls for this employee
            val currentMonth = java.time.LocalDate.now().monthValue
            val currentYear = java.time.LocalDate.now().year
            
            // Generate payrolls for the last 3 months
            for (monthOffset in 0..2) {
                val month = if (currentMonth - monthOffset <= 0) 
                    currentMonth - monthOffset + 12 
                else 
                    currentMonth - monthOffset
                
                val year = if (currentMonth - monthOffset <= 0)
                    currentYear - 1
                else
                    currentYear
                
                val payrollId = UUID.randomUUID().toString()
                val status = when {
                    monthOffset == 0 -> listOf(PaymentStatus.PENDING, PaymentStatus.PROCESSING).random()
                    monthOffset == 1 -> listOf(PaymentStatus.PAID, PaymentStatus.FAILED).random()
                    else -> PaymentStatus.PAID
                }
                
                payrolls.add(
                    EmployeePayroll(
                        id = payrollId,
                        employeeId = id,
                        month = month,
                        year = year,
                        basicSalary = basicSalary,
                        allowances = allowances,
                        deductions = deductions,
                        netSalary = basicSalary + allowances - deductions,
                        paymentDate = if (status == PaymentStatus.PAID) 
                            System.currentTimeMillis() - Random.nextLong(1, 15) * 86400000 
                        else 
                            null,
                        paymentStatus = status,
                        remarks = if (status == PaymentStatus.FAILED) "Transaction failed" else null,
                        createdAt = System.currentTimeMillis() - Random.nextLong(1, 30) * 86400000,
                        updatedAt = System.currentTimeMillis(),
                        createdBy = "admin",
                        updatedBy = "admin"
                    )
                )
            }
        }
    }
    
    // Employee CRUD Operations
    override fun createEmployee(employee: Employee): Result<Employee> {
        try {
            employees.add(employee)
            return Result.success(employee)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun updateEmployee(employee: Employee): Result<Employee> {
        try {
            val index = employees.indexOfFirst { it.id == employee.id }
            if (index != -1) {
                employees[index] = employee
                return Result.success(employee)
            }
            return Result.failure(NoSuchElementException("Employee not found"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun deleteEmployee(id: String): Result<Unit> {
        try {
            employees.removeIf { it.id == id }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun getEmployeeById(id: String): Employee? {
        return employees.find { it.id == id }
    }

    override fun getAllEmployees(isActive: Boolean): List<Employee> {
        return if (isActive) {
            employees.filter { it.isActive }
        } else {
            employees
        }
    }

    override fun searchEmployees(query: String): List<Employee> {
        return employees.filter {
            it.firstName.contains(query, ignoreCase = true) ||
            it.lastName.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.employeeId.contains(query, ignoreCase = true) ||
            it.department.contains(query, ignoreCase = true) ||
            it.designation.contains(query, ignoreCase = true)
        }
    }

    override fun getEmployeesByDepartment(department: String): List<Employee> {
        return employees.filter { it.department == department }
    }

    // Basic Payroll Operations
    override fun createPayroll(payroll: EmployeePayroll): Result<EmployeePayroll> {
        try {
            payrolls.add(payroll)
            return Result.success(payroll)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun updatePayroll(payroll: EmployeePayroll): Result<EmployeePayroll> {
        try {
            val index = payrolls.indexOfFirst { it.id == payroll.id }
            if (index != -1) {
                payrolls[index] = payroll
                return Result.success(payroll)
            }
            return Result.failure(NoSuchElementException("Payroll not found"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun getPayrollById(id: String): EmployeePayroll? {
        return payrolls.find { it.id == id }
    }

    override fun getEmployeePayrolls(employeeId: String): List<EmployeePayroll> {
        return payrolls.filter { it.employeeId == employeeId }
    }

    override fun getPayrollsByMonth(month: Int, year: Int): List<EmployeePayroll> {
        return payrolls.filter { it.month == month && it.year == year }
    }

    override fun getPayrollsByStatus(status: PaymentStatus): List<EmployeePayroll> {
        return payrolls.filter { it.paymentStatus == status }
    }

    override fun getPendingPayrolls(): List<EmployeePayroll> {
        return payrolls.filter { it.paymentStatus == PaymentStatus.PENDING }
    }

    override fun getFailedPayrolls(): List<EmployeePayroll> {
        return payrolls.filter { it.paymentStatus == PaymentStatus.FAILED }
    }

    // Enhanced Payroll Operations
    override fun processPayroll(employeeId: String, month: Int, year: Int): Result<EmployeePayroll> {
        try {
            val employee = getEmployeeById(employeeId) ?: 
                return Result.failure(NoSuchElementException("Employee not found"))
            
            // Find existing payroll or create a new one
            val payrollIndex = payrolls.indexOfFirst { 
                it.employeeId == employeeId && it.month == month && it.year == year
            }
            
            // Create new payroll if not found
            if (payrollIndex == -1) {
                val newPayroll = EmployeePayroll(
                    id = UUID.randomUUID().toString(),
                    employeeId = employeeId,
                    month = month,
                    year = year,
                    basicSalary = employee.basicSalary,
                    allowances = employee.allowances,
                    deductions = employee.deductions,
                    netSalary = employee.basicSalary + employee.allowances - employee.deductions,
                    paymentDate = null,
                    paymentStatus = PaymentStatus.PENDING,
                    remarks = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    createdBy = "system",
                    updatedBy = "system"
                )
                payrolls.add(newPayroll)
                
                // Simulate processing
                Thread.sleep(500)
                
                // 90% success rate for simulation
                val success = Random.nextDouble() < 0.9
                
                val processedPayroll = if (success) {
                    newPayroll.copy(
                        paymentStatus = PaymentStatus.PAID,
                        paymentDate = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        remarks = "Payment successful"
                    )
                } else {
                    newPayroll.copy(
                        paymentStatus = PaymentStatus.FAILED,
                        updatedAt = System.currentTimeMillis(),
                        remarks = "Transaction failed: Insufficient funds"
                    )
                }
                
                // Replace the new payroll with the processed version
                val updatedIndex = payrolls.indexOfFirst { it.id == newPayroll.id }
                payrolls[updatedIndex] = processedPayroll
                
                return Result.success(processedPayroll)
            } 
            // Update existing payroll
            else {
                val existingPayroll = payrolls[payrollIndex]
                
                // Only retry if failed, otherwise return existing
                if (existingPayroll.paymentStatus != PaymentStatus.FAILED) {
                    return Result.success(existingPayroll)
                }
                
                // Update to processing status
                val updatedPayroll = existingPayroll.copy(
                    paymentStatus = PaymentStatus.PROCESSING,
                    updatedAt = System.currentTimeMillis(),
                    remarks = "Retrying payment"
                )
                payrolls[payrollIndex] = updatedPayroll
                
                // Simulate processing
                Thread.sleep(500)
                
                // 90% success rate for simulation
                val success = Random.nextDouble() < 0.9
                
                val finalPayroll = if (success) {
                    updatedPayroll.copy(
                        paymentStatus = PaymentStatus.PAID,
                        paymentDate = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        remarks = "Payment successful on retry"
                    )
                } else {
                    updatedPayroll.copy(
                        paymentStatus = PaymentStatus.FAILED,
                        updatedAt = System.currentTimeMillis(),
                        remarks = "Transaction failed again: Insufficient funds"
                    )
                }
                
                payrolls[payrollIndex] = finalPayroll
                return Result.success(finalPayroll)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun bulkProcessPayroll(month: Int, year: Int): Result<List<EmployeePayroll>> {
        try {
            val activeEmployees = getAllEmployees(true)
            val results = mutableListOf<EmployeePayroll>()
            
            for (employee in activeEmployees) {
                val result = processPayroll(employee.id, month, year)
                if (result.isSuccess) {
                    results.add(result.getOrThrow())
                }
            }
            
            return Result.success(results)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun retryFailedPayments(): Result<List<EmployeePayroll>> {
        try {
            val failedPayrolls = getFailedPayrolls()
            val results = mutableListOf<EmployeePayroll>()
            
            for (payroll in failedPayrolls) {
                val result = processPayroll(payroll.employeeId, payroll.month, payroll.year)
                if (result.isSuccess) {
                    results.add(result.getOrThrow())
                }
            }
            
            return Result.success(results)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun markPayrollAsPaid(payrollId: String, paymentDate: Long): Result<EmployeePayroll> {
        try {
            val payroll = getPayrollById(payrollId) ?: 
                return Result.failure(NoSuchElementException("Payroll not found"))
            
            val updatedPayroll = payroll.copy(
                paymentStatus = PaymentStatus.PAID,
                paymentDate = paymentDate,
                updatedAt = System.currentTimeMillis(),
                remarks = "Manually marked as paid"
            )
            
            val index = payrolls.indexOfFirst { it.id == payrollId }
            payrolls[index] = updatedPayroll
            
            return Result.success(updatedPayroll)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // Analytics
    override fun getTotalPayrollCost(month: Int, year: Int): Double {
        return getPayrollsByMonth(month, year).sumOf { it.netSalary }
    }

    override fun getAverageSalaryByDepartment(): Map<String, Double> {
        return employees
            .filter { it.isActive }
            .groupBy { it.department }
            .mapValues { (_, employees) -> 
                employees.map { it.basicSalary + it.allowances - it.deductions }.average()
            }
    }

    override fun getPayrollTrendByMonth(startMonth: YearMonth, endMonth: YearMonth): Map<YearMonth, Double> {
        val result = mutableMapOf<YearMonth, Double>()
        var current = startMonth
        
        while (!current.isAfter(endMonth)) {
            val month = current.monthValue
            val year = current.year
            val totalCost = getTotalPayrollCost(month, year)
            result[current] = totalCost
            current = current.plusMonths(1)
        }
        
        return result
    }

    override fun getTopEarners(limit: Int): List<Employee> {
        return employees
            .filter { it.isActive }
            .sortedByDescending { it.basicSalary + it.allowances }
            .take(limit)
    }

    override fun getPayrollStatusSummary(month: Int, year: Int): Map<PaymentStatus, Int> {
        return getPayrollsByMonth(month, year)
            .groupBy { it.paymentStatus }
            .mapValues { it.value.size }
    }

    override fun getDepartmentHeadcount(): Map<String, Int> {
        return employees
            .filter { it.isActive }
            .groupBy { it.department }
            .mapValues { it.value.size }
    }

    override fun getSalaryDistribution(): Map<String, Int> {
        val ranges = listOf(
            "0-30k" to 0.0..30000.0,
            "30k-50k" to 30000.0..50000.0,
            "50k-75k" to 50000.0..75000.0,
            "75k-100k" to 75000.0..100000.0,
            "100k+" to 100000.0..Double.MAX_VALUE
        )
        
        return ranges.associate { (label, range) ->
            label to employees.count { 
                it.isActive && (it.basicSalary + it.allowances) in range 
            }
        }
    }
} 