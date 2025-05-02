package com.mikrochek.server.database.models

import com.mikrochek.utils.TimeUtils
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Department(
    val id: String,
    val name: String,
    val description: String? = null,
    val parentId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class Role(
    val id: String,
    val name: String,
    val description: String? = null,
    val permissions: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class Branch(
    val id: String,
    val name: String,
    val address: String? = null,
    val contactNumber: String? = null,
    val email: String? = null,
    val isMainBranch: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class OrganizationMetadata(
    val industry: String? = null,
    val size: String? = null,
    val website: String? = null,
    val taxId: String? = null,
    val registrationNumber: String? = null,
    val foundedYear: Int? = null,
    val customFields: Map<String, String> = emptyMap()
)

@Serializable
data class Organization(
    @BsonId
    @Contextual
    val _id: ObjectId = ObjectId(),
    val name: String,
    val logo: String? = null,
    val description: String? = null,
    val departments: List<Department> = emptyList(),
    val roles: List<Role> = emptyList(),
    val branches: List<Branch> = emptyList(),
    val services: List<String> = emptyList(),
    val metadata: OrganizationMetadata = OrganizationMetadata(),
    val createdAt: Long = TimeUtils.getCurrentISTTimestamp(),
    val updatedAt: Long? = null,
    val isActive: Boolean = true,
)
