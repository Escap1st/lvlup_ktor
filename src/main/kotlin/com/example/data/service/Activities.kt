package com.example.data.service

import com.example.data.database.table.ActivityTable
import com.example.data.mapper.toResponse
import com.example.data.response.ActivityListResponse
import com.example.plugins.DatabaseConnection
import com.example.tools.respondWithData
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select

fun Route.configureActivitiesService() {
    val db = DatabaseConnection.database

    get("/v1/activities") {
        val activities = db.from(ActivityTable)
            .select()
            .map { ActivityTable.createEntity(it) }
            .map { it.toResponse() }
        call.respondWithData(ActivityListResponse(activities))
    }
}
