package com.example.data.service

import com.example.data.response.ErrorDescriptions
import com.example.plugins.DatabaseConnection
import com.example.plugins.respondWithTokens
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import java.util.*

fun Route.configureProfileService() {
    val db = DatabaseConnection.database

    authenticate {
        get("/profile") {
            val principal = call.principal<JWTPrincipal>()
            /*if (principal == null) {
                call.respondWithError(
                    HttpStatusCode.Unauthorized,
                    ErrorDescriptions.unauthorizedUser
                )
            }

            val expiresAt = principal?.expiresAt
            if (expiresAt?.before(Date()) != false) {
                call.respondWithError(
                    HttpStatusCode.Forbidden,
                    ErrorDescriptions.outdatedToken
                )
            }*/

            val userId = principal!!.payload.getClaim("user_id").asString()
            call.respondWithData("123")
        }
    }
}