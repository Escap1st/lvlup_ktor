package com.example.data.database.entity

import org.ktorm.entity.Entity
import java.time.LocalDateTime

interface TripEntity : Entity<TripEntity> {
    val id: String
    val title: String
    val description: String
    val startDate: LocalDateTime
    val finishDate: LocalDateTime
}