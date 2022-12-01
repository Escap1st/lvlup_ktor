package com.example.data.service

import com.example.data.database.table.AuthApplicationsTable
import com.example.data.database.table.TokensTable
import com.example.data.database.table.UsersTable
import com.example.data.request.*
import com.example.data.response.AccountRecoveryResponse
import com.example.data.response.CodeResendResponse
import com.example.data.response.ErrorDescriptions
import com.example.data.response.SignUpResponse
import com.example.plugins.DatabaseConnection
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
import org.ktorm.dsl.*
import java.time.LocalDateTime
import java.util.*

fun Routing.configureAuthService() {
    val db = DatabaseConnection.database

    fun createRefreshToken(userId: String): String {
        return UUID.randomUUID().toString().apply {
            db.insert(TokensTable) {
                set(it.token, this@apply)
                set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
                set(it.userId, userId)
            }
        }
    }

    fun createConfirmationCode() = (0..9999).random().toString().padStart(4, '0')

    fun createNewApplication(userId: String, password: String, email: String): String? {
        val userApplies = db.from(AuthApplicationsTable)
            .select()
            .where(AuthApplicationsTable.userId eq userId)
        val token = UUID.randomUUID().toString()
        // TODO for easier test, remove on prod rollout
        val code = if (email.endsWith("test.test")) {
            "6666"
        } else {
            createConfirmationCode()
        }
        val appliedCount = if (userApplies.totalRecords == 0) {
            db.insert(AuthApplicationsTable) {
                set(it.code, code.sha256())
                set(it.token, token)
                set(it.datetime, LocalDateTime.now())
                set(it.password, password.sha256())
                set(it.userId, userId)
            }
        } else {
            db.update(AuthApplicationsTable) {
                set(it.code, code.sha256())
                set(it.token, token)
                set(it.datetime, LocalDateTime.now())
                where { it.userId eq userId }
            }
        }

        return if (appliedCount == 1) token else null
    }

    post("/v1/auth/confirm") {
        val request = call.receive<EmailConfirmationRequest>()
        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(AuthApplicationsTable.token eq request.token)
            .map { AuthApplicationsTable.createEntity(it) }

        if (applications.isNotEmpty()) {
            val application = applications.first()
            if (request.code.sha256() == application.code) {
                db.delete(AuthApplicationsTable) { it.token eq request.token }
                db.update(UsersTable) {
                    set(it.password, application.password)
                    where { it.id eq application.userId }
                }
                call.respondWithTokens(
                    application.userId,
                    createRefreshToken(application.userId)
                )
                return@post
            } else {
                call.respondWithError(
                    HttpStatusCode.BadRequest,
                    ErrorDescriptions.wrongConfirmationCode
                )
                return@post
            }
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.confirmationFailed
        )
    }

    post("/v1/auth/login") {
        val request = call.receive<LoginRequest>()
//
        val users = db.from(UsersTable).select()
            .where(UsersTable.email eq request.email)
            .where(UsersTable.password eq request.password.sha256())
            .map { UsersTable.createEntity(it) }

        if (users.size == 1) {
            val userId = users.first().id
            call.respondWithTokens(userId, createRefreshToken(userId))
        } else {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.wrongCredentials,
            )
        }
    }

    post("/v1/auth/refresh") {
        val request = call.receive<RefreshRequest>()
        val dbToken = db.from(TokensTable)
            .select()
            .where(TokensTable.token eq request.token)
            .where(TokensTable.userId eq request.userId)
            .map { TokensTable.createEntity(it) }
            .firstOrNull()

        if (dbToken?.expiresAt?.isAfter(LocalDateTime.now()) == true) {
            val newToken = UUID.randomUUID().toString()
            db.update(TokensTable) {
                set(it.token, newToken)
                set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
                where { it.token eq request.token }
            }
            call.respondWithTokens(request.userId, newToken)
            return@post
        }

        call.respondWithError(HttpStatusCode.BadRequest, ErrorDescriptions.invalidToken)
    }

    post("/v1/auth/sign_up") {
        val request = call.receive<SignUpRequest>()

        // validate received data
        if (request.name.isBlank() || request.surname.isBlank()
            || !EmailValidator.isEmailValid(request.email)
            || !PasswordValidator.isPasswordValid(request.password)
        ) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidSignUpData
            )
            return@post
        }

        // check if user with the same email is registered
        val registeredUsers = db.from(UsersTable)
            .select()
            .where(UsersTable.email eq request.email)
            .map { UsersTable.createEntity(it) }

        if (registeredUsers.firstOrNull()?.password != null) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.userIsRegistered
            )
            return@post
        }

        val haveUserWithTheSameEmail = registeredUsers.isNotEmpty()
        val userId = if (haveUserWithTheSameEmail) {
            registeredUsers.first().id
        } else {
            UUID.randomUUID().toString()
        }

        // creating new or updating current user
        val insertUser = if (haveUserWithTheSameEmail) {
            db.update(UsersTable) {
                set(it.name, request.name)
                set(it.surname, request.surname)
                where { it.email eq request.email }
            }
        } else {
            db.insert(UsersTable) {
                set(it.id, userId)
                set(it.name, request.name)
                set(it.surname, request.surname)
                set(it.email, request.email)
            }
        }

        if (insertUser == 1) {
            val token = createNewApplication(registeredUsers.first().id, request.password, request.email)
            if (token != null) {
                call.respondWithData(SignUpResponse(token))
                return@post
            }
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.userCreationFailed
        )
    }

    post("/v1/auth/recover") {
        val request = call.receive<AccountRecoveryRequest>()
        if (!EmailValidator.isEmailValid(request.email) || !PasswordValidator.isPasswordValid(request.password)) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidRecoveryData
            )
            return@post
        }

        val registeredUsers = db.from(UsersTable)
            .select()
            .where(UsersTable.email eq request.email)
            .map { UsersTable.createEntity(it) }

        if (registeredUsers.isEmpty()) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.userIsRegistered
            )
            return@post
        }

        val token = createNewApplication(registeredUsers.first().id, request.password, request.email)
        if (token != null) {
            call.respondWithData(AccountRecoveryResponse(token))
        } else {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.userCreationFailed
            )
        }
    }

    post("/v1/auth/resend_code") {
        val request = call.receive<CodeResendRequest>()
        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(AuthApplicationsTable.token eq request.token)
            .map { AuthApplicationsTable.createEntity(it) }

        if (applications.isNotEmpty()) {
            val application = applications.first()
            val newToken = UUID.randomUUID().toString()
            // TODO: only for test, remove later
            val newCode = if ("6666".sha256() == application.code) {
                application.code
            } else {
                createConfirmationCode()
            }
            db.update(AuthApplicationsTable) {
                set(it.code, newCode)
                set(it.token, newToken)
                set(it.datetime, LocalDateTime.now())
                where { it.token eq request.token }
            }

            call.respondWithData(CodeResendResponse(newToken))
            return@post
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.codeResendFailed
        )
    }
}