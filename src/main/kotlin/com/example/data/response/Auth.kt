package com.example.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthApplicationResponse(val token: String)

@Serializable
data class TokensResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("issued_at") val issuedAt: String,
    @SerialName("expires_at") val expiresAt: String,
)

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
)