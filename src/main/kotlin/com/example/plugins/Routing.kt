package com.example.plugins

import com.example.data.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(dataLayer: DataLayer) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        static("/static") {
            resources("static")
        }

        configureTripsService(dataLayer.tripRepository)
        configureActivitiesService(dataLayer.activityRepository)
        configureUsersService(dataLayer.userRepository)
        configureAuthService(dataLayer.authRepository, dataLayer.userRepository)
    }
}
