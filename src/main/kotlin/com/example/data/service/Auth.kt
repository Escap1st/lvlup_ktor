package com.example.data.service

import com.example.data.database.entity.AuthApplicationType
import com.example.data.model.request.*
import com.example.data.model.response.AuthApplicationResponse
import com.example.data.model.response.ErrorDescriptions
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserRepository
import com.example.plugins.respondWithTokens
import com.example.plugins.sha256
import com.example.tools.EmailValidator
import com.example.tools.PasswordValidator
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.util.*

fun Routing.configureAuthService(authRepository: AuthRepository, userRepository: UserRepository) {
    fun createRefreshToken(userId: String): String {
        return UUID.randomUUID().toString().apply {
            authRepository.insertRefreshToken(this@apply, userId)
        }
    }

    fun createConfirmationCode(email: String? = null) =
        // TODO: only for test, remove later
        if (email?.endsWith("test.test") == true) {
            "6666"
        } else {
            (0..9999).random().toString().padStart(4, '0')
        }

    fun createNewApplication(
        type: AuthApplicationType,
        userId: String,
        code: String? = null
    ): String? {
        val token = UUID.randomUUID().toString()
        val appliedCount = if (authRepository.isUserApplied(userId)) {
            authRepository.initApplication(token, userId, type, code)
        } else {
            authRepository.recreateApplication(token, userId, type, code)
        }

        return if (appliedCount == 1) token else null
    }

    suspend fun ApplicationCall.confirmEmail(
        initApplicationType: AuthApplicationType,
        completeApplicationType: AuthApplicationType
    ) {
        val request = receive<EmailConfirmationRequest>()

        val applications = authRepository.getApplicationsByTokenAndType(request.token, listOf(initApplicationType))

        if (applications.isNotEmpty()) {
            val application = applications.first()
            if (request.code.sha256() == application.code) {
                authRepository.deleteApplication(request.token)
                createNewApplication(completeApplicationType, application.userId)?.let {
                    respondWithData(AuthApplicationResponse(it))
                    return
                }
            } else {
                respondWithError(
                    HttpStatusCode.BadRequest,
                    ErrorDescriptions.wrongConfirmationCode
                )
                return
            }
        }

        respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.confirmationFailed
        )
    }

    suspend fun ApplicationCall.setPassword(applicationType: AuthApplicationType) {
        val request = receive<PasswordChangeRequest>()

        if (!PasswordValidator.isPasswordValid(request.password)) {
            respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidRecoveryData
            )
            return
        }

        val applications = authRepository.getApplicationsByTokenAndType(request.token, listOf(applicationType))

        if (applications.isNotEmpty()) {
            val application = applications.first()

            val changedUsers = userRepository.changePassword(application.userId, request.password.sha256())
            if (changedUsers == 1) {
                authRepository.deleteApplication(request.token)
                createRefreshToken(application.userId).apply {
                    respondWithTokens(userRepository, application.userId, this)
                    return
                }
            }
        }

        respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.passwordChangeFailed
        )
    }

    post("/v1/auth/sign_up/init") {
        val request = call.receive<SignUpInitRequest>()

        // validate received data
        if (request.name.isBlank() || request.surname.isBlank() || !EmailValidator.isEmailValid(request.email)) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidSignUpData
            )
            return@post
        }

        // check if user with the same email is registered
        val registeredUser = userRepository.getUserByEmail(request.email)

        if (registeredUser?.password != null) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.userIsRegistered
            )
            return@post
        }

        val haveUserWithTheSameEmail = registeredUser != null
        val userId = if (haveUserWithTheSameEmail) {
            registeredUser!!.id
        } else {
            UUID.randomUUID().toString()
        }

        // creating new or updating current user
        val insertUser = if (haveUserWithTheSameEmail) {
            userRepository.renameUser(userId, request.name, request.surname)
        } else {
            userRepository.initUser(userId, request.name, request.surname, request.email)
        }

        if (insertUser == 1) {
            createNewApplication(
                AuthApplicationType.sign_up_init,
                userId,
                createConfirmationCode(request.email)
            )?.let {
                call.respondWithData(AuthApplicationResponse(it))
                return@post
            }
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.userCreationFailed
        )
    }

    post("/v1/auth/sign_up/confirm") {
        call.confirmEmail(AuthApplicationType.sign_up_init, AuthApplicationType.sign_up_complete)
    }

    post("/v1/auth/sign_up/complete") {
        call.setPassword(AuthApplicationType.sign_up_complete)
    }

    post("/v1/auth/login") {
        val request = call.receive<LoginRequest>()
        val user = userRepository.getUserByCredentials(request.email, request.password.sha256())

        if (user != null) {
            createRefreshToken(user.id).apply {
                call.respondWithTokens(userRepository, user.id, this)
            }
        } else {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.wrongCredentials,
            )
        }
    }

    post("/v1/auth/refresh") {
        val request = call.receive<RefreshRequest>()
        val dbToken = authRepository.getUsersToken(request.userId, request.token)

        if (dbToken?.expiresAt?.isAfter(LocalDateTime.now()) == true) {
            UUID.randomUUID().toString().apply {
                authRepository.updateToken(request.token, this@apply)
                call.respondWithTokens(userRepository, request.userId, this@apply)
                return@post
            }
        }

        call.respondWithError(HttpStatusCode.BadRequest, ErrorDescriptions.invalidToken)
    }

    post("/v1/auth/recovery/init") {
        val request = call.receive<AccountRecoveryInitRequest>()
        if (!EmailValidator.isEmailValid(request.email)) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidRecoveryData
            )
            return@post
        }

        val registeredUser = userRepository.getUserByEmail(request.email)

        if (registeredUser == null) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.noUserFound
            )
            return@post
        }

        createNewApplication(
            AuthApplicationType.recovery_init,
            registeredUser.id,
            code = createConfirmationCode(request.email)
        )?.let {
            call.respondWithData(AuthApplicationResponse(it))
            return@post
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.userCreationFailed
        )
    }

    post("/v1/auth/recovery/confirm") {
        call.confirmEmail(AuthApplicationType.recovery_init, AuthApplicationType.recovery_complete)
    }

    post("/v1/auth/recovery/complete") {
        call.setPassword(AuthApplicationType.recovery_complete)
    }

    post("/v1/auth/resend_code") {
        val request = call.receive<CodeResendRequest>()
        val applications = authRepository.getApplicationsByTokenAndType(
            request.token,
            listOf(
                AuthApplicationType.sign_up_init,
                AuthApplicationType.recovery_init
            ),
        )

        if (applications.isNotEmpty()) {
            val application = applications.first()
            val newToken = UUID.randomUUID().toString()
            // TODO: only for test, remove later
            val newCode = if ("6666".sha256() == application.code) {
                application.code
            } else {
                createConfirmationCode()
            }

            authRepository.updateApplicationCode(request.token, newToken, newCode)

            call.respondWithData(AuthApplicationResponse(newToken))
            return@post
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.codeResendFailed
        )
    }

    // TODO: logout and remove account
}