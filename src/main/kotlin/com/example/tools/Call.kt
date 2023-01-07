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

suspend inline fun <reified T> ApplicationCall.respondWithData(data: T, headers: Map<String, String>? = null) {
    headers?.let {
        it.forEach { entry -> response.header(entry.key, entry.value) }
    }
    respond(
        HttpStatusCode.OK,
        Wrapper<T>(
            success = true,
            data = data
        )
    )
}

suspend inline fun ApplicationCall.respondSuccess() {
    respond(
        HttpStatusCode.OK,
        Wrapper<String>(
            success = true
        )
    )
}