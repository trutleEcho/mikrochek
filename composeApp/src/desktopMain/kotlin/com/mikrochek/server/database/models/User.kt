package com.mikrochek.server.database.models

import com.mikrochek.server.database.enum.AccountType
import com.mikrochek.utils.TimeUtils
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val userName: String,
    val password: String,
    val accountType: AccountType,
    val createdAt: Long = TimeUtils.getCurrentISTTimestamp(),
    val lastLoginAt: Long? = null,
    val isActive: Boolean = true
)
