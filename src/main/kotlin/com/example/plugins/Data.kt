package com.example.plugins

import com.example.data.repository.ActivityRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.TripRepository
import com.example.data.repository.UserRepository
import org.ktorm.database.Database

object DatabaseConnection {
    val database = Database.connect(
        "jdbc:mysql://localhost:3306/lvl_up",
        user = "ktor_client",
        password = "TempPswd0!"
    )
}

class DataLayer {
    private val db = DatabaseConnection.database
    val activityRepository = ActivityRepository(db)
    val authRepository = AuthRepository(db)
    val tripRepository = TripRepository(db)
    val userRepository = UserRepository(db)
}