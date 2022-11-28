package com.example.data.database.entity

import org.ktorm.entity.Entity
import java.time.LocalDateTime

interface UsersEntity : Entity<UsersEntity> {
    val id : String
    val name : String
    val surname : String
    val email : String
    val password : String
    val confirmed : Boolean
}

interface AuthApplicationEntity : Entity<AuthApplicationEntity> {
    val token: String
    val userId: String
    val code: String
    val datetime: LocalDateTime
    val type: AuthApplicationType
}

interface TokenEntity : Entity<TokenEntity> {
    val token: String
    val userId: String
    val expiresAt: LocalDateTime
}

enum class AuthApplicationType {
    sign_up,
    recovery,
}