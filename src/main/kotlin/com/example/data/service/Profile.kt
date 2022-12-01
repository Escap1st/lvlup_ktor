package com.example.data.service

import com.example.plugins.Claims
import com.example.plugins.DatabaseConnection
import com.example.plugins.getClaim
import com.example.tools.respondWithData
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.configureProfileService() {
    val db = DatabaseConnection.database

    authenticate {
        get("/v1/profile") {
            val userId = call.getClaim(Claims.userId)
            call.respondWithData("123")
        }
    }
}