package com.mikrochek.server.repository.auth

import com.mikrochek.server.database.models.User
import org.bson.types.ObjectId

interface AuthRepository {
    suspend fun login(userName: String, password: String): Result<User>
    suspend fun signup(user: User): Result<User>
    suspend fun forgotPassword(email: String): Result<Boolean>
    suspend fun resetPassword(token: String, newPassword: String): Result<Boolean>
    suspend fun changePassword(userId: ObjectId, oldPassword: String, newPassword: String): Result<Boolean>
    suspend fun verifyEmail(token: String): Result<Boolean>
    suspend fun refreshToken(refreshToken: String): Result<String>
    suspend fun logout(): Result<Boolean>
    suspend fun validateToken(token: String): Result<Boolean>
    suspend fun sendVerificationEmail(email: String): Result<Boolean>
    suspend fun validateResetToken(token: String): Result<Boolean>
} 