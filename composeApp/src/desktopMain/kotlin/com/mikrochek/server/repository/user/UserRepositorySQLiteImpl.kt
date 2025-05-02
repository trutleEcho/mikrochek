package com.mikrochek.server.repository.user

import com.mikrochek.server.database.SQLiteDatabase
import com.mikrochek.server.database.models.User
import com.mikrochek.server.database.enum.AccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

class UserRepositorySQLiteImpl : UserRepository {
    private val db = SQLiteDatabase

    private fun ResultSet.toUser(): User = User(
        id = getString("id"),
        name = getString("name"),
        userName = getString("userName"),
        password = getString("password"),
        accountType = AccountType.valueOf(getString("accountType")),
        createdAt = getLong("createdAt"),
        lastLoginAt = getLong("lastLoginAt").takeIf { !wasNull() },
        isActive = getBoolean("isActive")
    )

    override suspend fun createUser(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                INSERT INTO users (id, name, userName, password, accountType, createdAt, lastLoginAt, isActive)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, user.id)
                stmt.setString(2, user.name)
                stmt.setString(3, user.userName)
                stmt.setString(4, user.password)
                stmt.setString(5, user.accountType.name)
                stmt.setLong(6, user.createdAt)
                if (user.lastLoginAt != null) {
                    stmt.setLong(7, user.lastLoginAt)
                } else {
                    stmt.setNull(7, java.sql.Types.BIGINT)
                }
                stmt.setBoolean(8, user.isActive)
                stmt.executeUpdate()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByUserName(userName: String): User? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM users WHERE userName = ?").use { stmt ->
                stmt.setString(1, userName)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toUser() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("SELECT * FROM users WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toUser() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUser(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("""
                UPDATE users 
                SET name = ?, userName = ?, password = ?, accountType = ?, 
                    lastLoginAt = ?, isActive = ?
                WHERE id = ?
            """).use { stmt ->
                stmt.setString(1, user.name)
                stmt.setString(2, user.userName)
                stmt.setString(3, user.password)
                stmt.setString(4, user.accountType.name)
                if (user.lastLoginAt != null) {
                    stmt.setLong(5, user.lastLoginAt)
                } else {
                    stmt.setNull(5, java.sql.Types.BIGINT)
                }
                stmt.setBoolean(6, user.isActive)
                stmt.setString(7, user.id)
                stmt.executeUpdate()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val connection = db.getConnection()
            connection.prepareStatement("DELETE FROM users WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                Result.success(stmt.executeUpdate() > 0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 