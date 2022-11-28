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

class ErrorDescriptions {
    companion object {
        val invalidSignUpData = ErrorDescription(
            "INVALID_SIGN_UP_DATA",
            "Check the data you provided to sign up",
        )
        val userIsRegistered = ErrorDescription(
            "USED_IS_REGISTERED",
            "User is already registered",
        )
        val wrongConfirmationCode = ErrorDescription(
            "WRONG_CONFIRMATION_CODE",
            "Wrong confirmation code",
        )
        val confirmationFailed = ErrorDescription(
            "CONFIRMATION_FAILED",
            "Failed to confirm authentication",
        )
        val userCreationFailed = ErrorDescription(
            "FAILED_TO_ADD_USER",
            "Failed to add user",
        )
        val unauthorizedUser = ErrorDescription(
            "UNAUTHORIZED_USER",
            "Authenticate to access this data"
        )
        val outdatedToken = ErrorDescription(
            "TOKEN_IS_OUTDATED",
            "Need to re-authenticate"
        )
        val tripNotFound = ErrorDescription(
            "TRIP_NOT_FOUND",
            "No such trip"
        )
        val invalidToken = ErrorDescription(
            "INVALID_TOKEN",
            "Invalid token"
        )
    }
}