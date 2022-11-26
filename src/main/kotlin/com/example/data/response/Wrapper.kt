package com.example.data.response

import kotlinx.serialization.Serializable

@Serializable
data class Wrapper<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDescription? = null
)

@Serializable
data class ErrorDescription(
    val code: String,
    val message: String,
)