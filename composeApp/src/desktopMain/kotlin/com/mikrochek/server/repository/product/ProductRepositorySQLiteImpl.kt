package com.mikrochek.server.repository.product

import com.mikrochek.server.database.SQLiteDatabase
import com.mikrochek.server.database.models.Product
import com.mikrochek.server.database.models.ProductCategory
import com.mikrochek.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

class ProductRepositorySQLiteImpl : ProductRepository {
    private val db = SQLiteDatabase

    private fun ResultSet.toProduct(): Product = Product(
        id = getString("id"),
        code = getString("code"),
        name = getString("name"),
        description = getString("description"),
        category = getString("category"),
        unit = getString("unit"),
        sellingPrice = getDouble("sellingPrice"),
        costPrice = getDouble("costPrice"),
        tax = getDouble("tax"),
        minStock = getInt("minStock"),
        currentStock = getInt("currentStock"),
        isActive = getBoolean("isActive"),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
        createdBy = getString("createdBy"),
        updatedBy = getString("updatedBy")
    )

    private fun ResultSet.toCategory(): ProductCategory = ProductCategory(
        id = getString("id"),
        name = getString("name"),
        description = getString("description"),
        isActive = getBoolean("isActive"),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
        createdBy = getString("createdBy"),
        updatedBy = getString("updatedBy")
    )

    override suspend fun createProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                INSERT INTO products (
                    id, code, name, description, category, unit, 
                    sellingPrice, costPrice, tax, minStock, currentStock, 
                    isActive, createdAt, updatedAt, createdBy, updatedBy
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, product.id)
                stmt.setString(2, product.code)
                stmt.setString(3, product.name)
                stmt.setString(4, product.description)
                stmt.setString(5, product.category)
                stmt.setString(6, product.unit)
                stmt.setDouble(7, product.sellingPrice)
                stmt.setDouble(8, product.costPrice)
                stmt.setDouble(9, product.tax)
                stmt.setInt(10, product.minStock)
                stmt.setInt(11, product.currentStock)
                stmt.setBoolean(12, product.isActive)
                stmt.setLong(13, product.createdAt)
                stmt.setLong(14, product.updatedAt)
                stmt.setString(15, product.createdBy)
                stmt.setString(16, product.updatedBy)
                stmt.executeUpdate()
            }
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM products WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toProduct() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getProductByCode(code: String): Product? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM products WHERE code = ?").use { stmt ->
                stmt.setString(1, code)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toProduct() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE products SET 
                    code = ?, name = ?, description = ?, category = ?, unit = ?,
                    sellingPrice = ?, costPrice = ?, tax = ?, minStock = ?, 
                    currentStock = ?, isActive = ?, updatedAt = ?, updatedBy = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, product.code)
                stmt.setString(2, product.name)
                stmt.setString(3, product.description)
                stmt.setString(4, product.category)
                stmt.setString(5, product.unit)
                stmt.setDouble(6, product.sellingPrice)
                stmt.setDouble(7, product.costPrice)
                stmt.setDouble(8, product.tax)
                stmt.setInt(9, product.minStock)
                stmt.setInt(10, product.currentStock)
                stmt.setBoolean(11, product.isActive)
                stmt.setLong(12, product.updatedAt)
                stmt.setString(13, product.updatedBy)
                stmt.setString(14, product.id)
                stmt.executeUpdate()
            }
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("DELETE FROM products WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                Result.success(stmt.executeUpdate() > 0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllProducts(isActive: Boolean?): List<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            val query = when (isActive) {
                null -> "SELECT * FROM products"
                else -> "SELECT * FROM products WHERE isActive = ?"
            }
            connection.prepareStatement(query).use { stmt ->
                if (isActive != null) {
                    stmt.setBoolean(1, isActive)
                }
                val rs = stmt.executeQuery()
                val products = mutableListOf<Product>()
                while (rs.next()) {
                    products.add(rs.toProduct())
                }
                products
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM products WHERE category = ?").use { stmt ->
                stmt.setString(1, category)
                val rs = stmt.executeQuery()
                val products = mutableListOf<Product>()
                while (rs.next()) {
                    products.add(rs.toProduct())
                }
                products
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM products 
                WHERE name LIKE ? OR code LIKE ? OR description LIKE ?
            """).use { stmt ->
                val searchPattern = "%$query%"
                stmt.setString(1, searchPattern)
                stmt.setString(2, searchPattern)
                stmt.setString(3, searchPattern)
                val rs = stmt.executeQuery()
                val products = mutableListOf<Product>()
                while (rs.next()) {
                    products.add(rs.toProduct())
                }
                products
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createCategory(category: ProductCategory): Result<ProductCategory> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                INSERT INTO product_categories (
                    id, name, description, isActive, 
                    createdAt, updatedAt, createdBy, updatedBy
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, category.id)
                stmt.setString(2, category.name)
                stmt.setString(3, category.description)
                stmt.setBoolean(4, category.isActive)
                stmt.setLong(5, category.createdAt)
                stmt.setLong(6, category.updatedAt)
                stmt.setString(7, category.createdBy)
                stmt.setString(8, category.updatedBy)
                stmt.executeUpdate()
            }
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategoryById(id: String): ProductCategory? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM product_categories WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toCategory() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateCategory(category: ProductCategory): Result<ProductCategory> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE product_categories SET 
                    name = ?, description = ?, isActive = ?, 
                    updatedAt = ?, updatedBy = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, category.name)
                stmt.setString(2, category.description)
                stmt.setBoolean(3, category.isActive)
                stmt.setLong(4, category.updatedAt)
                stmt.setString(5, category.updatedBy)
                stmt.setString(6, category.id)
                stmt.executeUpdate()
            }
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("DELETE FROM product_categories WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                Result.success(stmt.executeUpdate() > 0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCategories(isActive: Boolean?): List<ProductCategory> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            val query = when (isActive) {
                null -> "SELECT * FROM product_categories"
                else -> "SELECT * FROM product_categories WHERE isActive = ?"
            }
            connection.prepareStatement(query).use { stmt ->
                if (isActive != null) {
                    stmt.setBoolean(1, isActive)
                }
                val rs = stmt.executeQuery()
                val categories = mutableListOf<ProductCategory>()
                while (rs.next()) {
                    categories.add(rs.toCategory())
                }
                categories
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateStock(productId: String, quantity: Int): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE products 
                SET currentStock = currentStock + ?, 
                    updatedAt = ? 
                WHERE id = ?
            """).use { stmt ->
                stmt.setInt(1, quantity)
                stmt.setLong(2, TimeUtils.getCurrentISTTimestamp())
                stmt.setString(3, productId)
                stmt.executeUpdate()
            }
            getProductById(productId)?.let { Result.success(it) }
                ?: Result.failure(Exception("Product not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLowStockProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                SELECT * FROM products 
                WHERE currentStock <= minStock AND isActive = true
            """).use { stmt ->
                val rs = stmt.executeQuery()
                val products = mutableListOf<Product>()
                while (rs.next()) {
                    products.add(rs.toProduct())
                }
                products
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}