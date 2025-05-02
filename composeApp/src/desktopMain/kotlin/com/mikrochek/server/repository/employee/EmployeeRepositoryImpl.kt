package com.mikrochek.server.repository.employee

import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.database.models.EmployeePayroll
import com.mikrochek.server.database.models.PaymentStatus
import java.time.YearMonth
import java.util.UUID

class EmployeeRepositoryImpl : EmployeeRepository {
    private val employees = mutableMapOf<String, Employee>()
    private val payrolls = mutableMapOf<String, EmployeePayroll>()

    // Employee CRUD operations
    override fun createEmployee(employee: Employee): Result<Employee> = runCatching {
        val id = employee.id.ifEmpty { UUID.randomUUID().toString() }
        val newEmployee = employee.copy(id = id)
        employees[id] = newEmployee
        newEmployee
    }

    override fun updateEmployee(employee: Employee): Result<Employee> = runCatching {
        if (!employees.containsKey(employee.id)) {
            throw IllegalArgumentException("Employee not found with id: ${employee.id}")
        }
        employees[employee.id] = employee
        employee
    }

    override fun deleteEmployee(id: String): Result<Unit> = runCatching {
        if (!employees.containsKey(id)) {
            throw IllegalArgumentException("Employee not found with id: $id")
        }
        employees.remove(id)
    }

    override fun getEmployeeById(id: String): Employee? = employees[id]

    override fun getAllEmployees(isActive: Boolean): List<Employee> =
        employees.values.filter { it.isActive == isActive }

    override fun searchEmployees(query: String): List<Employee> =
        employees.values.filter { employee ->
            employee.employeeId.contains(query, ignoreCase = true) ||
            employee.firstName.contains(query, ignoreCase = true) ||
            employee.lastName.contains(query, ignoreCase = true) ||
            employee.email.contains(query, ignoreCase = true) ||
            employee.designation.contains(query, ignoreCase = true) ||
            employee.department.contains(query, ignoreCase = true)
        }

    override fun getEmployeesByDepartment(department: String): List<Employee> =
        employees.values.filter { it.department.equals(department, ignoreCase = true) }

    // Basic Payroll operations
    override fun createPayroll(payroll: EmployeePayroll): Result<EmployeePayroll> = runCatching {
        val id = payroll.id.ifEmpty { UUID.randomUUID().toString() }
        val newPayroll = payroll.copy(id = id)
        payrolls[id] = newPayroll
        newPayroll
    }

    override fun updatePayroll(payroll: EmployeePayroll): Result<EmployeePayroll> = runCatching {
        if (!payrolls.containsKey(payroll.id)) {
            throw IllegalArgumentException("Payroll not found with id: ${payroll.id}")
        }
        payrolls[payroll.id] = payroll
        payroll
    }

    override fun getPayrollById(id: String): EmployeePayroll? = payrolls[id]

    override fun getEmployeePayrolls(employeeId: String): List<EmployeePayroll> =
        payrolls.values.filter { it.employeeId == employeeId }

    override fun getPayrollsByMonth(month: Int, year: Int): List<EmployeePayroll> =
        payrolls.values.filter { it.month == month && it.year == year }

    override fun getPayrollsByStatus(status: PaymentStatus): List<EmployeePayroll> =
        payrolls.values.filter { it.paymentStatus == status }

    override fun getPendingPayrolls(): List<EmployeePayroll> =
        payrolls.values.filter { it.paymentStatus == PaymentStatus.PENDING }

    override fun getFailedPayrolls(): List<EmployeePayroll> =
        payrolls.values.filter { it.paymentStatus == PaymentStatus.FAILED }

    // Enhanced Payroll operations
    override fun processPayroll(employeeId: String, month: Int, year: Int): Result<EmployeePayroll> = runCatching {
        val employee = employees[employeeId] ?: throw IllegalArgumentException("Employee not found")
        
        // Check if payroll already exists
        if (payrolls.values.any { it.employeeId == employeeId && it.month == month && it.year == year }) {
            throw IllegalStateException("Payroll already processed for this period")
        }

        val payroll = EmployeePayroll(
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

        payrolls[payroll.id] = payroll
        payroll
    }

    override fun bulkProcessPayroll(month: Int, year: Int): Result<List<EmployeePayroll>> = runCatching {
        employees.values
            .filter { it.isActive }
            .map { employee ->
                processPayroll(employee.id, month, year).getOrThrow()
            }
    }

    override fun retryFailedPayments(): Result<List<EmployeePayroll>> = runCatching {
        getFailedPayrolls().map { payroll ->
            val updated = payroll.copy(
                paymentStatus = PaymentStatus.PROCESSING,
                updatedAt = System.currentTimeMillis()
            )
            payrolls[payroll.id] = updated
            updated
        }
    }

    override fun markPayrollAsPaid(payrollId: String, paymentDate: Long): Result<EmployeePayroll> = runCatching {
        val payroll = payrolls[payrollId] ?: throw IllegalArgumentException("Payroll not found")
        val updated = payroll.copy(
            paymentStatus = PaymentStatus.PAID,
            paymentDate = paymentDate,
            updatedAt = System.currentTimeMillis()
        )
        payrolls[payrollId] = updated
        updated
    }

    // Analytics implementations
    override fun getTotalPayrollCost(month: Int, year: Int): Double =
        getPayrollsByMonth(month, year).sumOf { it.netSalary }

    override fun getAverageSalaryByDepartment(): Map<String, Double> =
        employees.values
            .groupBy { it.department }
            .mapValues { (_, employees) ->
                employees.map { it.basicSalary }.average()
            }

    override fun getPayrollTrendByMonth(startMonth: YearMonth, endMonth: YearMonth): Map<YearMonth, Double> {
        var current = startMonth
        val trend = mutableMapOf<YearMonth, Double>()
        
        while (!current.isAfter(endMonth)) {
            trend[current] = getTotalPayrollCost(current.monthValue, current.year)
            current = current.plusMonths(1)
        }
        
        return trend
    }

    override fun getTopEarners(limit: Int): List<Employee> =
        employees.values
            .sortedByDescending { it.basicSalary + it.allowances }
            .take(limit)

    override fun getPayrollStatusSummary(month: Int, year: Int): Map<PaymentStatus, Int> =
        getPayrollsByMonth(month, year)
            .groupBy { it.paymentStatus }
            .mapValues { it.value.size }

    override fun getDepartmentHeadcount(): Map<String, Int> =
        employees.values
            .groupBy { it.department }
            .mapValues { it.value.size }

    override fun getSalaryDistribution(): Map<String, Int> {
        val ranges = listOf(0..30000, 30001..50000, 50001..80000, 80001..100000, 100001..Int.MAX_VALUE)
        val labels = listOf("0-30k", "30k-50k", "50k-80k", "80k-100k", "100k+")
        
        return employees.values
            .groupBy { employee ->
                val salary = employee.basicSalary.toInt()
                val index = ranges.indexOfFirst { range -> salary in range }
                labels[index]
            }
            .mapValues { it.value.size }
    }
} 