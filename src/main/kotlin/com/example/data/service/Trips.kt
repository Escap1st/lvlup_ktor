package com.example.data.service

import com.example.data.mapper.toResponse
import com.example.data.model.request.ActivityListRequest
import com.example.data.model.request.TripLikeRequest
import com.example.data.model.response.ErrorDescriptions
import com.example.data.model.response.TripListResponse
import com.example.data.repository.TripRepository
import com.example.data.repository.UserRepository
import com.example.plugins.Claims
import com.example.plugins.getClaim
import com.example.tools.respondSuccess
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.configureTripsService(tripRepository: TripRepository, userRepository: UserRepository) {
    authenticate(optional = true) {
        get("/v1/trips") {
            val userId = call.getClaim(Claims.userId)

            val trips = tripRepository.getAllTrips().map {
                it.toResponse(
                    tripRepository.getSchedules(it.id),
                    tripRepository.getProvisions(it.id),
                    tripRepository.getTripRatingInfo(it.id, userId),
                    tripRepository.isFavoriteTrip(it.id, userId),
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
                val participants = tripRepository.getParticipants(tripId)
                call.respondWithData(
                    trip.toResponse(
                        tripRepository.getSchedules(tripId),
                        tripRepository.getProvisions(tripId),
                        tripRepository.getTripRatingInfo(tripId, userId),
                        tripRepository.isFavoriteTrip(tripId, userId),
                        false,
                        participants.shuffled()
                            .take(5)
                            .mapNotNull { participantId ->
                                userRepository.getUserById(participantId)
                            },
                        participants.size,
                    ),
                )
            }
        }
    }

    authenticate {
        post("v1/trips/favorites"){
            val userId = call.getClaim(Claims.userId)!!
            val request = call.receive<TripLikeRequest>()

            if (request.liked xor tripRepository.isFavoriteTrip(request.tripId, userId)) {
                tripRepository.setTripFavorite(request.tripId, userId, request.liked)
            }

            call.respondSuccess()
        }
    }
}
