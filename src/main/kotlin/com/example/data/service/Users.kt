package com.example.data.service

import com.example.data.mapper.toResponse
import com.example.data.model.response.ErrorDescriptions
import com.example.data.repository.UserRepository
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.configureUsersService(repository: UserRepository) {
    authenticate(optional = true) {
        get("/v1/users/{id}") {
//            val callingUserId = call.getClaim(Claims.userId)
            val user = repository.getUserById(call.parameters["id"]!!)?.toResponse()
            if (user == null) {
                call.respondWithError(HttpStatusCode.NotFound, ErrorDescriptions.noUserFound)
            } else {
                call.respondWithData(user)
            }
        }
    }
}