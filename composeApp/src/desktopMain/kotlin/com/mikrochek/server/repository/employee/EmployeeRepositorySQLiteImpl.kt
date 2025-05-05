package com.mikrochek.server.repository.employee

import com.mikrochek.server.database.SQLiteDatabase
import com.mikrochek.server.database.models.Employee
import com.mikrochek.server.database.models.EmployeePayroll
import com.mikrochek.server.database.models.PaymentStatus
import com.mikrochek.utils.TimeUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.YearMonth
import java.util.*
import kotlin.random.Random

class EmployeeRepositorySQLiteImpl : EmployeeRepository {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; isLenient = true }
    
    init {
        // Initialize database tables if needed
        createTablesIfNotExist()
    }
    
    // Initialize database tables if they don't exist
    private fun createTablesIfNotExist() {
        try {
            val connection = SQLiteDatabase.getConnection()
            connection.createStatement().use { statement ->
                // Create employees table with a structure that matches our Employee model
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS employees_v2 (
                        id TEXT PRIMARY KEY,
                        employeeId TEXT UNIQUE NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        email TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        designation TEXT NOT NULL,
                        department TEXT NOT NULL,
                        joiningDate INTEGER NOT NULL,
                        basicSalary REAL NOT NULL,
                        allowances REAL NOT NULL,
                        deductions REAL NOT NULL,
                        address TEXT NOT NULL,
                        city TEXT NOT NULL,
                        state TEXT NOT NULL,
                        postalCode TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        updatedBy TEXT NOT NULL
                    )
                """)
                
                // Create payrolls table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS employee_payrolls (
                        id TEXT PRIMARY KEY,
                        employeeId TEXT NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        basicSalary REAL NOT NULL,
                        allowances REAL NOT NULL,
                        deductions REAL NOT NULL,
                        netSalary REAL NOT NULL,
                        paymentDate INTEGER,
                        paymentStatus TEXT NOT NULL,
                        remarks TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        updatedBy TEXT NOT NULL,
                        FOREIGN KEY(employeeId) REFERENCES employees_v2(id)
                    )
                """)
                
                // Create a unique index on employee_payrolls to ensure one payroll per employee per month/year
                statement.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS idx_employee_month_year 
                    ON employee_payrolls(employeeId, month, year)
                """)
            }
        } catch (e: SQLException) {
            println("Error creating tables: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Helper function to convert ResultSet to Employee
    private fun resultSetToEmployee(rs: ResultSet): Employee {
        return Employee(
            id = rs.getString("id"),
            employeeId = rs.getString("employeeId"),
            firstName = rs.getString("firstName"),
            lastName = rs.getString("lastName"),
            email = rs.getString("email"),
            phone = rs.getString("phone"),
            designation = rs.getString("designation"),
            department = rs.getString("department"),
            joiningDate = rs.getLong("joiningDate"),
            basicSalary = rs.getDouble("basicSalary"),
            allowances = rs.getDouble("allowances"),
            deductions = rs.getDouble("deductions"),
            address = rs.getString("address"),
            city = rs.getString("city"),
            state = rs.getString("state"),
            postalCode = rs.getString("postalCode"),
            isActive = rs.getInt("isActive") == 1,
            createdAt = rs.getLong("createdAt"),
            updatedAt = rs.getLong("updatedAt"),
            createdBy = rs.getString("createdBy"),
            updatedBy = rs.getString("updatedBy")
        )
    }
    
    // Helper function to convert ResultSet to EmployeePayroll
    private fun resultSetToPayroll(rs: ResultSet): EmployeePayroll {
        val paymentDate = rs.getLong("paymentDate")
        return EmployeePayroll(
            id = rs.getString("id"),
            employeeId = rs.getString("employeeId"),
            month = rs.getInt("month"),
            year = rs.getInt("year"),
            basicSalary = rs.getDouble("basicSalary"),
            allowances = rs.getDouble("allowances"),
            deductions = rs.getDouble("deductions"),
            netSalary = rs.getDouble("netSalary"),
            paymentDate = if (rs.wasNull()) null else paymentDate,
            paymentStatus = PaymentStatus.valueOf(rs.getString("paymentStatus")),
            remarks = rs.getString("remarks"),
            createdAt = rs.getLong("createdAt"),
            updatedAt = rs.getLong("updatedAt"),
            createdBy = rs.getString("createdBy"),
            updatedBy = rs.getString("updatedBy")
        )
    }
    
    // Helper function to prepare a statement with Employee data
    private fun prepareEmployeeStatement(stmt: PreparedStatement, employee: Employee) {
        stmt.setString(1, employee.id)
        stmt.setString(2, employee.employeeId)
        stmt.setString(3, employee.firstName)
        stmt.setString(4, employee.lastName)
        stmt.setString(5, employee.email)
        stmt.setString(6, employee.phone)
        stmt.setString(7, employee.designation)
        stmt.setString(8, employee.department)
        stmt.setLong(9, employee.joiningDate)
        stmt.setDouble(10, employee.basicSalary)
        stmt.setDouble(11, employee.allowances)
        stmt.setDouble(12, employee.deductions)
        stmt.setString(13, employee.address)
        stmt.setString(14, employee.city)
        stmt.setString(15, employee.state)
        stmt.setString(16, employee.postalCode)
        stmt.setInt(17, if (employee.isActive) 1 else 0)
        stmt.setLong(18, employee.createdAt)
        stmt.setLong(19, employee.updatedAt)
        stmt.setString(20, employee.createdBy)
        stmt.setString(21, employee.updatedBy)
    }
    
    // Helper function to prepare a statement with EmployeePayroll data
    private fun preparePayrollStatement(stmt: PreparedStatement, payroll: EmployeePayroll) {
        stmt.setString(1, payroll.id)
        stmt.setString(2, payroll.employeeId)
        stmt.setInt(3, payroll.month)
        stmt.setInt(4, payroll.year)
        stmt.setDouble(5, payroll.basicSalary)
        stmt.setDouble(6, payroll.allowances)
        stmt.setDouble(7, payroll.deductions)
        stmt.setDouble(8, payroll.netSalary)
        if (payroll.paymentDate != null) {
            stmt.setLong(9, payroll.paymentDate)
        } else {
            stmt.setNull(9, java.sql.Types.BIGINT)
        }
        stmt.setString(10, payroll.paymentStatus.name)
        stmt.setString(11, payroll.remarks)
        stmt.setLong(12, payroll.createdAt)
        stmt.setLong(13, payroll.updatedAt)
        stmt.setString(14, payroll.createdBy)
        stmt.setString(15, payroll.updatedBy)
    }

    // Employee CRUD Operations
    override fun createEmployee(employee: Employee): Result<Employee> {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            // Check if employee with same ID already exists
            connection.prepareStatement("SELECT id FROM employees_v2 WHERE id = ?").use { stmt ->
                stmt.setString(1, employee.id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    return Result.failure(IllegalArgumentException("Employee with ID ${employee.id} already exists"))
                }
            }
            
            // Check if employee with same employeeId already exists
            connection.prepareStatement("SELECT id FROM employees_v2 WHERE employeeId = ?").use { stmt ->
                stmt.setString(1, employee.employeeId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    return Result.failure(IllegalArgumentException("Employee with employeeId ${employee.employeeId} already exists"))
                }
            }
            
            // Insert the employee
            connection.prepareStatement("""
                INSERT INTO employees_v2 (
                    id, employeeId, firstName, lastName, email, phone, designation, department,
                    joiningDate, basicSalary, allowances, deductions, address, city, state,
                    postalCode, isActive, createdAt, updatedAt, createdBy, updatedBy
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                prepareEmployeeStatement(stmt, employee)
                stmt.executeUpdate()
            }
            
            return Result.success(employee)
        } catch (e: Exception) {
            println("Error creating employee: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override fun updateEmployee(employee: Employee): Result<Employee> {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            // Check if employee exists
            connection.prepareStatement("SELECT id FROM employees_v2 WHERE id = ?").use { stmt ->
                stmt.setString(1, employee.id)
                val rs = stmt.executeQuery()
                if (!rs.next()) {
                    return Result.failure(NoSuchElementException("Employee with ID ${employee.id} not found"))
                }
            }
            
            // Update the employee
            connection.prepareStatement("""
                UPDATE employees_v2 SET
                    employeeId = ?, firstName = ?, lastName = ?, email = ?, phone = ?,
                    designation = ?, department = ?, joiningDate = ?, basicSalary = ?,
                    allowances = ?, deductions = ?, address = ?, city = ?, state = ?,
                    postalCode = ?, isActive = ?, createdAt = ?, updatedAt = ?,
                    createdBy = ?, updatedBy = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, employee.employeeId)
                stmt.setString(2, employee.firstName)
                stmt.setString(3, employee.lastName)
                stmt.setString(4, employee.email)
                stmt.setString(5, employee.phone)
                stmt.setString(6, employee.designation)
                stmt.setString(7, employee.department)
                stmt.setLong(8, employee.joiningDate)
                stmt.setDouble(9, employee.basicSalary)
                stmt.setDouble(10, employee.allowances)
                stmt.setDouble(11, employee.deductions)
                stmt.setString(12, employee.address)
                stmt.setString(13, employee.city)
                stmt.setString(14, employee.state)
                stmt.setString(15, employee.postalCode)
                stmt.setInt(16, if (employee.isActive) 1 else 0)
                stmt.setLong(17, employee.createdAt)
                stmt.setLong(18, employee.updatedAt)
                stmt.setString(19, employee.createdBy)
                stmt.setString(20, employee.updatedBy)
                stmt.setString(21, employee.id)
                
                val rowsAffected = stmt.executeUpdate()
                if (rowsAffected == 0) {
                    return Result.failure(NoSuchElementException("Employee with ID ${employee.id} not found"))
                }
            }
            
            return Result.success(employee)
        } catch (e: Exception) {
            println("Error updating employee: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override fun deleteEmployee(id: String): Result<Unit> {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            // First, delete any payrolls associated with this employee
            connection.prepareStatement("DELETE FROM employee_payrolls WHERE employeeId = ?").use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate()
            }
            
            // Then delete the employee
            connection.prepareStatement("DELETE FROM employees_v2 WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rowsAffected = stmt.executeUpdate()
                if (rowsAffected == 0) {
                    return Result.failure(NoSuchElementException("Employee with ID $id not found"))
                }
            }
            
            return Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting employee: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override fun getEmployeeById(id: String): Employee? {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            connection.prepareStatement("SELECT * FROM employees_v2 WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    return resultSetToEmployee(rs)
                }
            }
            
            return null
        } catch (e: Exception) {
            println("Error getting employee by ID: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override fun getAllEmployees(isActive: Boolean): List<Employee> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val employees = mutableListOf<Employee>()
            
            val query = if (isActive) {
                "SELECT * FROM employees_v2 WHERE isActive = 1"
            } else {
                "SELECT * FROM employees_v2"
            }
            
            connection.prepareStatement(query).use { stmt ->
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    employees.add(resultSetToEmployee(rs))
                }
            }
            
            return employees
        } catch (e: Exception) {
            println("Error getting all employees: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun searchEmployees(query: String): List<Employee> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val employees = mutableListOf<Employee>()
            
            // Using LIKE with wildcards for partial matching
            connection.prepareStatement("""
                SELECT * FROM employees_v2 
                WHERE firstName LIKE ? OR lastName LIKE ? OR email LIKE ? 
                OR employeeId LIKE ? OR department LIKE ? OR designation LIKE ?
            """).use { stmt ->
                val searchPattern = "%$query%"
                stmt.setString(1, searchPattern)
                stmt.setString(2, searchPattern)
                stmt.setString(3, searchPattern)
                stmt.setString(4, searchPattern)
                stmt.setString(5, searchPattern)
                stmt.setString(6, searchPattern)
                
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    employees.add(resultSetToEmployee(rs))
                }
            }
            
            return employees
        } catch (e: Exception) {
            println("Error searching employees: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun getEmployeesByDepartment(department: String): List<Employee> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val employees = mutableListOf<Employee>()
            
            connection.prepareStatement("SELECT * FROM employees_v2 WHERE department = ?").use { stmt ->
                stmt.setString(1, department)
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    employees.add(resultSetToEmployee(rs))
                }
            }
            
            return employees
        } catch (e: Exception) {
            println("Error getting employees by department: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    // Basic Payroll Operations
    override fun createPayroll(payroll: EmployeePayroll): Result<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            // Check if payroll with same ID already exists
            connection.prepareStatement("SELECT id FROM employee_payrolls WHERE id = ?").use { stmt ->
                stmt.setString(1, payroll.id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    return Result.failure(IllegalArgumentException("Payroll with ID ${payroll.id} already exists"))
                }
            }
            
            // Insert the payroll
            connection.prepareStatement("""
                INSERT INTO employee_payrolls (
                    id, employeeId, month, year, basicSalary, allowances, deductions,
                    netSalary, paymentDate, paymentStatus, remarks, createdAt,
                    updatedAt, createdBy, updatedBy
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                preparePayrollStatement(stmt, payroll)
                stmt.executeUpdate()
            }
            
            return Result.success(payroll)
        } catch (e: Exception) {
            println("Error creating payroll: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override fun updatePayroll(payroll: EmployeePayroll): Result<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            // Check if payroll exists
            connection.prepareStatement("SELECT id FROM employee_payrolls WHERE id = ?").use { stmt ->
                stmt.setString(1, payroll.id)
                val rs = stmt.executeQuery()
                if (!rs.next()) {
                    return Result.failure(NoSuchElementException("Payroll with ID ${payroll.id} not found"))
                }
            }
            
            // Update the payroll
            connection.prepareStatement("""
                UPDATE employee_payrolls SET
                    employeeId = ?, month = ?, year = ?, basicSalary = ?,
                    allowances = ?, deductions = ?, netSalary = ?, paymentDate = ?,
                    paymentStatus = ?, remarks = ?, createdAt = ?, updatedAt = ?,
                    createdBy = ?, updatedBy = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, payroll.employeeId)
                stmt.setInt(2, payroll.month)
                stmt.setInt(3, payroll.year)
                stmt.setDouble(4, payroll.basicSalary)
                stmt.setDouble(5, payroll.allowances)
                stmt.setDouble(6, payroll.deductions)
                stmt.setDouble(7, payroll.netSalary)
                if (payroll.paymentDate != null) {
                    stmt.setLong(8, payroll.paymentDate)
                } else {
                    stmt.setNull(8, java.sql.Types.BIGINT)
                }
                stmt.setString(9, payroll.paymentStatus.name)
                stmt.setString(10, payroll.remarks)
                stmt.setLong(11, payroll.createdAt)
                stmt.setLong(12, payroll.updatedAt)
                stmt.setString(13, payroll.createdBy)
                stmt.setString(14, payroll.updatedBy)
                stmt.setString(15, payroll.id)
                
                val rowsAffected = stmt.executeUpdate()
                if (rowsAffected == 0) {
                    return Result.failure(NoSuchElementException("Payroll with ID ${payroll.id} not found"))
                }
            }
            
            return Result.success(payroll)
        } catch (e: Exception) {
            println("Error updating payroll: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override fun getPayrollById(id: String): EmployeePayroll? {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            connection.prepareStatement("SELECT * FROM employee_payrolls WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    return resultSetToPayroll(rs)
                }
            }
            
            return null
        } catch (e: Exception) {
            println("Error getting payroll by ID: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override fun getEmployeePayrolls(employeeId: String): List<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val payrolls = mutableListOf<EmployeePayroll>()
            
            connection.prepareStatement("SELECT * FROM employee_payrolls WHERE employeeId = ?").use { stmt ->
                stmt.setString(1, employeeId)
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    payrolls.add(resultSetToPayroll(rs))
                }
            }
            
            return payrolls
        } catch (e: Exception) {
            println("Error getting employee payrolls: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun getPayrollsByMonth(month: Int, year: Int): List<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val payrolls = mutableListOf<EmployeePayroll>()
            
            connection.prepareStatement("SELECT * FROM employee_payrolls WHERE month = ? AND year = ?").use { stmt ->
                stmt.setInt(1, month)
                stmt.setInt(2, year)
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    payrolls.add(resultSetToPayroll(rs))
                }
            }
            
            return payrolls
        } catch (e: Exception) {
            println("Error getting payrolls by month: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun getPayrollsByStatus(status: PaymentStatus): List<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val payrolls = mutableListOf<EmployeePayroll>()
            
            connection.prepareStatement("SELECT * FROM employee_payrolls WHERE paymentStatus = ?").use { stmt ->
                stmt.setString(1, status.name)
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    payrolls.add(resultSetToPayroll(rs))
                }
            }
            
            return payrolls
        } catch (e: Exception) {
            println("Error getting payrolls by status: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun getPendingPayrolls(): List<EmployeePayroll> {
        return getPayrollsByStatus(PaymentStatus.PENDING)
    }

    override fun getFailedPayrolls(): List<EmployeePayroll> {
        return getPayrollsByStatus(PaymentStatus.PROCESSING)
    }

    // New function to create pending payrolls without processing them
    fun createPendingPayrolls(month: Int, year: Int): Result<List<EmployeePayroll>> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val results = mutableListOf<EmployeePayroll>()
            
            // Get all active employees
            val activeEmployees = getAllEmployees(true)
            
            for (employee in activeEmployees) {
                // Check if a payroll already exists for this employee/month/year
                var existingPayroll: EmployeePayroll? = null
                
                connection.prepareStatement("""
                    SELECT * FROM employee_payrolls 
                    WHERE employeeId = ? AND month = ? AND year = ?
                """).use { stmt ->
                    stmt.setString(1, employee.id)
                    stmt.setInt(2, month)
                    stmt.setInt(3, year)
                    val rs = stmt.executeQuery()
                    
                    if (rs.next()) {
                        existingPayroll = resultSetToPayroll(rs)
                    }
                }
                
                // Only create a new payroll if one doesn't already exist
                if (existingPayroll == null) {
                    val newPayroll = EmployeePayroll(
                        id = UUID.randomUUID().toString(),
                        employeeId = employee.id,
                        month = month,
                        year = year,
                        basicSalary = employee.basicSalary,
                        allowances = employee.allowances,
                        deductions = employee.deductions,
                        netSalary = employee.basicSalary + employee.allowances - employee.deductions,
                        paymentDate = null,
                        paymentStatus = PaymentStatus.PENDING,
                        remarks = null,
                        createdAt = TimeUtils.getCurrentISTTimestamp(),
                        updatedAt = TimeUtils.getCurrentISTTimestamp(),
                        createdBy = "system",
                        updatedBy = "system"
                    )
                    
                    // Insert the new payroll
                    connection.prepareStatement("""
                        INSERT INTO employee_payrolls (
                            id, employeeId, month, year, basicSalary, allowances, deductions,
                            netSalary, paymentDate, paymentStatus, remarks, createdAt,
                            updatedAt, createdBy, updatedBy
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """).use { stmt ->
                        preparePayrollStatement(stmt, newPayroll)
                        stmt.executeUpdate()
                    }
                    
                    results.add(newPayroll)
                } else {
                    // Include existing payroll in results
                    results.add(existingPayroll)
                }
            }
            
            return Result.success(results)
        } catch (e: Exception) {
            println("Error creating pending payrolls: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    // Enhanced Payroll Operations
    override fun processPayroll(employeeId: String, month: Int, year: Int): Result<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val employee = getEmployeeById(employeeId) ?: 
                return Result.failure(NoSuchElementException("Employee not found"))
            
            // Find existing payroll or create a new one
            var payroll: EmployeePayroll? = null
            
            connection.prepareStatement("""
                SELECT * FROM employee_payrolls 
                WHERE employeeId = ? AND month = ? AND year = ?
            """).use { stmt ->
                stmt.setString(1, employeeId)
                stmt.setInt(2, month)
                stmt.setInt(3, year)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    payroll = resultSetToPayroll(rs)
                }
            }
            
            // Create new payroll if not found
            if (payroll == null) {
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
                    createdAt = TimeUtils.getCurrentISTTimestamp(),
                    updatedAt = TimeUtils.getCurrentISTTimestamp(),
                    createdBy = "system",
                    updatedBy = "system"
                )
                
                // Insert the new payroll
                connection.prepareStatement("""
                    INSERT INTO employee_payrolls (
                        id, employeeId, month, year, basicSalary, allowances, deductions,
                        netSalary, paymentDate, paymentStatus, remarks, createdAt,
                        updatedAt, createdBy, updatedBy
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """).use { stmt ->
                    preparePayrollStatement(stmt, newPayroll)
                    stmt.executeUpdate()
                }
                
                // Simulate processing
                Thread.sleep(500)
                
                // Process the payment
                val processedPayroll = newPayroll.copy(
                    paymentStatus = PaymentStatus.PAID,
                    paymentDate = TimeUtils.getCurrentISTTimestamp(),
                    updatedAt = TimeUtils.getCurrentISTTimestamp(),
                    remarks = "Payment successful"
                )
                
                // Update the payroll status
                connection.prepareStatement("""
                    UPDATE employee_payrolls SET
                        paymentStatus = ?, paymentDate = ?, updatedAt = ?, remarks = ?
                    WHERE id = ?
                """).use { stmt ->
                    stmt.setString(1, processedPayroll.paymentStatus.name)
                    stmt.setLong(2, processedPayroll.paymentDate!!)
                    stmt.setLong(3, processedPayroll.updatedAt)
                    stmt.setString(4, processedPayroll.remarks)
                    stmt.setString(5, processedPayroll.id)
                    stmt.executeUpdate()
                }
                
                return Result.success(processedPayroll)
            } 
            // Update existing payroll
            else {
                // Update to processing status
                val updatedPayroll = payroll.copy(
                    paymentStatus = PaymentStatus.PROCESSING,
                    updatedAt = TimeUtils.getCurrentISTTimestamp(),
                    remarks = "Processing payment"
                )
                
                connection.prepareStatement("""
                    UPDATE employee_payrolls SET
                        paymentStatus = ?, updatedAt = ?, remarks = ?
                    WHERE id = ?
                """).use { stmt ->
                    stmt.setString(1, updatedPayroll.paymentStatus.name)
                    stmt.setLong(2, updatedPayroll.updatedAt)
                    stmt.setString(3, updatedPayroll.remarks)
                    stmt.setString(4, updatedPayroll.id)
                    stmt.executeUpdate()
                }
                
                // Simulate processing
                Thread.sleep(500)
                
                // Process the payment
                val finalPayroll = updatedPayroll.copy(
                    paymentStatus = PaymentStatus.PAID,
                    paymentDate = TimeUtils.getCurrentISTTimestamp(),
                    updatedAt = TimeUtils.getCurrentISTTimestamp(),
                    remarks = "Payment successful"
                )
                
                connection.prepareStatement("""
                    UPDATE employee_payrolls SET
                        paymentStatus = ?, paymentDate = ?, updatedAt = ?, remarks = ?
                    WHERE id = ?
                """).use { stmt ->
                    stmt.setString(1, finalPayroll.paymentStatus.name)
                    stmt.setLong(2, finalPayroll.paymentDate!!)
                    stmt.setLong(3, finalPayroll.updatedAt)
                    stmt.setString(4, finalPayroll.remarks)
                    stmt.setString(5, finalPayroll.id)
                    stmt.executeUpdate()
                }
                
                return Result.success(finalPayroll)
            }
        } catch (e: Exception) {
            println("Error processing payroll: ${e.message}")
            e.printStackTrace()
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
            println("Error processing bulk payroll: ${e.message}")
            e.printStackTrace()
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
            println("Error retrying failed payments: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override fun markPayrollAsPaid(payrollId: String, paymentDate: Long): Result<EmployeePayroll> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val payroll = getPayrollById(payrollId) ?: 
                return Result.failure(NoSuchElementException("Payroll not found"))
            
            val updatedPayroll = payroll.copy(
                paymentStatus = PaymentStatus.PAID,
                paymentDate = paymentDate,
                updatedAt = TimeUtils.getCurrentISTTimestamp(),
                remarks = "Manually marked as paid"
            )
            
            connection.prepareStatement("""
                UPDATE employee_payrolls SET
                    paymentStatus = ?, paymentDate = ?, updatedAt = ?, remarks = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, updatedPayroll.paymentStatus.name)
                stmt.setLong(2, updatedPayroll.paymentDate!!)
                stmt.setLong(3, updatedPayroll.updatedAt)
                stmt.setString(4, updatedPayroll.remarks)
                stmt.setString(5, updatedPayroll.id)
                
                val rowsAffected = stmt.executeUpdate()
                if (rowsAffected == 0) {
                    return Result.failure(NoSuchElementException("Payroll with ID ${updatedPayroll.id} not found"))
                }
            }
            
            return Result.success(updatedPayroll)
        } catch (e: Exception) {
            println("Error marking payroll as paid: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    // Analytics
    override fun getTotalPayrollCost(month: Int, year: Int): Double {
        try {
            val connection = SQLiteDatabase.getConnection()
            
            connection.prepareStatement("""
                SELECT SUM(netSalary) as totalCost FROM employee_payrolls 
                WHERE month = ? AND year = ?
            """).use { stmt ->
                stmt.setInt(1, month)
                stmt.setInt(2, year)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    return rs.getDouble("totalCost")
                }
            }
            
            return 0.0
        } catch (e: Exception) {
            println("Error getting total payroll cost: ${e.message}")
            e.printStackTrace()
            return 0.0
        }
    }

    override fun getAverageSalaryByDepartment(): Map<String, Double> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val result = mutableMapOf<String, Double>()
            
            connection.prepareStatement("""
                SELECT department, AVG(basicSalary + allowances - deductions) as avgSalary
                FROM employees_v2 
                WHERE isActive = 1
                GROUP BY department
            """).use { stmt ->
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    val department = rs.getString("department")
                    val avgSalary = rs.getDouble("avgSalary")
                    result[department] = avgSalary
                }
            }
            
            return result
        } catch (e: Exception) {
            println("Error getting average salary by department: ${e.message}")
            e.printStackTrace()
            return emptyMap()
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
        try {
            val connection = SQLiteDatabase.getConnection()
            val employees = mutableListOf<Employee>()
            
            connection.prepareStatement("""
                SELECT * FROM employees_v2 
                WHERE isActive = 1
                ORDER BY (basicSalary + allowances) DESC
                LIMIT ?
            """).use { stmt ->
                stmt.setInt(1, limit)
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    employees.add(resultSetToEmployee(rs))
                }
            }
            
            return employees
        } catch (e: Exception) {
            println("Error getting top earners: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun getPayrollStatusSummary(month: Int, year: Int): Map<PaymentStatus, Int> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val result = mutableMapOf<PaymentStatus, Int>()
            
            connection.prepareStatement("""
                SELECT paymentStatus, COUNT(*) as count
                FROM employee_payrolls 
                WHERE month = ? AND year = ?
                GROUP BY paymentStatus
            """).use { stmt ->
                stmt.setInt(1, month)
                stmt.setInt(2, year)
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    val status = PaymentStatus.valueOf(rs.getString("paymentStatus"))
                    val count = rs.getInt("count")
                    result[status] = count
                }
            }
            
            return result
        } catch (e: Exception) {
            println("Error getting payroll status summary: ${e.message}")
            e.printStackTrace()
            return emptyMap()
        }
    }

    override fun getDepartmentHeadcount(): Map<String, Int> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val result = mutableMapOf<String, Int>()
            
            connection.prepareStatement("""
                SELECT department, COUNT(*) as count
                FROM employees_v2 
                WHERE isActive = 1
                GROUP BY department
            """).use { stmt ->
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    val department = rs.getString("department")
                    val count = rs.getInt("count")
                    result[department] = count
                }
            }
            
            return result
        } catch (e: Exception) {
            println("Error getting department headcount: ${e.message}")
            e.printStackTrace()
            return emptyMap()
        }
    }

    override fun getSalaryDistribution(): Map<String, Int> {
        try {
            val connection = SQLiteDatabase.getConnection()
            val result = mutableMapOf<String, Int>()
            
            // Define the salary ranges
            val ranges = listOf(
                "0-30k" to 0.0..30000.0,
                "30k-50k" to 30000.0..50000.0,
                "50k-75k" to 50000.0..75000.0,
                "75k-100k" to 75000.0..100000.0,
                "100k+" to 100000.0..Double.MAX_VALUE
            )
            
            // Initialize all ranges with 0 count
            ranges.forEach { (label, _) -> result[label] = 0 }
            
            // Get all active employees
            connection.prepareStatement("""
                SELECT (basicSalary + allowances) as totalSalary
                FROM employees_v2 
                WHERE isActive = 1
            """).use { stmt ->
                val rs = stmt.executeQuery()
                
                while (rs.next()) {
                    val totalSalary = rs.getDouble("totalSalary")
                    
                    // Find the appropriate range and increment its count
                    ranges.find { (_, range) -> totalSalary in range }?.let { (label, _) ->
                        result[label] = (result[label] ?: 0) + 1
                    }
                }
            }
            
            return result
        } catch (e: Exception) {
            println("Error getting salary distribution: ${e.message}")
            e.printStackTrace()
            return emptyMap()
        }
    }
} 