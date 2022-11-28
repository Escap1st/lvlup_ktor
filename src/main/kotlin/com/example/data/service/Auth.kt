package com.example.data.service

import com.example.data.database.entity.AuthApplicationType
import com.example.data.database.table.AuthApplicationsTable
import com.example.data.database.table.TokensTable
import com.example.data.database.table.UsersTable
import com.example.data.request.EmailConfirmationRequest
import com.example.data.request.LoginRequest
import com.example.data.request.RefreshRequest
import com.example.data.request.SignUpRequest
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

    fun addToken(userId: String): String {
        return UUID.randomUUID().toString().apply {
            db.insert(TokensTable) {
                set(it.token, this@apply)
                set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
                set(it.userId, userId)
            }
        }
    }

    post("/auth/sign_up/confirm") {
        val request = call.receive<EmailConfirmationRequest>()
        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(AuthApplicationsTable.token eq request.token)
            .where(AuthApplicationsTable.type eq AuthApplicationType.sign_up)
            .map {
                AuthApplicationsTable.createEntity(it)
            }

        if (applications.isNotEmpty()) {
            val application = applications.first()
            if (request.code.sha256() == application.code) {
                db.delete(AuthApplicationsTable) {
                    it.token eq request.token
                }
                db.update(UsersTable) {
                    set(it.confirmed, true)
                    where {
                        it.id eq application.userId
                    }
                }
                call.respondWithTokens(application.userId, addToken(application.userId))
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

    post("/auth/recover/confirm") {
//            UUID.randomUUID()
//            val user = call.receive<User>()
        // Check username and password
        // ...
        call.respondWithTokens("", addToken(""))
    }

    post("/auth/login") {
        val request = call.receive<LoginRequest>()
//            val user = call.receive<User>()
        // Check username and password
        // ...
        call.respondWithTokens("", addToken(""))
    }

    post("/auth/refresh") {
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

    post("/auth/sign_up") {
        val request = call.receive<SignUpRequest>()
        if (request.name.isBlank() || request.surname.isBlank() ||
            !EmailValidator.isEmailValid(request.email) || !PasswordValidator.isPasswordValid(request.password)
        ) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidSignUpData
            )
            return@post
        }

        val registeredUsers = db.from(UsersTable)
            .select()
            .where(UsersTable.email eq request.email)
            .map {
                UsersTable.createEntity(it)
            }

        if (registeredUsers.firstOrNull()?.confirmed == true) {
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

        val insertUser = if (haveUserWithTheSameEmail) {
            db.update(UsersTable) {
                set(it.name, request.name)
                set(it.surname, request.surname)
                set(it.password, request.password.sha256())
                where {
                    it.email eq request.email
                }
            }
        } else {
            db.insert(UsersTable) {
                set(it.id, userId)
                set(it.name, request.name)
                set(it.surname, request.surname)
                set(it.email, request.email)
                set(it.password, request.password.sha256())
            }
        }

        if (insertUser == 1) {
            val userApplies = db.from(AuthApplicationsTable)
                .select()
                .where(AuthApplicationsTable.userId eq userId)
                .where(AuthApplicationsTable.type eq AuthApplicationType.sign_up)
            val token = UUID.randomUUID().toString()
            // TODO for easier test, remove on prod rollout
            val code = if (request.email.endsWith("test.test")) {
                "6666"
            } else {
                (0..9999).random().toString().padStart(4, '0')
            }
            val apply = if (userApplies.totalRecords == 0) {
                db.insert(AuthApplicationsTable) {
                    set(it.code, code.sha256())
                    set(it.token, token)
                    set(it.datetime, LocalDateTime.now())
                    set(it.type, AuthApplicationType.sign_up)
                    set(it.userId, userId)
                }
            } else {
                db.update(AuthApplicationsTable) {
                    set(it.code, code.sha256())
                    set(it.token, token)
                    set(it.datetime, LocalDateTime.now())
                    where {
                        it.userId eq userId
                    }
                }
            }

            if (apply == 1) {
                call.respondWithData(SignUpResponse(token))
                return@post
            }
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.userCreationFailed
        )
    }

    post("/auth/recover") {

    }

    post("/auth/sign_up/resend") {

    }

    post("/auth/recover/resend") {

    }
}