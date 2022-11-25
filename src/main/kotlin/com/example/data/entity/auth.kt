package com.example.data.entity

import org.ktorm.schema.*

object UserEntity : Table<Nothing>("users") {
    val id = varchar("id").primaryKey()
    val name = varchar("name")
    val surname = varchar("surname")
    val email = varchar("email")
    val password = varchar("password")
    val confirmed = boolean("confirmed")
}

object AuthApplicationEntity : Table<Nothing>("auth_applications") {
    val token = varchar("token").primaryKey()
    val userId = varchar("user_id")
    val code = varchar("code")
    val datetime = datetime("datetime")
    val type = enum<AuthApplicationType>("type")
}

enum class AuthApplicationType {
    sign_up,
    recovery,
}