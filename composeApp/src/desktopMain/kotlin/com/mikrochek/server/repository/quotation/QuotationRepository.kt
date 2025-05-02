package com.mikrochek.server.repository.quotation

import com.mikrochek.server.database.models.Quotation
import com.mikrochek.server.database.models.QuotationStatus

interface QuotationRepository {
    suspend fun createQuotation(quotation: Quotation): Result<Quotation>
    suspend fun getQuotationById(id: String): Quotation?
    suspend fun getQuotationByNumber(number: String): Quotation?
    suspend fun updateQuotation(quotation: Quotation): Result<Quotation>
    suspend fun deleteQuotation(id: String): Result<Boolean>
    suspend fun getAllQuotations(): List<Quotation>
    suspend fun getQuotationsByStatus(status: QuotationStatus): List<Quotation>
    suspend fun getQuotationsByCustomer(customerId: String): List<Quotation>
    suspend fun searchQuotations(query: String): List<Quotation>
    suspend fun updateQuotationStatus(id: String, status: QuotationStatus): Result<Quotation>
    suspend fun getExpiredQuotations(): List<Quotation>
    suspend fun getRecentQuotations(limit: Int = 10): List<Quotation>
} 