package com.example.data.service

import com.example.data.entity.TripEntity
import com.example.data.response.ErrorDescription
import com.example.data.response.TripListResponse
import com.example.data.response.TripResponse
import com.example.data.response.Wrapper
import com.example.plugins.DatabaseConnection
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.format.DateTimeFormatter

fun Route.configureTripsService() {
    val db = DatabaseConnection.database

    get("/trips") {
        val trips = db.from(TripEntity).select().map {
            TripResponse(
                it[TripEntity.id]!!,
                it[TripEntity.title]!!,
                it[TripEntity.description]!!,
                it[TripEntity.startDate]!!.format(DateTimeFormatter.ISO_DATE_TIME),
                it[TripEntity.finishDate]!!.format(DateTimeFormatter.ISO_DATE_TIME),
            )
        }
        call.respondWithData(TripListResponse(trips))
    }

    get("/trips/{id}") {
        val id: String = call.parameters["id"]?.toString() ?: ""
        val trip = db.from(TripEntity)
            .select()
            .where { TripEntity.id eq id }
            .map {
                TripResponse(
                    it[TripEntity.id]!!,
                    it[TripEntity.title]!!,
                    it[TripEntity.description]!!,
                    it[TripEntity.startDate]!!.format(DateTimeFormatter.ISO_DATE_TIME),
                    it[TripEntity.finishDate]!!.format(DateTimeFormatter.ISO_DATE_TIME),
                )
            }.firstOrNull()
        if (trip == null) {
            call.respondWithError(
                HttpStatusCode.NotFound,
                "TRIP_NOT_FOUND",
                "No such trip"
            )
        } else {
            call.respondWithData(trip)
        }
    }
}
