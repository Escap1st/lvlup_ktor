package com.example.data.service

import com.example.data.database.entity.AuthApplicationType
import com.example.data.database.table.AuthApplicationsTable
import com.example.data.database.table.RefreshTokensTable
import com.example.data.database.table.UsersTable
import com.example.data.request.*
import com.example.data.response.AuthApplicationResponse
import com.example.data.response.ErrorDescriptions
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
            db.insert(RefreshTokensTable) {
                set(it.token, this@apply)
                set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
                set(it.userId, userId)
            }
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
        password: String? = null,
        code: String? = null
    ): String? {
        val userApplies = db.from(AuthApplicationsTable)
            .select()
            .where((AuthApplicationsTable.userId eq userId) and (AuthApplicationsTable.type eq type))
        val token = UUID.randomUUID().toString()
        val appliedCount = if (userApplies.totalRecords == 0) {
            db.insert(AuthApplicationsTable) {
                set(it.token, token)
                set(it.userId, userId)
                set(it.datetime, LocalDateTime.now())
                set(it.type, type)
                if (password != null) set(it.password, password.sha256())
                if (code != null) set(it.code, code.sha256())
            }
        } else {
            db.update(AuthApplicationsTable) {
                set(it.token, token)
                set(it.datetime, LocalDateTime.now())
                set(it.code, code?.sha256())
                where { it.userId eq userId }
                where { it.type eq type }
            }
        }

        return if (appliedCount == 1) token else null
    }

    post("/v1/auth/sign_up/init") {
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
            createNewApplication(
                AuthApplicationType.sign_up,
                userId,
                request.password,
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
        val request = call.receive<EmailConfirmationRequest>()
        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(
                (AuthApplicationsTable.token eq request.token) and
                        (AuthApplicationsTable.type eq AuthApplicationType.sign_up)
            )
            .map { AuthApplicationsTable.createEntity(it) }

        if (applications.isNotEmpty()) {
            val application = applications.first()
            if (request.code.sha256() == application.code) {
                db.delete(AuthApplicationsTable) { it.token eq request.token }
                db.update(UsersTable) {
                    set(it.password, application.password)
                    where { it.id eq application.userId }
                }
                createRefreshToken(application.userId).apply {
                    call.respondWithTokens(application.userId, this)
                    return@post
                }
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
        val users = db.from(UsersTable)
            .select()
            .where((UsersTable.email eq request.email) and (UsersTable.password eq request.password.sha256()))
            .map { UsersTable.createEntity(it) }

        if (users.size == 1) {
            val userId = users.first().id
            createRefreshToken(userId).apply {
                call.respondWithTokens(userId, this)
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
        val dbToken = db.from(RefreshTokensTable)
            .select()
            .where(
                (RefreshTokensTable.token eq request.token)
                        and (RefreshTokensTable.userId eq request.userId)
            )
            .map { RefreshTokensTable.createEntity(it) }
            .firstOrNull()

        if (dbToken?.expiresAt?.isAfter(LocalDateTime.now()) == true) {
            UUID.randomUUID().toString().apply {
                db.update(RefreshTokensTable) {
                    set(it.token, this@apply)
                    set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
                    where { it.token eq request.token }
                }
                call.respondWithTokens(request.userId, this@apply)
                return@post
            }
        }

        call.respondWithError(HttpStatusCode.BadRequest, ErrorDescriptions.invalidToken)
    }

    post("/v1/auth/recover/init") {
        val request = call.receive<AccountRecoveryInitRequest>()
        if (!EmailValidator.isEmailValid(request.email)) {
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
                ErrorDescriptions.noUserFound
            )
            return@post
        }

        createNewApplication(
            AuthApplicationType.recovery_init,
            registeredUsers.first().id,
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

    post("/v1/auth/recover/confirm") {
        val request = call.receive<EmailConfirmationRequest>()

        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(
                (AuthApplicationsTable.token eq request.token)
                        and (AuthApplicationsTable.type eq AuthApplicationType.recovery_init)
            )
            .map { AuthApplicationsTable.createEntity(it) }

        if (applications.isNotEmpty()) {
            val application = applications.first()
            if (request.code.sha256() == application.code) {
                db.delete(AuthApplicationsTable) { it.token eq request.token }
                createNewApplication(AuthApplicationType.recovery_complete, application.userId)?.let {
                    call.respondWithData(AuthApplicationResponse(it))
                    return@post
                }
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

    post("/v1/auth/recover/complete") {
        val request = call.receive<AccountRecoveryCompleteRequest>()

        if (!PasswordValidator.isPasswordValid(request.password)) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorDescriptions.invalidRecoveryData
            )
            return@post
        }

        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(
                (AuthApplicationsTable.token eq request.token)
                        and (AuthApplicationsTable.type eq AuthApplicationType.recovery_complete)
            )
            .map { AuthApplicationsTable.createEntity(it) }

        if (applications.isNotEmpty()) {
            val application = applications.first()

            val changedUsers = db.update(UsersTable) {
                set(it.password, request.password.sha256())
                where { it.id eq application.userId }
            }

            if (changedUsers == 1) {
                db.delete(AuthApplicationsTable) { it.token eq request.token }
                createRefreshToken(application.userId).apply {
                    call.respondWithTokens(application.userId, this)
                    return@post
                }
            }
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.passwordChangeFailed
        )
    }

    post("/v1/auth/resend_code") {
        val request = call.receive<CodeResendRequest>()
        val applications = db.from(AuthApplicationsTable)
            .select()
            .where(
                (AuthApplicationsTable.token eq request.token) and
                        ((AuthApplicationsTable.type eq AuthApplicationType.sign_up)
                                or (AuthApplicationsTable.type eq AuthApplicationType.recovery_init))
            )
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

            call.respondWithData(AuthApplicationResponse(newToken))
            return@post
        }

        call.respondWithError(
            HttpStatusCode.BadRequest,
            ErrorDescriptions.codeResendFailed
        )
    }
}