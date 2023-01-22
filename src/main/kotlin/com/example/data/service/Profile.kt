package com.example.data.service

import com.example.plugins.DatabaseConnection
import io.ktor.server.routing.*

fun Route.configureProfileService() {
    val db = DatabaseConnection.database
}