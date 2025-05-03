package com.mikrochek.server.database.models

import com.mikrochek.utils.TimeUtils
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
    val createdAt: Long = TimeUtils.getCurrentISTTimestamp(),
    val updatedAt: Long = TimeUtils.getCurrentISTTimestamp(),
    val createdBy: String,  // User ID
    val metadata: Map<String, String> = emptyMap()  // For additional fields like customer, total amount, etc.
) 