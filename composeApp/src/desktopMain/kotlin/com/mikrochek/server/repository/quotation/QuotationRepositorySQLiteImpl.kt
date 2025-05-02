package com.mikrochek.server.repository.quotation

import com.mikrochek.server.database.SQLiteDatabase
import com.mikrochek.server.database.models.Quotation
import com.mikrochek.server.database.models.QuotationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.sql.ResultSet

class QuotationRepositorySQLiteImpl : QuotationRepository {
    private val db = SQLiteDatabase
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private fun ResultSet.toQuotation(): Quotation = Quotation(
        id = getString("id"),
        quotationNumber = getString("quotationNumber"),
        customerId = getString("customerId"),
        customerName = getString("customerName"),
        date = getLong("date"),
        validUntil = getLong("validUntil"),
        items = json.decodeFromString(getString("items")),
        subtotal = getDouble("subtotal"),
        taxTotal = getDouble("taxTotal"),
        total = getDouble("total"),
        notes = getString("notes"),
        terms = getString("terms"),
        status = QuotationStatus.valueOf(getString("status")),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
        createdBy = getString("createdBy"),
        updatedBy = getString("updatedBy"),
        discountTotal = getDouble("discountTotal")
    )

    override suspend fun createQuotation(quotation: Quotation): Result<Quotation> = withContext(Dispatchers.IO) {
        try {
            println("Creating quotation: ${quotation.quotationNumber}")
            val connection = db.getConnection()
            val itemsJson = json.encodeToString(quotation.items)
            println("Serialized items: $itemsJson")
            
            connection.prepareStatement("""
                INSERT INTO quotations (
                    id, quotationNumber, customerId, customerName, date, validUntil,
                    items, subtotal, taxTotal, total, notes, terms, status,
                    createdAt, updatedAt, createdBy, updatedBy
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, quotation.id)
                stmt.setString(2, quotation.quotationNumber)
                stmt.setString(3, quotation.customerId)
                stmt.setString(4, quotation.customerName)
                stmt.setLong(5, quotation.date)
                stmt.setLong(6, quotation.validUntil)
                stmt.setString(7, itemsJson)
                stmt.setDouble(8, quotation.subtotal)
                stmt.setDouble(9, quotation.taxTotal)
                stmt.setDouble(10, quotation.total)
                stmt.setString(11, quotation.notes)
                stmt.setString(12, quotation.terms)
                stmt.setString(13, quotation.status.name)
                stmt.setLong(14, quotation.createdAt)
                stmt.setLong(15, quotation.updatedAt)
                stmt.setString(16, quotation.createdBy)
                stmt.setString(17, quotation.updatedBy)
                
                println("Executing insert for quotation: ${quotation.quotationNumber}")
                val result = stmt.executeUpdate()
                println("Insert result: $result")
            }
            println("Successfully created quotation: ${quotation.quotationNumber}")
            Result.success(quotation)
        } catch (e: Exception) {
            println("Failed to create quotation: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getQuotationById(id: String): Quotation? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM quotations WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toQuotation() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getQuotationByNumber(number: String): Quotation? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM quotations WHERE quotationNumber = ?").use { stmt ->
                stmt.setString(1, number)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toQuotation() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateQuotation(quotation: Quotation): Result<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE quotations SET 
                    quotationNumber = ?, customerId = ?, customerName = ?, 
                    date = ?, validUntil = ?, items = ?, subtotal = ?, 
                    taxTotal = ?, total = ?, notes = ?, terms = ?, 
                    status = ?, updatedAt = ?, updatedBy = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, quotation.quotationNumber)
                stmt.setString(2, quotation.customerId)
                stmt.setString(3, quotation.customerName)
                stmt.setLong(4, quotation.date)
                stmt.setLong(5, quotation.validUntil)
                stmt.setString(6, json.encodeToString(quotation.items))
                stmt.setDouble(7, quotation.subtotal)
                stmt.setDouble(8, quotation.taxTotal)
                stmt.setDouble(9, quotation.total)
                stmt.setString(10, quotation.notes)
                stmt.setString(11, quotation.terms)
                stmt.setString(12, quotation.status.name)
                stmt.setLong(13, quotation.updatedAt)
                stmt.setString(14, quotation.updatedBy)
                stmt.setString(15, quotation.id)
                stmt.executeUpdate()
            }
            Result.success(quotation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteQuotation(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("DELETE FROM quotations WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                Result.success(stmt.executeUpdate() > 0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllQuotations(): List<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM quotations ORDER BY date DESC").use { stmt ->
                val rs = stmt.executeQuery()
                val quotations = mutableListOf<Quotation>()
                while (rs.next()) {
                    quotations.add(rs.toQuotation())
                }
                quotations
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getQuotationsByStatus(status: QuotationStatus): List<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM quotations 
                WHERE status = ? 
                ORDER BY date DESC
            """).use { stmt ->
                stmt.setString(1, status.name)
                val rs = stmt.executeQuery()
                val quotations = mutableListOf<Quotation>()
                while (rs.next()) {
                    quotations.add(rs.toQuotation())
                }
                quotations
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getQuotationsByCustomer(customerId: String): List<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM quotations 
                WHERE customerId = ? 
                ORDER BY date DESC
            """).use { stmt ->
                stmt.setString(1, customerId)
                val rs = stmt.executeQuery()
                val quotations = mutableListOf<Quotation>()
                while (rs.next()) {
                    quotations.add(rs.toQuotation())
                }
                quotations
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchQuotations(query: String): List<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM quotations 
                WHERE quotationNumber LIKE ? 
                   OR customerName LIKE ? 
                   OR notes LIKE ?
                ORDER BY date DESC
            """).use { stmt ->
                val searchPattern = "%$query%"
                stmt.setString(1, searchPattern)
                stmt.setString(2, searchPattern)
                stmt.setString(3, searchPattern)
                val rs = stmt.executeQuery()
                val quotations = mutableListOf<Quotation>()
                while (rs.next()) {
                    quotations.add(rs.toQuotation())
                }
                quotations
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateQuotationStatus(id: String, status: QuotationStatus): Result<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE quotations 
                SET status = ?, updatedAt = ? 
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, status.name)
                stmt.setLong(2, System.currentTimeMillis())
                stmt.setString(3, id)
                stmt.executeUpdate()
            }
            getQuotationById(id)?.let { Result.success(it) }
                ?: Result.failure(Exception("Quotation not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpiredQuotations(): List<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM quotations 
                WHERE validUntil < ? 
                  AND status NOT IN (?, ?, ?)
                ORDER BY validUntil DESC
            """).use { stmt ->
                stmt.setLong(1, System.currentTimeMillis())
                stmt.setString(2, QuotationStatus.APPROVED.name)
                stmt.setString(3, QuotationStatus.REJECTED.name)
                stmt.setString(4, QuotationStatus.IN_PRODUCTION.name)
                val rs = stmt.executeQuery()
                val quotations = mutableListOf<Quotation>()
                while (rs.next()) {
                    quotations.add(rs.toQuotation())
                }
                quotations
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRecentQuotations(limit: Int): List<Quotation> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM quotations 
                ORDER BY date DESC 
                LIMIT ?
            """).use { stmt ->
                stmt.setInt(1, limit)
                val rs = stmt.executeQuery()
                val quotations = mutableListOf<Quotation>()
                while (rs.next()) {
                    quotations.add(rs.toQuotation())
                }
                quotations
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 