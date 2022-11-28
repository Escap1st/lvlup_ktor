package com.example.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String
)

@Serializable
data class EmailConfirmationRequest(
    val token: String,
    val code: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AccountRecoveryRequest(
    val email: String
)

@Serializable
data class RefreshRequest(
    @SerialName("user_id") val userId: String,
    val token: String,
)