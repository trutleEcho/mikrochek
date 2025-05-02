package com.mikrochek.server.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val customerCode: String,
    val companyName: String,
    // Contact person details flattened
    val contactName: String,
    val contactDesignation: String,
    val contactEmail: String,
    val contactPhone: String,
    // Address as simple fields
    val billingAddress: String,
    val billingCity: String,
    val billingState: String,
    val billingPostalCode: String,
    // Optional shipping address
    val shippingAddress: String?,
    val shippingCity: String?,
    val shippingState: String?,
    val shippingPostalCode: String?,
    // Business details
    val gstNumber: String?,
    val panNumber: String?,
    val creditLimit: Double = 0.0,
    val paymentTerms: Int = 30, // in days
    // Status and audit
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
)

@Serializable
data class ContactPerson(
    val name: String,
    val designation: String,
    val email: String,
    val phone: String,
    val alternatePhone: String?
) 