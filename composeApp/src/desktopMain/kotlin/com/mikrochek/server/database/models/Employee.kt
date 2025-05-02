package com.mikrochek.server.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    // Basic employment details
    val designation: String,
    val department: String,
    val joiningDate: Long,
    // Payroll information
    val basicSalary: Double,
    val allowances: Double = 0.0,
    val deductions: Double = 0.0,
    // Address as simple fields instead of nested object
    val address: String,
    val city: String,
    val state: String,
    val postalCode: String,
    // Status and audit
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
)

@Serializable
data class EmployeePayroll(
    val id: String,
    val employeeId: String,
    val month: Int,
    val year: Int,
    val basicSalary: Double,
    val allowances: Double,
    val deductions: Double,
    val netSalary: Double,
    val paymentDate: Long?,
    val paymentStatus: PaymentStatus,
    val remarks: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
)

@Serializable
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED
} 