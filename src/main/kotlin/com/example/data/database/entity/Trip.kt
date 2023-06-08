package com.example.data.database.entity

import org.ktorm.entity.Entity
import java.time.LocalDateTime
import java.time.LocalTime

interface TripEntity : Entity<TripEntity> {
    val id: String
    val title: String
    val activityId: Int
    val place: String
    val dateFrom: LocalDateTime
    val dateTo: LocalDateTime
    val price: Double
    val descriptionShort: String
    val descriptionFull: String?
    val accommodation: String?
}

interface TripLikeEntity: Entity<TripLikeEntity>{
    val userId: String
    val tripId: String
}

interface TripParticipantEntity: Entity<TripParticipantEntity>{
    val userId: String
    val tripId: String
}

interface TripRatingEntity: Entity<TripRatingEntity>{
    val userId: String
    val tripId: String
    val rating: Int
}

interface TripProvisionEntity: Entity<TripProvisionEntity> {
    val id: Int
    val tripId: String
    val position: Int
    val title: String
    val included: Boolean
}

interface TripScheduleEntity: Entity<TripScheduleEntity> {
    val id: Int
    val tripId: String
    val position: Int
    val title: String
}

interface TripScheduleEntryEntity: Entity<TripScheduleEntryEntity> {
    val id: Int
    val title: String
    val scheduleId: Int
    val position: Int
    val timeFrom: LocalTime?
    val timeTo: LocalTime?
}