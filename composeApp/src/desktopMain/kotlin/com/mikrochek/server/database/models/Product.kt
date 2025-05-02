package com.mikrochek.server.database.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class GaugeType {
    CARBIDE_PLUG,
    THREAD_PLUG,
    RING,
    SNAP,
    PIN,
    MASTER,
    SETTING_RING,
    TAPER
}

@Serializable
enum class GaugeMaterial {
    CARBIDE,
    STEEL,
    CHROME_STEEL,
    TUNGSTEN_CARBIDE
}

@Serializable
data class Product(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val category: String,
    val unit: String, // e.g., "pcs", "kg", "m"
    val sellingPrice: Double,
    val costPrice: Double,
    val tax: Double = 0.0, // GST percentage
    val minStock: Int = 0,
    val currentStock: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
)

@Serializable
data class ProductCategory(
    val id: String,
    val name: String,
    val description: String?,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
) 