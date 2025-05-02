package com.mikrochek.server.service

import com.mikrochek.server.database.models.Product
import com.mikrochek.server.database.models.ProductCategory
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.server.repository.product.ProductRepositorySQLiteImpl
import java.util.UUID

class ProductService {
    private val repository: ProductRepository = ProductRepositorySQLiteImpl()

    suspend fun createProduct(
        code: String,
        name: String,
        description: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        costPrice: Double,
        tax: Double = 0.0,
        minStock: Int = 0,
        currentStock: Int = 0,
        userId: String
    ): Result<Product> {
        val product = Product(
            id = UUID.randomUUID().toString(),
            code = code,
            name = name,
            description = description,
            category = category,
            unit = unit,
            sellingPrice = sellingPrice,
            costPrice = costPrice,
            tax = tax,
            minStock = minStock,
            currentStock = currentStock,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            createdBy = userId,
            updatedBy = userId
        )
        return repository.createProduct(product)
    }

    suspend fun getProductById(id: String): Product? {
        return repository.getProductById(id)
    }

    suspend fun getProductByCode(code: String): Product? {
        return repository.getProductByCode(code)
    }

    suspend fun updateProduct(
        id: String,
        code: String,
        name: String,
        description: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        costPrice: Double,
        tax: Double,
        minStock: Int,
        currentStock: Int,
        isActive: Boolean,
        userId: String
    ): Result<Product> {
        val existingProduct = repository.getProductById(id) ?: return Result.failure(Exception("Product not found"))
        
        val updatedProduct = existingProduct.copy(
            code = code,
            name = name,
            description = description,
            category = category,
            unit = unit,
            sellingPrice = sellingPrice,
            costPrice = costPrice,
            tax = tax,
            minStock = minStock,
            currentStock = currentStock,
            isActive = isActive,
            updatedAt = System.currentTimeMillis(),
            updatedBy = userId
        )
        
        return repository.updateProduct(updatedProduct)
    }

    suspend fun deleteProduct(id: String): Result<Boolean> {
        return repository.deleteProduct(id)
    }

    suspend fun getAllProducts(activeOnly: Boolean = true): List<Product> {
        return repository.getAllProducts(if (activeOnly) true else null)
    }

    suspend fun getProductsByCategory(category: String): List<Product> {
        return repository.getProductsByCategory(category)
    }

    suspend fun searchProducts(query: String): List<Product> {
        return repository.searchProducts(query)
    }

    suspend fun updateStock(productId: String, quantity: Int): Result<Product> {
        return repository.updateStock(productId, quantity)
    }

    suspend fun getLowStockProducts(): List<Product> {
        return repository.getLowStockProducts()
    }

    // Category management
    suspend fun createCategory(
        name: String,
        description: String?,
        userId: String
    ): Result<ProductCategory> {
        val category = ProductCategory(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            createdBy = userId,
            updatedBy = userId
        )
        return repository.createCategory(category)
    }

    suspend fun getCategoryById(id: String): ProductCategory? {
        return repository.getCategoryById(id)
    }

    suspend fun updateCategory(
        id: String,
        name: String,
        description: String?,
        isActive: Boolean,
        userId: String
    ): Result<ProductCategory> {
        val existingCategory = repository.getCategoryById(id) ?: return Result.failure(Exception("Category not found"))
        
        val updatedCategory = existingCategory.copy(
            name = name,
            description = description,
            isActive = isActive,
            updatedAt = System.currentTimeMillis(),
            updatedBy = userId
        )
        
        return repository.updateCategory(updatedCategory)
    }

    suspend fun deleteCategory(id: String): Result<Boolean> {
        return repository.deleteCategory(id)
    }

    suspend fun getAllCategories(activeOnly: Boolean = true): List<ProductCategory> {
        return repository.getAllCategories(if (activeOnly) true else null)
    }
} 