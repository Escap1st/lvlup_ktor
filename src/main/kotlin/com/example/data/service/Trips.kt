package com.example.data.service

import com.example.data.mapper.toResponse
import com.example.data.model.response.ErrorDescriptions
import com.example.data.model.response.TripListResponse
import com.example.data.repository.TripRepository
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.configureTripsService(repository: TripRepository) {
    authenticate(optional = true) {
        get("/v1/trips") {
            val trips = repository.getAllTrips().map { it.toResponse() }
            call.respondWithData(TripListResponse(trips))
        }
    }

    get("/v1/trips/{id}") {
        val id: String = call.parameters["id"] ?: ""
        val trip = repository.getTripById(id)

        if (trip == null) {
            call.respondWithError(
                HttpStatusCode.NotFound,
                ErrorDescriptions.tripNotFound
            )
        } else {
            call.respondWithData(trip.toResponse())
        }
    }
}
