package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.response.TokensResponse
import com.example.plugins.SecurityConfig.audience
import com.example.plugins.SecurityConfig.issuer
import com.example.plugins.SecurityConfig.secret
import com.example.tools.respondWithData
import com.example.tools.respondWithError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object SecurityConfig {
    const val secret = "secret" //this@configureSecurity.environment.config.property("jwt.secret").getString()
    const val issuer =
        "http://lvl_up.com" //this@configureSecurity.environment.config.property("jwt.domain").getString()
    const val audience =
        "http://lvl_up.com" //this@configureSecurity.environment.config.property("jwt.audience").getString()
}

fun Application.configureSecurity() {
    authentication {
        jwt {
//            realm = "Access to 'hello'" //this@configureSecurity.environment.config.property("jwt.realm").getString()

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            challenge { _, _ ->
                call.respondWithError(
                    HttpStatusCode.Unauthorized,
                    "UNAUTHORIZED_USER",
                    "Authenticate to access this data"
                )
            }
            validate { credential ->
                if (credential.payload.getClaim("user_id").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }


        }
    }
}

suspend fun ApplicationCall.respondWithTokens(userId: String) {
    val issuedAt = Date()
    val expiresAt = Date(issuedAt.time + 180000)
    val accessToken = generateToken(audience, issuer, secret, userId, issuedAt, expiresAt)
    val refreshToken = generateToken(audience, issuer, secret, userId, issuedAt)
    val tokensResponse = TokensResponse(
        accessToken,
        refreshToken,
        issuedAt.formatZoned(),
        expiresAt.formatZoned(),
    )
    respondWithData(tokensResponse)
}

fun Date.formatZoned(): String {
    return ZonedDateTime.ofInstant(toInstant(), ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME)
}

fun generateToken(
    audience: String,
    issuer: String,
    secret: String,
    userId: String,
    issuedAt: Date,
    expiresAt: Date? = null
): String {
    val token = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("user_id", userId)
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