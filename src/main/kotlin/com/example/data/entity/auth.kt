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

object SignUpApplicationEntity : Table<Nothing>("sign_up_applications") {
    val id = varchar("id").primaryKey()
    val userId = varchar("user_id")
    val code = varchar("code")
    val datetime = datetime("datetime")
    val token = datetime("token")
}

object RecoveryApplicationEntity : Table<Nothing>("recovery_applications") {
    val id = varchar("id").primaryKey()
    val userId = varchar("user_id")
    val code = varchar("code")
    val datetime = datetime("datetime")
    val token = datetime("token")
}