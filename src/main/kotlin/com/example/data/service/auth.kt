package com.example.data.service

import com.example.data.entity.AuthApplicationEntity
import com.example.data.entity.AuthApplicationType
import com.example.data.entity.UserEntity
import com.example.data.request.LoginRequest
import com.example.data.request.SignUpRequest
import com.example.data.response.SignUpResponse
import com.example.data.response.UserResponse
import com.example.plugins.DatabaseConnection
import com.example.plugins.respondWithTokens
import com.example.plugins.sha256
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

    post("/auth/sign_up/confirm") {
//            UUID.randomUUID()
//            val user = call.receive<User>()
        // Check username and password
        // ...
        respondWithTokens(call)
    }

    post("/auth/recover/confirm") {
//            UUID.randomUUID()
//            val user = call.receive<User>()
        // Check username and password
        // ...
        respondWithTokens(call)
    }

    post("/auth/login") {
        val request = call.receive<LoginRequest>()
//            val user = call.receive<User>()
        // Check username and password
        // ...
        respondWithTokens(call)
    }

    post("/auth/refresh") {
//            val user = call.receive<User>()
        // Check username and password
        // ...
        respondWithTokens(call)
    }

    post("/auth/sign_up") {
        val request = call.receive<SignUpRequest>()
        if (request.name.isBlank() || request.surname.isBlank() ||
            //TODO validate email and password
            request.email.isBlank() || request.password.isBlank()
        ) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                "INVALID_SIGN_UP_DATA",
                "Check the data you provided to sign up",
            )
            return@post
        }

        val registeredUsers = db.from(UserEntity)
            .select()
            .where(UserEntity.email eq request.email)
            .map {
                UserResponse(
                    it[UserEntity.id]!!,
                    it[UserEntity.name]!!,
                    it[UserEntity.surname]!!,
                    it[UserEntity.email]!!,
                    it[UserEntity.password]!!,
                    it[UserEntity.confirmed]!!,
                )
            }

        if (registeredUsers.firstOrNull()?.confirmed == true) {
            call.respondWithError(
                HttpStatusCode.BadRequest,
                "USED_IS_REGISTERED",
                "User is already registered",
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
            db.update(UserEntity) {
                set(it.name, request.name)
                set(it.surname, request.surname)
                set(it.password, request.password.sha256())
                where {
                    it.email eq request.email
                }
            }
        } else {
            db.insert(UserEntity) {
                set(it.id, userId)
                set(it.name, request.name)
                set(it.surname, request.surname)
                set(it.email, request.email)
                set(it.password, request.password.sha256())
            }
        }

        if (insertUser == 1) {
            val userApplies = db.from(AuthApplicationEntity)
                .select()
                .where(AuthApplicationEntity.userId eq userId)
                .where(AuthApplicationEntity.type eq AuthApplicationType.sign_up)
            val token = UUID.randomUUID().toString()
            val code = (0..9999).random().toString().padStart(4, '0').sha256()
            val apply = if (userApplies.totalRecords == 0) {
                db.insert(AuthApplicationEntity) {
                    set(it.code, code)
                    set(it.token, token)
                    set(it.datetime, LocalDateTime.now())
                    set(it.type, AuthApplicationType.sign_up)
                    set(it.userId, userId)
                }
            } else {
                db.update(AuthApplicationEntity) {
                    set(it.code, code)
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
            "FAILED_TO_ADD_USER",
            "Failed to add user",
        )
    }

    post("/auth/recover") {

    }
}