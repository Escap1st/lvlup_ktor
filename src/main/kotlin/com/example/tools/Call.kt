package com.example.tools

import com.example.data.response.ErrorDescription
import com.example.data.response.Wrapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondWithError(httpCode: HttpStatusCode, description: ErrorDescription) {
    respond(
        httpCode,
        Wrapper<String>(
            success = false,
            error = description
        )
    )
}

suspend inline fun <reified T> ApplicationCall.respondWithData(data: T) {
    respond(
        HttpStatusCode.OK,
        Wrapper<T>(
            success = true,
            data = data
        )
    )
}