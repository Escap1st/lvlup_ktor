package com.example.data.service

import com.example.data.mapper.toResponse
import com.example.data.model.response.ErrorDescriptions
import com.example.data.model.response.TripListResponse
import com.example.data.repository.TripRepository
import com.example.data.repository.UserRepository
import com.example.plugins.Claims
import com.example.plugins.getClaim
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.configureTripsService(tripRepository: TripRepository, userRepository: UserRepository) {
    authenticate(optional = true) {
        get("/v1/trips") {
            val userId = call.getClaim(Claims.userId)

            val trips = tripRepository.getAllTrips().map {
                val participants = tripRepository.getParticipants(it.id)
                it.toResponse(
                    tripRepository.getSchedules(it.id),
                    tripRepository.getProvisions(it.id),
                    tripRepository.getTripRatingInfo(it.id, userId),
                    tripRepository.isFavoriteTrip(it.id, userId),
                    false,
                    participants.shuffled()
                        .take(5)
                        .mapNotNull { participantId ->
                            userRepository.getUserById(participantId)
                        },
                    participants.size,
                )
            }
            call.respondWithData(TripListResponse(trips))
        }
    }

    authenticate(optional = true) {
        get("/v1/trips/{id}") {
            val tripId: String = call.parameters["id"] ?: ""
            val userId = call.getClaim(Claims.userId)
            val trip = tripRepository.getTripById(tripId)

            if (trip == null) {
                call.respondWithError(
                    HttpStatusCode.NotFound,
                    ErrorDescriptions.tripNotFound
                )
            } else {
                call.respondWithData(
                    trip.toResponse(
                        tripRepository.getSchedules(tripId),
                        tripRepository.getProvisions(tripId),
                        tripRepository.getTripRatingInfo(tripId, userId),
                    ),
                )
            }
        }
    }
}
