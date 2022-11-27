package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.database.table.UsersTable
import com.example.data.response.ErrorDescriptions
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
import org.ktorm.dsl.*
import java.lang.NullPointerException
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
                    ErrorDescriptions.unauthorizedUser
                )
            }
            validate { credential ->
                val db = DatabaseConnection.database
                val userId = credential.payload.getClaim(Claims.userId).asString()
                if (userId != "") {
                    val version = db.from(UsersTable)
                        .select(UsersTable.password)
                        .where { UsersTable.id eq userId }
                        .map { it[UsersTable.password] }
                        .firstOrNull()
                        ?.sha256()

                    if (version == credential.payload.getClaim(Claims.version).asString()) {
                        return@validate JWTPrincipal(credential.payload)
                    }
                }

                null
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
    val version = DatabaseConnection.database
        .from(UsersTable)
        .select(UsersTable.password)
        .where { UsersTable.id eq userId }
        .map { it[UsersTable.password] }
        .first()!!

    val token = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim(Claims.userId, userId)
        .withClaim(Claims.version, version.sha256())
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

class Claims {
    companion object {
        const val userId = "user_id"
        const val version = "version"
    }
}