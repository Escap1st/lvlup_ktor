package com.example.data.service

import com.example.data.database.table.TripTable
import com.example.data.mapper.toResponse
import com.example.data.response.ErrorDescriptions
import com.example.data.response.TripListResponse
import com.example.data.response.TripResponse
import com.example.plugins.DatabaseConnection
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.format.DateTimeFormatter

fun Route.configureTripsService() {
    val db = DatabaseConnection.database

    get("/trips") {
        val trips = db.from(TripTable)
            .select()
            .map { TripTable.createEntity(it) }
            .map { it.toResponse() }
        call.respondWithData(TripListResponse(trips))
    }

    get("/trips/{id}") {
        val id: String = call.parameters["id"]?.toString() ?: ""
        val trip = db.from(TripTable)
            .select()
            .where { TripTable.id eq id }
            .map { TripTable.createEntity(it) }
            .firstOrNull()
            ?.toResponse()

        if (trip == null) {
            call.respondWithError(
                HttpStatusCode.NotFound,
                ErrorDescriptions.tripNotFound
            )
        } else {
            call.respondWithData(trip)
        }
    }
}
