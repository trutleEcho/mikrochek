package com.mikrochek.server.database.models

import com.mikrochek.server.database.enum.AccountType
import com.mikrochek.utils.TimeUtils
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class UpdatedBy(
    @SerialName("_id")
    val id: Id<UpdatedBy> = newId(),
    @Contextual
    val userId: ObjectId,
    val userName: String,
    val accountType: AccountType,
    val timestamp: Long = TimeUtils.getCurrentISTTimestamp()
)
