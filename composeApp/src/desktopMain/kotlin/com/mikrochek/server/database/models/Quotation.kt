package com.mikrochek.server.database.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class QuotationItem(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val productCode: String,
    val productName: String,
    val description: String,
    val quantity: Int,
    val unit: String,
    val unitPrice: Double,
    val tax: Double, // GST percentage
    val taxAmount: Double,
    val discount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val subtotal: Double, // Before tax and discount
    val total: Double // After tax and discount
)

@Serializable
data class Quotation(
    val id: String,
    val quotationNumber: String,
    val customerId: String,
    val customerName: String, // Denormalized for quick access
    val date: Long,
    val validUntil: Long,
    val items: List<QuotationItem>,
    val subtotal: Double,
    val discountTotal: Double,
    val taxTotal: Double,
    val total: Double,
    val notes: String?,
    val terms: String?,
    val status: QuotationStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
)

@Serializable
enum class QuotationStatus {
    DRAFT,
    SENT,
    APPROVED,
    REJECTED,
    EXPIRED,
    IN_PRODUCTION,
    COMPLETED,
    READY_FOR_DISPATCH,
    DISPATCHED,
}