package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.model.response.ErrorDescriptions
import com.example.data.model.response.TokensResponse
import com.example.data.repository.UserRepository
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

fun Application.configureSecurity(repository: UserRepository) {
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
                val userId = credential.payload.getClaim(Claims.userId)?.asString()
                if (!userId.isNullOrEmpty()) {
                    val user = repository.getUserById(userId)

                    if (user?.password != null &&
                        (userId + user.password!!).sha256() == credential.payload.getClaim(Claims.version).asString()
                    ) {
                        return@validate JWTPrincipal(credential.payload)
                    }
                }

                null
            }
        }
    }
}

suspend fun ApplicationCall.respondWithTokens(repository: UserRepository, userId: String, refreshToken: String) {
    val issuedAt = Date()
    val accessTokenTtl = 3 * 60 * 1000L // 3 minutes
    val expiresAt = Date(issuedAt.time + accessTokenTtl)
    val accessToken = generateToken(repository, audience, issuer, secret, userId, issuedAt, expiresAt)
    val tokensResponse = TokensResponse(
        userId,
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
    repository: UserRepository,
    audience: String,
    issuer: String,
    secret: String,
    userId: String,
    issuedAt: Date,
    expiresAt: Date,
): String {
    val version = userId + repository.getUsersPassword(userId)

    val token = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim(Claims.userId, userId)
        .withClaim(Claims.version, version.sha256())
        .withIssuedAt(issuedAt)
        .withExpiresAt(expiresAt)
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

fun ApplicationCall.getClaim(claim: String): String? {
    val requiredClaim = principal<JWTPrincipal>()?.payload?.getClaim(claim)
    return if (requiredClaim?.isNull != false) null else requiredClaim.asString()
}