package com.example.data.database.entity

import org.ktorm.entity.Entity
import java.time.LocalDateTime

interface AuthApplicationEntity : Entity<AuthApplicationEntity> {
    val token: String
    val userId: String
    val code: String?
    val datetime: LocalDateTime
    val password: String?
    val type: AuthApplicationType
}

interface RefreshTokenEntity : Entity<RefreshTokenEntity> {
    val token: String
    val userId: String
    val expiresAt: LocalDateTime
}

enum class AuthApplicationType {
    sign_up,
    recovery_init,
    recovery_complete,
}