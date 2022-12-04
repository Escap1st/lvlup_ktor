package com.example.data.service

import com.example.data.database.table.ActivityTable
import com.example.data.database.table.UsersActivitiesTable
import com.example.data.request.SetActivitiesRequest
import com.example.data.response.ErrorDescriptions
import com.example.plugins.Claims
import com.example.plugins.DatabaseConnection
import com.example.plugins.getClaim
import com.example.tools.respondSuccess
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select

fun Route.configureProfileService() {
    val db = DatabaseConnection.database

    authenticate {
        post("/v1/profile/activities") {
            val userId = call.getClaim(Claims.userId)
            val request = call.receive<SetActivitiesRequest>()

            val activities = db.from(ActivityTable)
                .select()
                .map { ActivityTable.createEntity(it) }

            request.activities.forEach { activityId ->
                if (activities.none { it.id == activityId }) {
                    call.respondWithError(
                        HttpStatusCode.NotFound,
                        ErrorDescriptions.noActivityFound,
                    )
                    return@post
                }

                db.insert(UsersActivitiesTable) {
                    set(it.userId, userId)
                    set(it.activityId, activityId)
                }
            }

            call.respondSuccess()
        }
    }
}