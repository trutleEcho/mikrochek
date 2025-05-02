package com.mikrochek.server.database.models

import com.mikrochek.server.database.enum.AccountType
import com.mikrochek.utils.TimeUtils
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class ActionLog(
    @SerialName("_id")
    @Contextual
    val _id: ObjectId = ObjectId(),
    @Contextual
    val entityId: ObjectId,
    val entityType: String,
    val accountType: AccountType,
    val action: String,
    val description: String,

    @Contextual
    val metadata: Map<String, String>,

    val revertInstructions: String? = null,
    val revertible: Boolean = true,
    val reverted: Boolean = false,
    val performedBy: UpdatedBy,
    val revertedBy: UpdatedBy? = null,
    val createdAt: Long = TimeUtils.getCurrentISTTimestamp()
)
