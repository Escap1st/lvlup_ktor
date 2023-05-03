package com.example.data.service

import com.example.data.mapper.toResponse
import com.example.data.model.request.ActivityListRequest
import com.example.data.model.response.ActivityListResponse
import com.example.data.model.response.ErrorDescriptions
import com.example.data.repository.ActivityRepository
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

fun Route.configureActivitiesService() {
    authenticate(optional = true) {
        get("/v1/activities") {
            var activities = ActivityRepository
                .getAllActivities()
                .map { it.toResponse() }

            call.getClaim(Claims.userId)?.let {
                val favorites = ActivityRepository.getUserFavoritesIds(it)
                activities = activities.sortedWith { a1, a2 ->
                    when {
                        favorites.contains(a2.id) -> 1
                        favorites.contains(a1.id) -> -1
                        else -> 0
                    }
                }
            }

            call.respondWithData(ActivityListResponse(activities))
        }
    }

    authenticate {
        post("/v1/activities/favorite") {
            val userId = call.getClaim(Claims.userId)!!
            val request = call.receive<ActivityListRequest>()

            val allActivitiesIds = ActivityRepository.getAllActivitiesIds()

            request.activitiesIds.forEach { activityId ->
                if (allActivitiesIds.none { it == activityId }) {
                    call.respondWithError(
                        HttpStatusCode.NotFound,
                        ErrorDescriptions.noActivityFound,
                    )
                    return@post
                }
            }

            val userActivitiesIds = ActivityRepository.getUserFavoritesIds(userId)

            val toRemove = userActivitiesIds.filter { id -> !request.activitiesIds.contains(id) }
            if (toRemove.isNotEmpty()) ActivityRepository.deleteUserActivities(userId, toRemove)

            val toAdd = request.activitiesIds.filter { id -> !userActivitiesIds.contains(id) }
            if (toAdd.isNotEmpty()) ActivityRepository.insertUserActivities(userId, toAdd)

            call.respondSuccess()
        }
    }

    authenticate {
        get("/v1/activities/favorite") {
            val userId = call.getClaim(Claims.userId)!!
            val userFavorites = ActivityRepository.getUserFavorites(userId)
            call.respondWithData(ActivityListResponse(userFavorites.map { it.toResponse() }))
        }
    }
}
