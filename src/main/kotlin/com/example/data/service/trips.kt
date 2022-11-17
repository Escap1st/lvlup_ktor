package com.example.data.service

import com.example.data.entity.TripEntity
import com.example.data.response.ErrorDescription
import com.example.data.response.TripListResponse
import com.example.data.response.TripResponse
import com.example.data.response.Wrapper
import com.example.plugins.DatabaseConnection
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
                it[TripEntity.startDate]!!.format(DateTimeFormatter.ISO_DATE),
                it[TripEntity.finishDate]!!.format(DateTimeFormatter.ISO_DATE),
            )
        }
        call.respond(
            HttpStatusCode.OK,
            Wrapper(
                success = true,
                data = TripListResponse(trips)
            )
        )
    }

    get("/trips/{id}") {
        val id: Int = call.parameters["id"]?.toInt() ?: -1
        val trip = db.from(TripEntity)
            .select()
            .where { TripEntity.id eq id }
            .map {
                TripResponse(
                    it[TripEntity.id]!!,
                    it[TripEntity.title]!!,
                    it[TripEntity.description]!!,
                    it[TripEntity.startDate]!!.format(DateTimeFormatter.ISO_DATE),
                    it[TripEntity.finishDate]!!.format(DateTimeFormatter.ISO_DATE),
                )
            }.firstOrNull()
        if (trip == null) {
            call.respond(
                HttpStatusCode.NotFound,
                Wrapper<String>(success = false, error = ErrorDescription("TRIP_NOT_FOUND", "No such trip"))
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                Wrapper(
                    success = true,
                    data = trip
                )
            )
        }
    }
}
