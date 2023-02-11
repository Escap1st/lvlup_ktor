package com.example.data.service

import com.example.data.database.table.UsersTable
import com.example.data.mapper.toResponse
import com.example.plugins.Claims
import com.example.plugins.DatabaseConnection
import com.example.plugins.getClaim
import com.example.tools.respondWithData
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*

fun Route.configureUsersService() {
    val db = DatabaseConnection.database

    authenticate(optional = true) {
        get("/v1/users/{id}") {
            val callingUserId = call.getClaim(Claims.userId)

            val user = db.from(UsersTable)
                .select()
                .where(UsersTable.id eq call.parameters["id"]!!)
                .map { UsersTable.createEntity(it) }
                .single()
                .toResponse()


            call.respondWithData(user)
        }
    }
}