package com.example.data.response

import kotlinx.serialization.Serializable

@Serializable()
data class UserResponse(
    val id: String,
    val name: String,
    val surname: String
)