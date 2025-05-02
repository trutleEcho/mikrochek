package com.mikrochek.server.database.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class DocumentType {
    PURCHASE_ORDER,
    QUOTATION
}

@Serializable
data class Document(
    val id: String = UUID.randomUUID().toString(),
    val type: DocumentType,
    val number: String,  // PO-001 or QT-001 format
    val filePath: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String,  // User ID
    val metadata: Map<String, String> = emptyMap()  // For additional fields like customer, total amount, etc.
) 