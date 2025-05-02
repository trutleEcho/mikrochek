package com.mikrochek.server.repository.employee

import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.database.models.EmployeePayroll
import com.mikrochek.server.database.models.PaymentStatus
import java.time.YearMonth

interface EmployeeRepository {
    // Employee CRUD operations
    fun createEmployee(employee: Employee): Result<Employee>
    fun updateEmployee(employee: Employee): Result<Employee>
    fun deleteEmployee(id: String): Result<Unit>
    fun getEmployeeById(id: String): Employee?
    fun getAllEmployees(isActive: Boolean = true): List<Employee>
    fun searchEmployees(query: String): List<Employee>
    fun getEmployeesByDepartment(department: String): List<Employee>

    // Basic Payroll operations
    fun createPayroll(payroll: EmployeePayroll): Result<EmployeePayroll>
    fun updatePayroll(payroll: EmployeePayroll): Result<EmployeePayroll>
    fun getPayrollById(id: String): EmployeePayroll?
    fun getEmployeePayrolls(employeeId: String): List<EmployeePayroll>
    fun getPayrollsByMonth(month: Int, year: Int): List<EmployeePayroll>
    fun getPayrollsByStatus(status: PaymentStatus): List<EmployeePayroll>
    fun getPendingPayrolls(): List<EmployeePayroll>
    fun getFailedPayrolls(): List<EmployeePayroll>

    // Enhanced Payroll operations
    fun processPayroll(employeeId: String, month: Int, year: Int): Result<EmployeePayroll>
    fun bulkProcessPayroll(month: Int, year: Int): Result<List<EmployeePayroll>>
    fun retryFailedPayments(): Result<List<EmployeePayroll>>
    fun markPayrollAsPaid(payrollId: String, paymentDate: Long): Result<EmployeePayroll>

    // Analytics
    fun getTotalPayrollCost(month: Int, year: Int): Double
    fun getAverageSalaryByDepartment(): Map<String, Double>
    fun getPayrollTrendByMonth(startMonth: YearMonth, endMonth: YearMonth): Map<YearMonth, Double>
    fun getTopEarners(limit: Int = 10): List<Employee>
    fun getPayrollStatusSummary(month: Int, year: Int): Map<PaymentStatus, Int>
    fun getDepartmentHeadcount(): Map<String, Int>
    fun getSalaryDistribution(): Map<String, Int> // Ranges like "0-30k", "30k-50k", etc.
} 