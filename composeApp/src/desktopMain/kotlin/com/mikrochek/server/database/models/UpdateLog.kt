package com.mikrochek.server.database.models

import com.mikrochek.server.database.enum.AccountType
import com.mikrochek.utils.TimeUtils
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class UpdateLog(
    @BsonId
    @Contextual
    val _id: ObjectId = ObjectId(),
    @Contextual
    val entityId: ObjectId,
    val entityType: String,
    val accountType: AccountType,
    val updatedFields: Map<String, String>,
    val previousValues: Map<String, String>,
    val description: String,
    val updatedBy: UpdatedBy,
    val createdAt: Long = TimeUtils.getCurrentISTTimestamp()
)
