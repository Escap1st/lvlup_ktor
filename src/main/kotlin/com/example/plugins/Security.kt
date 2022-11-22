package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.response.TokensResponse
import com.example.data.response.Wrapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun Application.configureSecurity() {
    val secret = "secret" //this@configureSecurity.environment.config.property("jwt.secret").getString()
    val issuer = "http://lvl_up.com" //this@configureSecurity.environment.config.property("jwt.domain").getString()
    val audience = "http://lvl_up.com" //this@configureSecurity.environment.config.property("jwt.audience").getString()

    authentication {
        jwt {
            realm = "Access to 'hello'" //this@configureSecurity.environment.config.property("jwt.realm").getString()

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("user_id").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }


        }
    }

    routing {
        post("/auth/email_confirmation") {
//            UUID.randomUUID()
//            val user = call.receive<User>()
            // Check username and password
            // ...
            respondWithTokens(call, audience, issuer, secret)
        }

        post("/auth/login") {
//            val user = call.receive<User>()
            // Check username and password
            // ...
            respondWithTokens(call, audience, issuer, secret)
        }

        post("/auth/refresh") {
//            val user = call.receive<User>()
            // Check username and password
            // ...
            respondWithTokens(call, audience, issuer, secret)
        }
    }
}

suspend fun respondWithTokens(call: ApplicationCall, audience: String, issuer: String, secret: String) {
    val issuedAt = Date()
    val expiresAt = Date(issuedAt.time + 180000)
    val accessToken = generateToken(audience, issuer, secret, issuedAt, expiresAt)
    val refreshToken = generateToken(audience, issuer, secret, issuedAt)
    val tokensResponse = TokensResponse(
        accessToken,
        refreshToken,
        ZonedDateTime.ofInstant(issuedAt.toInstant(), ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME),
        ZonedDateTime.ofInstant(expiresAt.toInstant(), ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME)
    )
    call.respond(
        HttpStatusCode.OK,
        Wrapper(
            success = true,
            data = tokensResponse
        )
    )
}

fun generateToken(audience: String, issuer: String, secret: String, issuedAt: Date, expiresAt: Date? = null): String {
    val token = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("user_id", "123")
        .withIssuedAt(issuedAt)
    if (expiresAt != null) token.withExpiresAt(expiresAt)
    return token.sign(Algorithm.HMAC256(secret))
}

fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}