package com.mikrochek.server.repository.po

import com.mikrochek.server.database.models.PurchaseOrder

interface PurchaseOrderRepository {
    suspend fun createPurchaseOrder(purchaseOrder: PurchaseOrder): PurchaseOrder
    suspend fun getPurchaseOrderById(id: String): PurchaseOrder?
    suspend fun getPurchaseOrderByNumber(poNumber: String): PurchaseOrder?
    suspend fun updatePurchaseOrder(purchaseOrder: PurchaseOrder): PurchaseOrder
    suspend fun deletePurchaseOrder(id: String): Boolean
    suspend fun getAllPurchaseOrders(): List<PurchaseOrder>
    suspend fun searchPurchaseOrders(query: String): List<PurchaseOrder>
    suspend fun getPurchaseOrdersByVendor(vendorName: String): List<PurchaseOrder>
    suspend fun getPurchaseOrdersByStatus(status: String): List<PurchaseOrder>
} 