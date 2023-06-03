package com.example.data.repository

import com.example.data.database.entity.TripEntity
import com.example.data.database.entity.TripProvisionEntity
import com.example.data.database.entity.TripRatingEntity
import com.example.data.database.table.*
import org.ktorm.database.Database
import org.ktorm.dsl.*

class TripRepository(private val db: Database) {
    fun getAllTrips(): List<TripEntity> = db.from(TripTable)
        .select()
        .map { TripTable.createEntity(it) }

    fun getTripById(tripId: String): TripEntity? = db.from(TripTable)
        .select()
        .where { TripTable.id eq tripId }
        .map { TripTable.createEntity(it) }
        .firstOrNull()

    fun getTripRatings(tripId: String): List<TripRatingEntity> = db.from(TripsRatingsTable)
        .select()
        .where(TripsRatingsTable.tripId eq tripId)
        .map { TripsRatingsTable.createEntity(it) }

    fun getOverallTripRating(tripId: String): Double {
        val ratings = getTripRatings(tripId)
        return getOverallTripRating(ratings)
    }

    fun getOverallTripRating(ratings: List<TripRatingEntity>): Double {
        return if (ratings.isEmpty()) {
            0.0
        } else {
            ratings.sumOf { it.rating }.toDouble() / ratings.size
        }
    }

    fun getPersonalTripRating(tripId: String, userId: String): Int = db.from(TripsRatingsTable)
        .select()
        .where((TripsRatingsTable.tripId eq tripId) and (TripsRatingsTable.userId eq userId))
        .map { TripsRatingsTable.createEntity(it) }
        .firstOrNull()?.rating ?: 0

    fun isFavoriteTrip(tripId: String, userId: String): Boolean = db.from(TripsLikesTable)
        .select()
        .where((TripsLikesTable.tripId eq tripId) and (TripsLikesTable.userId eq userId))
        .map { TripsLikesTable.createEntity(it) }
        .isNotEmpty()

    fun getParticipants(tripId: String): List<String> = db.from(TripsParticipantsTable)
        .select()
        .where(TripsParticipantsTable.tripId eq tripId)
        .map { TripsParticipantsTable.createEntity(it).userId }

    fun getProvisions(tripId: String): List<TripProvisionEntity> = db.from(TripsProvisionTable)
        .select()
        .where(TripsProvisionTable.tripId eq tripId)
        .map { TripsProvisionTable.createEntity(it) }
}