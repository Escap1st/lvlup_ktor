package com.example.data.service

import com.example.plugins.DatabaseConnection
import com.example.tools.respondWithData
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.configureUsersService() {
    val db = DatabaseConnection.database

    authenticate(optional = true) {
        get("/v1/users/{id}") {
            call.respondWithData("123")
        }
    }
}