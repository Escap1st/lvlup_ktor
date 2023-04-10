package com.example.data.service

import com.example.data.database.table.ActivityTable
import com.example.data.database.table.UsersActivitiesTable
import com.example.data.mapper.toResponse
import com.example.data.request.ActivityListRequest
import com.example.data.response.ActivityListResponse
import com.example.data.response.ErrorDescriptions
import com.example.plugins.Claims
import com.example.plugins.DatabaseConnection
import com.example.plugins.getClaim
import com.example.tools.respondSuccess
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*

fun Route.configureActivitiesService() {
    val db = DatabaseConnection.database

    fun getUserFavorites(userId: String): List<Int> {
        return db.from(UsersActivitiesTable)
            .select()
            .where(UsersActivitiesTable.userId eq userId)
            .map { UsersActivitiesTable.createEntity(it) }
            .map { activity -> activity.activityId }
    }

    authenticate(optional = true) {
        get("/v1/activities") {
            var activities = db.from(ActivityTable)
                .select()
                .map { ActivityTable.createEntity(it) }
                .map { it.toResponse() }

            call.getClaim(Claims.userId)?.let {
                val favorites = getUserFavorites(it)
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

            val allActivitiesIds = db.from(ActivityTable)
                .select()
                .map { ActivityTable.createEntity(it) }

            request.activitiesIds.forEach { activityId ->
                if (allActivitiesIds.none { it.id == activityId }) {
                    call.respondWithError(
                        HttpStatusCode.NotFound,
                        ErrorDescriptions.noActivityFound,
                    )
                    return@post
                }
            }

            val userActivitiesIds = db.from(UsersActivitiesTable)
                .select()
                .where(UsersActivitiesTable.userId eq userId)
                .map { UsersActivitiesTable.createEntity(it) }
                .map { activity -> activity.activityId }

            val toRemove = userActivitiesIds.filter { id -> !request.activitiesIds.contains(id) }
            if (toRemove.isNotEmpty()) db.delete(UsersActivitiesTable) { it.activityId inList toRemove }

            val toAdd = request.activitiesIds.filter { id -> !userActivitiesIds.contains(id) }
            if (toAdd.isNotEmpty()) {
                db.batchInsert(UsersActivitiesTable) {
                    toAdd.map { id ->
                        item {
                            set(it.userId, userId)
                            set(it.activityId, id)
                        }
                    }
                }
            }

            call.respondSuccess()
        }
    }

    authenticate {
        get("/v1/activities/favorite") {
            val userId = call.getClaim(Claims.userId)!!

            val userFavorites = getUserFavorites(userId)

            val activities = db.from(ActivityTable)
                .select()
                .where(ActivityTable.id inList userFavorites)
                .map { ActivityTable.createEntity(it) }
                .map { it.toResponse() }

            call.respondWithData(ActivityListResponse(activities))
        }
    }
}
