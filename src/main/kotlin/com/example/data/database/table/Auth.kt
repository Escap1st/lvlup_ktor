package com.example.data.database.table

import com.example.data.database.entity.AuthApplicationEntity
import com.example.data.database.entity.AuthApplicationType
import com.example.data.database.entity.RefreshTokenEntity
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.enum
import org.ktorm.schema.varchar

object AuthApplicationsTable : Table<AuthApplicationEntity>("auth_applications") {
    val token = varchar("token").primaryKey().bindTo { it.token }
    val userId = varchar("user_id").bindTo { it.userId }
    val code = varchar("code").bindTo { it.code }
    val datetime = datetime("datetime").bindTo { it.datetime }
    val password = varchar("password").bindTo { it.password }
    val type = enum<AuthApplicationType>("type").bindTo { it.type }
}

object RefreshTokensTable : Table<RefreshTokenEntity>("refresh_tokens") {
    val token = varchar("token").primaryKey().bindTo { it.token }
    val userId = varchar("user_id").bindTo { it.userId }
    val expiresAt = datetime("expires_at").bindTo { it.expiresAt }
}