package com.mikrochek.server.repository.auth

import com.mikrochek.server.database.models.User
import com.mikrochek.server.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import java.time.Instant

class AuthRepositorySQLiteImpl(
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun login(userName: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = userRepository.getUserByUserName(userName)
            when {
                user == null -> Result.failure(Exception("User not found"))
                !user.isActive -> Result.failure(Exception("Account is inactive"))
                user.password != password -> Result.failure(Exception("Invalid password"))
                else -> {
                    // Update last login time
                    val updatedUser = user.copy(lastLoginAt = Instant.now().toEpochMilli())
                    userRepository.updateUser(updatedUser).fold(
                        onSuccess = { Result.success(updatedUser) },
                        onFailure = { Result.failure(it) }
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Check if username already exists
            if (userRepository.getUserByUserName(user.userName) != null) {
                return@withContext Result.failure(Exception("Username already exists"))
            }
            userRepository.createUser(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        // Since we're using username-based authentication, this feature is not implemented
        Result.failure(Exception("Password reset functionality is not available"))
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Boolean> = withContext(Dispatchers.IO) {
        // Since we're using username-based authentication, this feature is not implemented
        Result.failure(Exception("Password reset functionality is not available"))
    }

    override suspend fun changePassword(
        userId: ObjectId,
        oldPassword: String,
        newPassword: String
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun verifyEmail(token: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshToken(refreshToken: String): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun validateToken(token: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun sendVerificationEmail(email: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun validateResetToken(token: String): Result<Boolean> {
        TODO("Not yet implemented")
    }
} 