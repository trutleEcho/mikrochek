package com.mikrochek.server.repository.po

import com.mikrochek.data.UserState
import com.mikrochek.server.database.SQLiteDatabase
import com.mikrochek.server.database.models.PurchaseOrder
import com.mikrochek.server.database.models.Document
import com.mikrochek.server.database.models.DocumentType
import com.mikrochek.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.sql.ResultSet

class PurchaseOrderRepositorySQLiteImpl : PurchaseOrderRepository {
    private val db = SQLiteDatabase
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }

    private fun ResultSet.toDocument(): Document = Document(
        id = getString("id"),
        type = DocumentType.valueOf(getString("type")),
        number = getString("number"),
        filePath = getString("filePath"),
        content = getString("content"),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
        createdBy = getString("createdBy"),
        metadata = json.decodeFromString(getString("metadata"))
    )

    override suspend fun createPurchaseOrder(purchaseOrder: PurchaseOrder): PurchaseOrder = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            val document = Document(
                id = purchaseOrder.id,
                type = DocumentType.PURCHASE_ORDER,
                number = purchaseOrder.poNumber,
                filePath = "documents/po/${purchaseOrder.poNumber}.json",
                content = json.encodeToString(purchaseOrder),
                createdBy = UserState.currentUser.value?.id ?: "Guest",
                metadata = mapOf(
                    "vendorName" to purchaseOrder.vendorName,
                    "total" to purchaseOrder.total.toString(),
                    "status" to purchaseOrder.status
                )
            )

            connection.prepareStatement("""
                INSERT INTO documents (id, type, number, filePath, content, createdAt, updatedAt, createdBy, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, document.id)
                stmt.setString(2, document.type.name)
                stmt.setString(3, document.number)
                stmt.setString(4, document.filePath)
                stmt.setString(5, document.content)
                stmt.setLong(6, document.createdAt)
                stmt.setLong(7, document.updatedAt)
                stmt.setString(8, document.createdBy)
                stmt.setString(9, json.encodeToString(document.metadata))
                stmt.executeUpdate()
            }
            purchaseOrder
        } catch (e: Exception) {
            println("Error creating purchase order: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getPurchaseOrderById(id: String): PurchaseOrder? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            println("Searching for PO with id/number: $id")
            connection.prepareStatement("""
                SELECT * FROM documents 
                WHERE (id = ? OR number = ?) AND type = ?
            """).use { stmt ->
                stmt.setString(1, id)
                stmt.setString(2, id)
                stmt.setString(3, DocumentType.PURCHASE_ORDER.name)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    val document = rs.toDocument()
                    println("Found document: $document")
                    println("Document content: ${document.content}")
                    json.decodeFromString<PurchaseOrder>(document.content)
                } else {
                    println("No purchase order found with id/number: $id")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error getting purchase order by id/number: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun getPurchaseOrderByNumber(poNumber: String): PurchaseOrder? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM documents 
                WHERE number = ? AND type = ?
            """).use { stmt ->
                stmt.setString(1, poNumber)
                stmt.setString(2, DocumentType.PURCHASE_ORDER.name)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    json.decodeFromString(rs.toDocument().content)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updatePurchaseOrder(purchaseOrder: PurchaseOrder): PurchaseOrder = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE documents 
                SET content = ?, metadata = ?, updatedAt = ?, number = ?
                WHERE id = ? AND type = ?
            """).use { stmt ->
                val metadata = mapOf(
                    "vendorName" to purchaseOrder.vendorName,
                    "total" to purchaseOrder.total.toString(),
                    "status" to purchaseOrder.status
                )
                stmt.setString(1, json.encodeToString(purchaseOrder))
                stmt.setString(2, json.encodeToString(metadata))
                stmt.setLong(3, TimeUtils.getCurrentISTTimestamp())
                stmt.setString(4, purchaseOrder.poNumber)
                stmt.setString(5, purchaseOrder.id)
                stmt.setString(6, DocumentType.PURCHASE_ORDER.name)
                val rowsAffected = stmt.executeUpdate()
                if (rowsAffected == 0) {
                    throw Exception("No purchase order found with id: ${purchaseOrder.id}")
                }
            }
            purchaseOrder
        } catch (e: Exception) {
            println("Error updating purchase order: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun deletePurchaseOrder(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                DELETE FROM documents 
                WHERE number = ? AND type = ?
            """).use { stmt ->
                stmt.setString(1, id)
                stmt.setString(2, DocumentType.PURCHASE_ORDER.name)
                stmt.executeUpdate() > 0
            }
        } catch (e: Exception) {
            println("Error deleting purchase order: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun getAllPurchaseOrders(): List<PurchaseOrder> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM documents 
                WHERE type = ?
                ORDER BY createdAt DESC
            """).use { stmt ->
                stmt.setString(1, DocumentType.PURCHASE_ORDER.name)
                val rs = stmt.executeQuery()
                val purchaseOrders = mutableListOf<PurchaseOrder>()
                while (rs.next()) {
                    purchaseOrders.add(json.decodeFromString(rs.toDocument().content))
                }
                purchaseOrders
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchPurchaseOrders(query: String): List<PurchaseOrder> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM documents 
                WHERE type = ? AND (
                    number LIKE ? OR 
                    content LIKE ? OR 
                    metadata LIKE ?
                )
                ORDER BY createdAt DESC
            """).use { stmt ->
                val searchPattern = "%$query%"
                stmt.setString(1, DocumentType.PURCHASE_ORDER.name)
                stmt.setString(2, searchPattern)
                stmt.setString(3, searchPattern)
                stmt.setString(4, searchPattern)
                val rs = stmt.executeQuery()
                val purchaseOrders = mutableListOf<PurchaseOrder>()
                while (rs.next()) {
                    purchaseOrders.add(json.decodeFromString(rs.toDocument().content))
                }
                purchaseOrders
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPurchaseOrdersByVendor(vendorName: String): List<PurchaseOrder> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM documents 
                WHERE type = ? AND 
                metadata LIKE ?
                ORDER BY createdAt DESC
            """).use { stmt ->
                stmt.setString(1, DocumentType.PURCHASE_ORDER.name)
                stmt.setString(2, "%\"vendorName\":\"$vendorName\"%")
                val rs = stmt.executeQuery()
                val purchaseOrders = mutableListOf<PurchaseOrder>()
                while (rs.next()) {
                    purchaseOrders.add(json.decodeFromString(rs.toDocument().content))
                }
                purchaseOrders
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPurchaseOrdersByStatus(status: String): List<PurchaseOrder> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM documents 
                WHERE type = ? AND 
                metadata LIKE ?
                ORDER BY createdAt DESC
            """).use { stmt ->
                stmt.setString(1, DocumentType.PURCHASE_ORDER.name)
                stmt.setString(2, "%\"status\":\"$status\"%")
                val rs = stmt.executeQuery()
                val purchaseOrders = mutableListOf<PurchaseOrder>()
                while (rs.next()) {
                    purchaseOrders.add(json.decodeFromString(rs.toDocument().content))
                }
                purchaseOrders
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 