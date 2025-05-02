package com.mikrochek.server.repository.product

import com.mikrochek.server.database.models.Product
import com.mikrochek.server.database.models.ProductCategory

interface ProductRepository {
    suspend fun createProduct(product: Product): Result<Product>
    suspend fun getProductById(id: String): Product?
    suspend fun getProductByCode(code: String): Product?
    suspend fun updateProduct(product: Product): Result<Product>
    suspend fun deleteProduct(id: String): Result<Boolean>
    suspend fun getAllProducts(isActive: Boolean? = null): List<Product>
    suspend fun getProductsByCategory(category: String): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    
    // Category management
    suspend fun createCategory(category: ProductCategory): Result<ProductCategory>
    suspend fun getCategoryById(id: String): ProductCategory?
    suspend fun updateCategory(category: ProductCategory): Result<ProductCategory>
    suspend fun deleteCategory(id: String): Result<Boolean>
    suspend fun getAllCategories(isActive: Boolean? = null): List<ProductCategory>
    
    // Stock management
    suspend fun updateStock(productId: String, quantity: Int): Result<Product>
    suspend fun getLowStockProducts(): List<Product>
} 