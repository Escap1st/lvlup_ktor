package com.example

import com.example.plugins.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    val keyStoreFile = File("keystore.jks")
    val keystore = generateCertificate(
        file = keyStoreFile,
        keyAlias = "lvl_up_ssl",
        keyPassword = "TempPswd0!",
        jksPassword = "TempPswd0!"
    )

    val environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            port = 8080
        }
        sslConnector(
            keyStore = keystore,
            keyAlias = "lvl_up_ssl",
            keyStorePassword = { "TempPswd0!".toCharArray() },
            privateKeyPassword = { "TempPswd0!".toCharArray() }) {
            port = 8443
            keyStorePath = keyStoreFile
        }
        module(Application::module)
    }

    embeddedServer(Netty, environment = environment)
        .start(wait = true)
}

fun Application.module() {
    val dataLayer = DataLayer()
    configureSecurity(dataLayer.userRepository)
    configureSerialization()
    configureHTTP()
    configureRouting(dataLayer)
}
