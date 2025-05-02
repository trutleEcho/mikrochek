package com.mikrochek.server.database.enum

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    CLIENT,EMPLOYEE,ADMIN,ORG_ADMIN,KUSH,TEST
}