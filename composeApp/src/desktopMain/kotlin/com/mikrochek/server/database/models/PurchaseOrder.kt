package com.mikrochek.server.database.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PurchaseOrderItem(
    val id: String = UUID.randomUUID().toString(),
    val product: Product,
    val quantity: Int,
    val unitPrice: Double = product.sellingPrice,
    val tax: Double = 0.0,
    val discount: Double = 0.0
) {
    val total: Double get() = quantity * unitPrice * (1 + tax/100) * (1 - discount/100)
}

@Serializable
data class PurchaseOrder(
    val id: String = UUID.randomUUID().toString(),
    val poNumber: String,
    val vendorName: String,
    val vendorAddress: String,
    val vendorContact: String,
    val issueDate: Long,
    val deliveryDate: Long?,
    val items: List<PurchaseOrderItem>,
    val terms: String,
    val notes: String = "",
    val status: String = "DRAFT",
    val subtotal: Double = items.sumOf { it.quantity * it.unitPrice },
    val tax: Double = items.sumOf { it.total * it.tax/100 },
    val discount: Double = items.sumOf { it.total * it.discount/100 },
    val total: Double = items.sumOf { it.total }
) 