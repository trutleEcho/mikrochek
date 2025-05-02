//package com.mikrochek.server.repository.auth
//
//import com.mikrochek.server.database.Collection
//import com.mikrochek.server.database.models.User
//import com.mikrochek.config.EnvVariables
//import com.mikrochek.data.OrganizationState
//import com.mikrochek.data.UserState
//import com.mikrochek.utils.TimeUtils
//import com.mongodb.client.model.Filters
//import com.mongodb.client.model.Updates
//import org.bson.types.ObjectId
//import org.litote.kmongo.coroutine.CoroutineClient
//import org.mindrot.jbcrypt.BCrypt
//import java.util.*
//
//class AuthRepositoryImpl(
//    private val client: CoroutineClient
//) : AuthRepository {
//    private val db = client.getDatabase(EnvVariables.ORGANIZATION_ID)
//    private val userCollection = db.getCollection<User>(Collection.USER)
//
//    // In-memory storage for tokens (in a production environment, use Redis or similar)
//    private val resetTokens = mutableMapOf<String, TokenInfo>()
//    private val verificationTokens = mutableMapOf<String, TokenInfo>()
//    private val refreshTokens = mutableMapOf<String, TokenInfo>()
//
//    private data class TokenInfo(
//        val userId: ObjectId,
//        val expiresAt: Long,
//        val userName: String? = null
//    )
//
//    override suspend fun login(userName: String, password: String): Result<User> {
//        return try {
//            val user = userCollection.find(Filters.eq(User::userName.name, userName)).first()
//                ?: return Result.failure(Exception("User not found"))
//
//            if (!BCrypt.checkpw(password, user.password)) {
//                return Result.failure(Exception("Invalid password"))
//            }
//
//            if (!user.isActive) {
//                return Result.failure(Exception("User account is inactive"))
//            }
//
//            // Update last login
//            val updateResult = userCollection.updateOne(
//                Filters.eq(User::_id.name, user._id),
//                Updates.set(User::lastLoginAt.name, TimeUtils.getCurrentISTTimestamp())
//            )
//
//            if (updateResult.modifiedCount > 0) {
//                // Set user in UserState
//                UserState.setUser(user)
//
//                // Generate and set auth token
//                val token = generateAuthToken()
//                UserState.setAuthToken(token)
//
//                Result.success(user)
//            } else {
//                Result.failure(Exception("Failed to update last login"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun signup(user: User): Result<User> {
//        return try {
//            // Check if userName already exists
//            val existingUser = userCollection.find(Filters.eq(User::userName.name, user.userName)).first()
//            if (existingUser != null) {
//                return Result.failure(Exception("Email already registered"))
//            }
//
//            // Hash password
//            val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
//            val userWithHashedPassword = user.copy(password = hashedPassword)
//
//            // Insert user
//            userCollection.insertOne(userWithHashedPassword)
//
//            // Generate verification token
//            val token = generateVerificationToken()
//            verificationTokens[token] = TokenInfo(
//                userId = userWithHashedPassword._id,
//                expiresAt = TimeUtils.getCurrentISTTimestamp() + 24 * 60 * 60 * 1000, // 24 hours
//                userName = user.userName
//            )
//
//            // Send verification userName
//            sendVerificationEmail(user.userName)
//
//            Result.success(userWithHashedPassword)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun forgotPassword(userName: String): Result<Boolean> {
//        return try {
//            val user = userCollection.find(Filters.eq(User::userName.name, userName)).first()
//                ?: return Result.failure(Exception("User not found"))
//
//            // Generate reset token
//            val token = generateResetToken()
//            resetTokens[token] = TokenInfo(
//                userId = user._id,
//                expiresAt = TimeUtils.getCurrentISTTimestamp() + 1 * 60 * 60 * 1000, // 1 hour
//                userName = userName
//            )
//
//            // TODO: Send reset password userName with token
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun resetPassword(token: String, newPassword: String): Result<Boolean> {
//        return try {
//            val tokenInfo = resetTokens[token] ?: return Result.failure(Exception("Invalid token"))
//
//            if (tokenInfo.expiresAt < TimeUtils.getCurrentISTTimestamp()) {
//                resetTokens.remove(token)
//                return Result.failure(Exception("Token expired"))
//            }
//
//            val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
//            val updateResult = userCollection.updateOne(
//                Filters.eq(User::_id.name, tokenInfo.userId),
//                Updates.combine(
//                    Updates.set(User::password.name, hashedPassword),
//                )
//            )
//
//            resetTokens.remove(token)
//            Result.success(updateResult.modifiedCount > 0)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun changePassword(
//        userId: ObjectId,
//        oldPassword: String,
//        newPassword: String
//    ): Result<Boolean> {
//        return try {
//            val user = userCollection.find(Filters.eq(User::_id.name, userId)).first()
//                ?: return Result.failure(Exception("User not found"))
//
//            if (!BCrypt.checkpw(oldPassword, user.password)) {
//                return Result.failure(Exception("Invalid old password"))
//            }
//
//            val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
//            val updateResult = userCollection.updateOne(
//                Filters.eq(User::_id.name, userId),
//                Updates.combine(
//                    Updates.set(User::password.name, hashedPassword),
//                )
//            )
//
//            Result.success(updateResult.modifiedCount > 0)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun verifyEmail(token: String): Result<Boolean> {
//        return try {
//            val tokenInfo = verificationTokens[token] ?: return Result.failure(Exception("Invalid token"))
//
//            if (tokenInfo.expiresAt < TimeUtils.getCurrentISTTimestamp()) {
//                verificationTokens.remove(token)
//                return Result.failure(Exception("Token expired"))
//            }
//
//            // TODO: Update user's userName verification status
//            verificationTokens.remove(token)
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun refreshToken(refreshToken: String): Result<String> {
//        return try {
//            val tokenInfo = refreshTokens[refreshToken] ?: return Result.failure(Exception("Invalid refresh token"))
//
//            if (tokenInfo.expiresAt < TimeUtils.getCurrentISTTimestamp()) {
//                refreshTokens.remove(refreshToken)
//                return Result.failure(Exception("Refresh token expired"))
//            }
//
//            val newToken = generateAuthToken()
//            Result.success(newToken)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun logout(): Result<Boolean> {
//        return try {
//            UserState.clearUserSession()
//            OrganizationState.clearOrganization()
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun validateToken(token: String): Result<Boolean> {
//        return try {
//            // TODO: Implement proper token validation
//            Result.success(token == UserState.getAuthToken())
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun sendVerificationEmail(userName: String): Result<Boolean> {
//        return try {
//            // TODO: Implement userName sending logic
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun validateResetToken(token: String): Result<Boolean> {
//        return try {
//            val tokenInfo = resetTokens[token] ?: return Result.failure(Exception("Invalid token"))
//
//            if (tokenInfo.expiresAt < TimeUtils.getCurrentISTTimestamp()) {
//                resetTokens.remove(token)
//                return Result.failure(Exception("Token expired"))
//            }
//
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    private fun generateAuthToken(): String {
//        return UUID.randomUUID().toString()
//    }
//
//    private fun generateResetToken(): String {
//        return UUID.randomUUID().toString()
//    }
//
//    private fun generateVerificationToken(): String {
//        return UUID.randomUUID().toString()
//    }
//}