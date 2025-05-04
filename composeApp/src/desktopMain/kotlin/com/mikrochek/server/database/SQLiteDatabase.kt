package com.mikrochek.server.database

import com.mikrochek.utils.TimeUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.io.File
import java.nio.file.Paths
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object SQLiteDatabase {
    private var connection: Connection? = null
    private val DB_DIR = Paths.get(System.getProperty("user.home"), "AppData", "Local", "MikroChek", "data").toString()
    private val DB_PATH = Paths.get(DB_DIR, "mikrochek.db").toString()

    init {
        try {
            println("Initializing SQLite database...")
            println("Database directory: $DB_DIR")
            println("Database path: $DB_PATH")
            
            // Create data directory if it doesn't exist
            val dbDir = File(DB_DIR)
            if (!dbDir.exists()) {
                println("Creating database directory...")
                dbDir.mkdirs()
            }
            println("Database directory exists: ${dbDir.exists()}")
            
            Class.forName("org.sqlite.JDBC")
            println("JDBC driver loaded successfully")
            createTables()
            println("Database tables created successfully")
        } catch (e: Exception) {
            println("Failed to initialize database: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Failed to initialize database", e)
        }
    }

    @Synchronized
    fun getConnection(): Connection {
        if (connection == null || connection?.isClosed == true) {
            println("Creating new database connection to: $DB_PATH")
            val dbFile = File(DB_PATH)
            println("Database file exists: ${dbFile.exists()}")
            connection = DriverManager.getConnection("jdbc:sqlite:$DB_PATH")
            connection?.autoCommit = true
            
            // Enable foreign key constraints
            connection?.createStatement()?.use { stmt ->
                stmt.execute("PRAGMA foreign_keys = ON;")
                println("Foreign key constraints enabled")
            }
            
            println("Database connection established successfully")
        }
        return connection!!
    }

    private fun createTables() {
        try {
            getConnection().createStatement().use { statement ->
                // Users table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        userName TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        accountType TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        lastLoginAt INTEGER,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // Insert default admin user if not exists
                statement.execute("""
                    INSERT OR IGNORE INTO users (
                        id, name, userName, password, accountType, createdAt, isActive
                    ) VALUES (
                        'admin', 'Admin User', 'admin', 'admin', 'ADMIN', 
                        ${TimeUtils.getCurrentISTTimestamp()}, 1
                    )
                """)

                // Employees table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        address TEXT NOT NULL,
                        designation TEXT NOT NULL,
                        department TEXT NOT NULL,
                        joiningDate INTEGER NOT NULL,
                        salary REAL NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        updatedBy TEXT NOT NULL,
                        FOREIGN KEY(createdBy) REFERENCES users(id),
                        FOREIGN KEY(updatedBy) REFERENCES users(id)
                    )
                """)

                // Products table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id TEXT PRIMARY KEY,
                        code TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        unit TEXT NOT NULL,
                        sellingPrice REAL NOT NULL,
                        costPrice REAL NOT NULL,
                        tax REAL NOT NULL,
                        minStock INTEGER NOT NULL,
                        currentStock INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        updatedBy TEXT NOT NULL,
                        FOREIGN KEY(createdBy) REFERENCES users(id),
                        FOREIGN KEY(updatedBy) REFERENCES users(id)
                    )
                """)

                // Quotations table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS quotations (
                        id TEXT PRIMARY KEY,
                        quotationNumber TEXT UNIQUE NOT NULL,
                        customerId TEXT NOT NULL,
                        customerName TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        validUntil INTEGER NOT NULL,
                        items TEXT NOT NULL,
                        subtotal REAL NOT NULL,
                        taxTotal REAL NOT NULL,
                        total REAL NOT NULL,
                        notes TEXT,
                        terms TEXT,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        updatedBy TEXT NOT NULL,
                        discountTotal REAL NOT NULL DEFAULT 0.0,
                        FOREIGN KEY(createdBy) REFERENCES users(id),
                        FOREIGN KEY(updatedBy) REFERENCES users(id)
                    )
                """)

                // Purchase Orders table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS purchase_orders (
                        id TEXT PRIMARY KEY,
                        poNumber TEXT UNIQUE NOT NULL,
                        supplierId TEXT NOT NULL,
                        supplierName TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        expectedDeliveryDate INTEGER NOT NULL,
                        items TEXT NOT NULL,
                        subtotal REAL NOT NULL,
                        taxTotal REAL NOT NULL,
                        total REAL NOT NULL,
                        notes TEXT,
                        terms TEXT,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        updatedBy TEXT NOT NULL,
                        FOREIGN KEY(createdBy) REFERENCES users(id),
                        FOREIGN KEY(updatedBy) REFERENCES users(id)
                    )
                """)

                // Documents table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS documents (
                        id TEXT PRIMARY KEY,
                        type TEXT NOT NULL,
                        number TEXT UNIQUE NOT NULL,
                        filePath TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdBy TEXT NOT NULL,
                        metadata TEXT NOT NULL,
                        FOREIGN KEY(createdBy) REFERENCES users(id)
                    )
                """)

                // Document counters table for auto-incrementing document numbers
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS document_counters (
                        type TEXT PRIMARY KEY,
                        lastNumber INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
            populateDummyData()
        } catch (e: SQLException) {
            e.printStackTrace()
            throw RuntimeException("Failed to create database tables", e)
        }
    }

    private fun populateDummyData() {
        try {
            val connection = getConnection()
            val json = Json { 
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }

            // Insert admin user
            connection.prepareStatement("""
                INSERT OR IGNORE INTO users (
                    id, name, userName, password, accountType, createdAt, isActive
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, "admin")
                stmt.setString(2, "Admin User")
                stmt.setString(3, "admin")
                stmt.setString(4, "admin") // In production, this should be hashed
                stmt.setString(5, "ADMIN")
                stmt.setLong(6, TimeUtils.getCurrentISTTimestamp())
                stmt.setInt(7, 1)
                stmt.executeUpdate()
            }

            // Sample employees
            val employees = listOf(
                mapOf(
                    "id" to "E001",
                    "name" to "John Doe",
                    "email" to "john.doe@mikrochek.com",
                    "phone" to "9876543210",
                    "address" to "123 Main St, City",
                    "designation" to "Sales Manager",
                    "department" to "Sales",
                    "joiningDate" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 365), // 1 year ago
                    "salary" to 50000.0,
                    "isActive" to 1,
                    "createdAt" to TimeUtils.getCurrentISTTimestamp(),
                    "updatedAt" to TimeUtils.getCurrentISTTimestamp(),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "E002",
                    "name" to "Jane Smith",
                    "email" to "jane.smith@mikrochek.com",
                    "phone" to "9876543211",
                    "address" to "456 Oak St, City",
                    "designation" to "Purchase Manager",
                    "department" to "Purchase",
                    "joiningDate" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 180), // 6 months ago
                    "salary" to 45000.0,
                    "isActive" to 1,
                    "createdAt" to TimeUtils.getCurrentISTTimestamp(),
                    "updatedAt" to TimeUtils.getCurrentISTTimestamp(),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "E003",
                    "name" to "Mike Johnson",
                    "email" to "mike.johnson@mikrochek.com",
                    "phone" to "9876543212",
                    "address" to "789 Pine St, City",
                    "designation" to "Store Manager",
                    "department" to "Store",
                    "joiningDate" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 90), // 3 months ago
                    "salary" to 40000.0,
                    "isActive" to 1,
                    "createdAt" to TimeUtils.getCurrentISTTimestamp(),
                    "updatedAt" to TimeUtils.getCurrentISTTimestamp(),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                )
            )

            // Insert employees
            employees.forEach { employee ->
                connection.prepareStatement("""
                    INSERT OR IGNORE INTO employees (
                        id, name, email, phone, address, designation,
                        department, joiningDate, salary, isActive,
                        createdAt, updatedAt, createdBy, updatedBy
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """).use { stmt ->
                    stmt.setString(1, employee["id"] as String)
                    stmt.setString(2, employee["name"] as String)
                    stmt.setString(3, employee["email"] as String)
                    stmt.setString(4, employee["phone"] as String)
                    stmt.setString(5, employee["address"] as String)
                    stmt.setString(6, employee["designation"] as String)
                    stmt.setString(7, employee["department"] as String)
                    stmt.setLong(8, employee["joiningDate"] as Long)
                    stmt.setDouble(9, employee["salary"] as Double)
                    stmt.setInt(10, employee["isActive"] as Int)
                    stmt.setLong(11, employee["createdAt"] as Long)
                    stmt.setLong(12, employee["updatedAt"] as Long)
                    stmt.setString(13, employee["createdBy"] as String)
                    stmt.setString(14, employee["updatedBy"] as String)
                    stmt.executeUpdate()
                }
            }

            // Sample products
            val products = listOf(
                mapOf(
                    "id" to "P001",
                    "code" to "P001",
                    "name" to "Stainless Steel Pipe",
                    "description" to "304 Grade, 1 inch diameter",
                    "category" to "Pipes",
                    "unit" to "Meter",
                    "sellingPrice" to 150.0,
                    "costPrice" to 100.0,
                    "tax" to 18.0,
                    "minStock" to 100,
                    "currentStock" to 500,
                    "isActive" to 1,
                    "createdAt" to TimeUtils.getCurrentISTTimestamp(),
                    "updatedAt" to TimeUtils.getCurrentISTTimestamp(),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "P002",
                    "code" to "P002",
                    "name" to "Copper Fitting",
                    "description" to "1/2 inch elbow",
                    "category" to "Fittings",
                    "unit" to "Piece",
                    "sellingPrice" to 25.0,
                    "costPrice" to 15.0,
                    "tax" to 18.0,
                    "minStock" to 50,
                    "currentStock" to 200,
                    "isActive" to 1,
                    "createdAt" to TimeUtils.getCurrentISTTimestamp(),
                    "updatedAt" to TimeUtils.getCurrentISTTimestamp(),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "P003",
                    "code" to "P003",
                    "name" to "PVC Valve",
                    "description" to "2 inch ball valve",
                    "category" to "Valves",
                    "unit" to "Piece",
                    "sellingPrice" to 120.0,
                    "costPrice" to 80.0,
                    "tax" to 18.0,
                    "minStock" to 20,
                    "currentStock" to 100,
                    "isActive" to 1,
                    "createdAt" to TimeUtils.getCurrentISTTimestamp(),
                    "updatedAt" to TimeUtils.getCurrentISTTimestamp(),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                )
            )

            // Insert products
            products.forEach { product ->
                connection.prepareStatement("""
                    INSERT OR IGNORE INTO products (
                        id, code, name, description, category, unit,
                        sellingPrice, costPrice, tax, minStock, currentStock,
                        isActive, createdAt, updatedAt, createdBy, updatedBy
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """).use { stmt ->
                    stmt.setString(1, product["id"] as String)
                    stmt.setString(2, product["code"] as String)
                    stmt.setString(3, product["name"] as String)
                    stmt.setString(4, product["description"] as String)
                    stmt.setString(5, product["category"] as String)
                    stmt.setString(6, product["unit"] as String)
                    stmt.setDouble(7, product["sellingPrice"] as Double)
                    stmt.setDouble(8, product["costPrice"] as Double)
                    stmt.setDouble(9, product["tax"] as Double)
                    stmt.setInt(10, product["minStock"] as Int)
                    stmt.setInt(11, product["currentStock"] as Int)
                    stmt.setInt(12, product["isActive"] as Int)
                    stmt.setLong(13, product["createdAt"] as Long)
                    stmt.setLong(14, product["updatedAt"] as Long)
                    stmt.setString(15, product["createdBy"] as String)
                    stmt.setString(16, product["updatedBy"] as String)
                    stmt.executeUpdate()
                }
            }

            // Sample quotations
            val quotations = listOf(
                mapOf(
                    "id" to "Q001",
                    "quotationNumber" to "QT-2024001",
                    "customerId" to "C001",
                    "customerName" to "ABC Industries",
                    "date" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 30), // 30 days ago
                    "validUntil" to (TimeUtils.getCurrentISTTimestamp() + 86400000 * 30), // 30 days from now
                    "items" to listOf(
                        mapOf(
                            "id" to "QI001",
                            "productId" to "P001",
                            "productCode" to "P001",
                            "productName" to "Stainless Steel Pipe",
                            "description" to "304 Grade, 1 inch diameter",
                            "quantity" to 10,
                            "unit" to "Meter",
                            "unitPrice" to 150.0,
                            "tax" to 18.0,
                            "taxAmount" to 270.0,
                            "discount" to 5.0,
                            "discountAmount" to 75.0,
                            "subtotal" to 1500.0,
                            "total" to 1695.0
                        )
                    ),
                    "subtotal" to 1500.0,
                    "discountTotal" to 75.0,
                    "taxTotal" to 270.0,
                    "total" to 1695.0,
                    "notes" to "Urgent delivery required",
                    "terms" to "Payment within 15 days",
                    "status" to "APPROVED",
                    "createdAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 30),
                    "updatedAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 30),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "Q002",
                    "quotationNumber" to "QT-2024002",
                    "customerId" to "C002",
                    "customerName" to "XYZ Corporation",
                    "date" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 15), // 15 days ago
                    "validUntil" to (TimeUtils.getCurrentISTTimestamp() + 86400000 * 15), // 15 days from now
                    "items" to listOf(
                        mapOf(
                            "id" to "QI002",
                            "productId" to "P002",
                            "productCode" to "P002",
                            "productName" to "Copper Fitting",
                            "description" to "1/2 inch elbow",
                            "quantity" to 50,
                            "unit" to "Piece",
                            "unitPrice" to 25.0,
                            "tax" to 18.0,
                            "taxAmount" to 225.0,
                            "discount" to 0.0,
                            "discountAmount" to 0.0,
                            "subtotal" to 1250.0,
                            "total" to 1475.0
                        ),
                        mapOf(
                            "id" to "QI003",
                            "productId" to "P003",
                            "productCode" to "P003",
                            "productName" to "PVC Valve",
                            "description" to "2 inch ball valve",
                            "quantity" to 5,
                            "unit" to "Piece",
                            "unitPrice" to 120.0,
                            "tax" to 18.0,
                            "taxAmount" to 108.0,
                            "discount" to 10.0,
                            "discountAmount" to 60.0,
                            "subtotal" to 600.0,
                            "total" to 648.0
                        )
                    ),
                    "subtotal" to 1850.0,
                    "discountTotal" to 60.0,
                    "taxTotal" to 333.0,
                    "total" to 2123.0,
                    "notes" to "Bulk order discount applied",
                    "terms" to "Payment within 30 days",
                    "status" to "DRAFT",
                    "createdAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 15),
                    "updatedAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 15),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "Q003",
                    "quotationNumber" to "QT-2024003",
                    "customerId" to "C003",
                    "customerName" to "DEF Manufacturing",
                    "date" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 5), // 5 days ago
                    "validUntil" to (TimeUtils.getCurrentISTTimestamp() + 86400000 * 25), // 25 days from now
                    "items" to listOf(
                        mapOf(
                            "id" to "QI004",
                            "productId" to "P001",
                            "productCode" to "P001",
                            "productName" to "Stainless Steel Pipe",
                            "description" to "304 Grade, 1 inch diameter",
                            "quantity" to 20,
                            "unit" to "Meter",
                            "unitPrice" to 150.0,
                            "tax" to 18.0,
                            "taxAmount" to 540.0,
                            "discount" to 0.0,
                            "discountAmount" to 0.0,
                            "subtotal" to 3000.0,
                            "total" to 3540.0
                        )
                    ),
                    "subtotal" to 3000.0,
                    "discountTotal" to 0.0,
                    "taxTotal" to 540.0,
                    "total" to 3540.0,
                    "notes" to "Regular customer",
                    "terms" to "Payment within 45 days",
                    "status" to "SENT",
                    "createdAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 5),
                    "updatedAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 5),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                )
            )

            // Insert quotations
            quotations.forEach { quotation ->
                connection.prepareStatement("""
                    INSERT OR IGNORE INTO quotations (
                        id, quotationNumber, customerId, customerName, date, validUntil,
                        items, subtotal, taxTotal, total, notes, terms, status,
                        createdAt, updatedAt, createdBy, updatedBy, discountTotal
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """).use { stmt ->
                    stmt.setString(1, quotation["id"] as String)
                    stmt.setString(2, quotation["quotationNumber"] as String)
                    stmt.setString(3, quotation["customerId"] as String)
                    stmt.setString(4, quotation["customerName"] as String)
                    stmt.setLong(5, quotation["date"] as Long)
                    stmt.setLong(6, quotation["validUntil"] as Long)
                    stmt.setString(7, json.encodeToString(quotation["items"]))
                    stmt.setDouble(8, quotation["subtotal"] as Double)
                    stmt.setDouble(9, quotation["taxTotal"] as Double)
                    stmt.setDouble(10, quotation["total"] as Double)
                    stmt.setString(11, quotation["notes"] as String)
                    stmt.setString(12, quotation["terms"] as String)
                    stmt.setString(13, quotation["status"] as String)
                    stmt.setLong(14, quotation["createdAt"] as Long)
                    stmt.setLong(15, quotation["updatedAt"] as Long)
                    stmt.setString(16, quotation["createdBy"] as String)
                    stmt.setString(17, quotation["updatedBy"] as String)
                    stmt.setDouble(18, quotation["discountTotal"] as Double)
                    stmt.executeUpdate()
                }
            }

            // Sample purchase orders
            val purchaseOrders = listOf(
                mapOf(
                    "id" to "PO001",
                    "poNumber" to "PO-2024001",
                    "supplierId" to "S001",
                    "supplierName" to "Steel Suppliers Ltd",
                    "date" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 20), // 20 days ago
                    "expectedDeliveryDate" to (TimeUtils.getCurrentISTTimestamp() + 86400000 * 10), // 10 days from now
                    "items" to listOf(
                        mapOf(
                            "id" to "POI001",
                            "productId" to "P001",
                            "productCode" to "P001",
                            "productName" to "Stainless Steel Pipe",
                            "description" to "304 Grade, 1 inch diameter",
                            "quantity" to 100,
                            "unit" to "Meter",
                            "unitPrice" to 100.0,
                            "tax" to 18.0,
                            "taxAmount" to 1800.0,
                            "subtotal" to 10000.0,
                            "total" to 11800.0
                        )
                    ),
                    "subtotal" to 10000.0,
                    "taxTotal" to 1800.0,
                    "total" to 11800.0,
                    "notes" to "Urgent delivery required",
                    "terms" to "Payment within 30 days",
                    "status" to "PENDING",
                    "createdAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 20),
                    "updatedAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 20),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                ),
                mapOf(
                    "id" to "PO002",
                    "poNumber" to "PO-2024002",
                    "supplierId" to "S002",
                    "supplierName" to "Valve Manufacturers Inc",
                    "date" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 10), // 10 days ago
                    "expectedDeliveryDate" to (TimeUtils.getCurrentISTTimestamp() + 86400000 * 5), // 5 days from now
                    "items" to listOf(
                        mapOf(
                            "id" to "POI002",
                            "productId" to "P003",
                            "productCode" to "P003",
                            "productName" to "PVC Valve",
                            "description" to "2 inch ball valve",
                            "quantity" to 50,
                            "unit" to "Piece",
                            "unitPrice" to 80.0,
                            "tax" to 18.0,
                            "taxAmount" to 720.0,
                            "subtotal" to 4000.0,
                            "total" to 4720.0
                        )
                    ),
                    "subtotal" to 4000.0,
                    "taxTotal" to 720.0,
                    "total" to 4720.0,
                    "notes" to "Regular supplier",
                    "terms" to "Payment within 45 days",
                    "status" to "APPROVED",
                    "createdAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 10),
                    "updatedAt" to (TimeUtils.getCurrentISTTimestamp() - 86400000 * 10),
                    "createdBy" to "admin",
                    "updatedBy" to "admin"
                )
            )

            // Insert purchase orders
            purchaseOrders.forEach { po ->
                connection.prepareStatement("""
                    INSERT OR IGNORE INTO purchase_orders (
                        id, poNumber, supplierId, supplierName, date, expectedDeliveryDate,
                        items, subtotal, taxTotal, total, notes, terms, status,
                        createdAt, updatedAt, createdBy, updatedBy
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """).use { stmt ->
                    stmt.setString(1, po["id"] as String)
                    stmt.setString(2, po["poNumber"] as String)
                    stmt.setString(3, po["supplierId"] as String)
                    stmt.setString(4, po["supplierName"] as String)
                    stmt.setLong(5, po["date"] as Long)
                    stmt.setLong(6, po["expectedDeliveryDate"] as Long)
                    stmt.setString(7, json.encodeToString(po["items"]))
                    stmt.setDouble(8, po["subtotal"] as Double)
                    stmt.setDouble(9, po["taxTotal"] as Double)
                    stmt.setDouble(10, po["total"] as Double)
                    stmt.setString(11, po["notes"] as String)
                    stmt.setString(12, po["terms"] as String)
                    stmt.setString(13, po["status"] as String)
                    stmt.setLong(14, po["createdAt"] as Long)
                    stmt.setLong(15, po["updatedAt"] as Long)
                    stmt.setString(16, po["createdBy"] as String)
                    stmt.setString(17, po["updatedBy"] as String)
                    stmt.executeUpdate()
                }
            }
        } catch (e: Exception) {
            println("Failed to populate dummy data: ${e.message}")
            e.printStackTrace()
        }
    }
} 