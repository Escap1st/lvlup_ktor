package com.example.data.database.table

import com.example.data.database.entity.AuthApplicationEntity
import com.example.data.database.entity.AuthApplicationType
import com.example.data.database.entity.TokenEntity
import com.example.data.database.entity.UsersEntity
import org.ktorm.schema.*

object UsersTable : Table<UsersEntity>("users") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val surname = varchar("surname").bindTo { it.surname }
    val email = varchar("email").bindTo { it.email }
    val password = varchar("password").bindTo { it.password }
    val confirmed = boolean("confirmed").bindTo { it.confirmed }
}

object AuthApplicationsTable : Table<AuthApplicationEntity>("auth_applications") {
    val token = varchar("token").primaryKey().bindTo { it.token }
    val userId = varchar("user_id").bindTo { it.userId }
    val code = varchar("code").bindTo { it.code }
    val datetime = datetime("datetime").bindTo { it.datetime }
    val type = enum<AuthApplicationType>("type").bindTo { it.type }
}

object TokensTable : Table<TokenEntity>("tokens") {
    val token = varchar("token").primaryKey().bindTo { it.token }
    val userId = varchar("user_id").bindTo { it.userId }
    val expiresAt = datetime("expires_at").bindTo { it.expiresAt }
}