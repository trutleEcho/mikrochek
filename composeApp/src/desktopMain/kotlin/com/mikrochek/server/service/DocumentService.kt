package com.mikrochek.server.service

import com.mikrochek.server.database.SQLiteDatabase
import com.mikrochek.server.database.models.Document
import com.mikrochek.server.database.models.DocumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

class DocumentService {
    private val db = SQLiteDatabase
    private val json = Json { prettyPrint = true }
    private val baseDocumentPath = Paths.get(System.getProperty("user.home"), "AppData", "Local", "MikroChek", "documents").toString()

    init {
        // Create base document directory if it doesn't exist
        File(baseDocumentPath).mkdirs()
        File("$baseDocumentPath/po").mkdirs()
        File("$baseDocumentPath/quotation").mkdirs()
    }

    private suspend fun getNextDocumentNumber(type: DocumentType): String = withContext(Dispatchers.IO) {
        val prefix = when (type) {
            DocumentType.PURCHASE_ORDER -> "PO"
            DocumentType.QUOTATION -> "QT"
        }

        db.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT OR IGNORE INTO document_counters (type, lastNumber) VALUES (?, 0)
            """).use { stmt ->
                stmt.setString(1, type.name)
                stmt.executeUpdate()
            }

            conn.prepareStatement("""
                UPDATE document_counters SET lastNumber = lastNumber + 1 WHERE type = ? RETURNING lastNumber
            """).use { stmt ->
                stmt.setString(1, type.name)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    "$prefix-${rs.getInt(1).toString().padStart(3, '0')}"
                } else {
                    throw IllegalStateException("Failed to generate document number")
                }
            }
        }
    }

    suspend fun importExistingDocuments() = withContext(Dispatchers.IO) {
        val poDir = File("$baseDocumentPath/po")
        val quotationDir = File("$baseDocumentPath/quotation")

        poDir.listFiles()?.forEach { file ->
            importDocument(file, DocumentType.PURCHASE_ORDER)
        }

        quotationDir.listFiles()?.forEach { file ->
            importDocument(file, DocumentType.QUOTATION)
        }
    }

    private suspend fun importDocument(file: File, type: DocumentType) {
        try {
            val content = file.readText()
            val document = Document(
                type = type,
                number = file.nameWithoutExtension,
                filePath = file.absolutePath,
                content = content,
                createdBy = "SYSTEM_IMPORT"  // You might want to set this differently
            )
            saveDocument(document)
        } catch (e: Exception) {
            println("Failed to import document ${file.name}: ${e.message}")
        }
    }

    suspend fun createDocument(
        type: DocumentType,
        content: String,
        userId: String,
        metadata: Map<String, String> = emptyMap()
    ): Result<Document> = withContext(Dispatchers.IO) {
        try {
            val number = getNextDocumentNumber(type)
            val subDir = when (type) {
                DocumentType.PURCHASE_ORDER -> "po"
                DocumentType.QUOTATION -> "quotation"
            }
            val filePath = "$baseDocumentPath/$subDir/$number.json"

            val document = Document(
                type = type,
                number = number,
                filePath = filePath,
                content = content,
                createdBy = userId,
                metadata = metadata
            )

            // Save to file system
            File(filePath).writeText(json.encodeToString(document))

            // Save to database
            saveDocument(document)

            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveDocument(document: Document): Result<Document> = withContext(Dispatchers.IO) {
        try {
            db.getConnection().prepareStatement("""
                INSERT OR REPLACE INTO documents (
                    id, type, number, filePath, content, createdAt, updatedAt, createdBy, metadata
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDocument(id: String): Document? = withContext(Dispatchers.IO) {
        db.getConnection().prepareStatement("SELECT * FROM documents WHERE id = ?").use { stmt ->
            stmt.setString(1, id)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                Document(
                    id = rs.getString("id"),
                    type = DocumentType.valueOf(rs.getString("type")),
                    number = rs.getString("number"),
                    filePath = rs.getString("filePath"),
                    content = rs.getString("content"),
                    createdAt = rs.getLong("createdAt"),
                    updatedAt = rs.getLong("updatedAt"),
                    createdBy = rs.getString("createdBy"),
                    metadata = json.decodeFromString(rs.getString("metadata"))
                )
            } else null
        }
    }

    suspend fun searchDocuments(
        type: DocumentType? = null,
        query: String? = null
    ): List<Document> = withContext(Dispatchers.IO) {
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()

        if (type != null) {
            conditions.add("type = ?")
            params.add(type.name)
        }

        if (!query.isNullOrBlank()) {
            conditions.add("(number LIKE ? OR content LIKE ? OR metadata LIKE ?)")
            params.add("%$query%")
            params.add("%$query%")
            params.add("%$query%")
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else ""

        val documents = mutableListOf<Document>()
        db.getConnection().prepareStatement(
            "SELECT * FROM documents $whereClause ORDER BY createdAt DESC"
        ).use { stmt ->
            params.forEachIndexed { index, param ->
                stmt.setObject(index + 1, param)
            }
            val rs = stmt.executeQuery()
            while (rs.next()) {
                documents.add(
                    Document(
                        id = rs.getString("id"),
                        type = DocumentType.valueOf(rs.getString("type")),
                        number = rs.getString("number"),
                        filePath = rs.getString("filePath"),
                        content = rs.getString("content"),
                        createdAt = rs.getLong("createdAt"),
                        updatedAt = rs.getLong("updatedAt"),
                        createdBy = rs.getString("createdBy"),
                        metadata = json.decodeFromString(rs.getString("metadata"))
                    )
                )
            }
        }
        documents
    }

    suspend fun updateDocument(
        id: String,
        content: String,
        userId: String,
        metadata: Map<String, String> = emptyMap()
    ): Result<Document> = withContext(Dispatchers.IO) {
        try {
            val existingDoc = getDocument(id) ?: throw IllegalArgumentException("Document not found")
            
            val updatedDoc = existingDoc.copy(
                content = content,
                updatedAt = System.currentTimeMillis(),
                metadata = metadata
            )

            // Update file system
            File(updatedDoc.filePath).writeText(json.encodeToString(updatedDoc))

            // Update database
            saveDocument(updatedDoc)

            Result.success(updatedDoc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val document = getDocument(id) ?: throw IllegalArgumentException("Document not found")
            
            // Delete from file system
            val file = File(document.filePath)
            if (file.exists()) {
                file.delete()
            }

            // Delete from database
            db.getConnection().prepareStatement(
                "DELETE FROM documents WHERE id = ?"
            ).use { stmt ->
                stmt.setString(1, id)
                val result = stmt.executeUpdate() > 0
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 