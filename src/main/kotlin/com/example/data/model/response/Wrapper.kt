package com.example.data.model.response

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
        val invalidRecoveryData = ErrorDescription(
            "INVALID_RECOVERY_DATA",
            "Check the data you provided to recover an account",
        )
        val userIsRegistered = ErrorDescription(
            "USED_IS_REGISTERED",
            "User is already registered",
        )
        val userIsNotRegistered = ErrorDescription(
            "USED_IS_NOT_REGISTERED",
            "User is not registered",
        )
        val wrongConfirmationCode = ErrorDescription(
            "WRONG_CONFIRMATION_CODE",
            "Wrong confirmation code",
        )
        val confirmationFailed = ErrorDescription(
            "CONFIRMATION_FAILED",
            "Failed to confirm authentication",
        )
        val passwordChangeFailed = ErrorDescription(
            "PASSWORD_CHANGE_FAILED",
            "Failed to change password",
        )
        val codeResendFailed = ErrorDescription(
            "CODE_RESEND_FAILED",
            "Failed to resend the code",
        )
        val wrongCredentials = ErrorDescription(
            "WRONG_CREDENTIALS",
            "Wrong email and/or password",
        )
        val userCreationFailed = ErrorDescription(
            "FAILED_TO_ADD_USER",
            "Failed to add user",
        )
        val accountRecoveryFailed = ErrorDescription(
            "FAILED_TO_RECOVER_AN_ACCOUNT",
            "Failed to recover an account",
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
        val noUserFound = ErrorDescription(
            "NO_USER_FOUND",
            "No user found"
        )
        val noActivityFound = ErrorDescription(
            "NO_ACTIVITY_FOUND",
            "No activity found"
        )
    }
}