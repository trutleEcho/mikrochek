package com.mikrochek.server.repository.user

import com.mikrochek.server.database.models.User

interface UserRepository {
    // Basic CRUD operations
    suspend fun createUser(user: User): Result<User>
    suspend fun getUserById(id: String): User?
    suspend fun getUserByUserName(userName: String): User?
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(id: String): Result<Boolean>
}