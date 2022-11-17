package com.example.plugins

import io.ktor.server.application.*
import org.ktorm.database.Database

object DatabaseConnection {
    val database = Database.connect(
        "jdbc:mysql://localhost:3306/lvl_up",
        user = "ktor_client",
        password = "TempPswd0!"
    )
}