package com.mikrochek.server.database

import com.mikrochek.utils.TimeUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.io.File
import java.nio.file.Paths

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
        } catch (e: SQLException) {
            e.printStackTrace()
            throw RuntimeException("Failed to create database tables", e)
        }
    }
} 