package com.example.data.repository

import com.example.data.database.entity.*
import com.example.data.database.table.*
import com.example.data.model.dto.TripRatingDto
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

    fun getTripRatingInfo(tripId: String, userId: String?): TripRatingDto {
        val ratingsList = getTripRatingsList(tripId)
        return TripRatingDto(
            getOverallTripRating(ratingsList),
            ratingsList.size,
            if (userId != null) getPersonalTripRating(tripId, userId) else null
        )
    }

    private fun getTripRatingsList(tripId: String): List<TripRatingEntity> = db.from(TripsRatingsTable)
        .select()
        .where(TripsRatingsTable.tripId eq tripId)
        .map { TripsRatingsTable.createEntity(it) }

    private fun getOverallTripRating(ratings: List<TripRatingEntity>): Double {
        return if (ratings.isEmpty()) {
            0.0
        } else {
            ratings.sumOf { it.rating }.toDouble() / ratings.size
        }
    }

    private fun getPersonalTripRating(tripId: String, userId: String): Int = db.from(TripsRatingsTable)
        .select()
        .where((TripsRatingsTable.tripId eq tripId) and (TripsRatingsTable.userId eq userId))
        .map { TripsRatingsTable.createEntity(it) }
        .firstOrNull()?.rating ?: 0

    fun isFavoriteTrip(tripId: String, userId: String?): Boolean = if (userId != null)
        db.from(TripsLikesTable)
            .select()
            .where((TripsLikesTable.tripId eq tripId) and (TripsLikesTable.userId eq userId))
            .map { TripsLikesTable.createEntity(it) }
            .isNotEmpty()
    else
        false

    fun getParticipants(tripId: String): List<String> = db.from(TripsParticipantsTable)
        .select()
        .where(TripsParticipantsTable.tripId eq tripId)
        .map { TripsParticipantsTable.createEntity(it).userId }

    fun getProvisions(tripId: String): List<TripProvisionEntity> = db.from(TripsProvisionTable)
        .select()
        .where(TripsProvisionTable.tripId eq tripId)
        .map { TripsProvisionTable.createEntity(it) }

    fun getSchedules(tripId: String): List<Pair<TripScheduleEntity, List<TripScheduleEntryEntity>>> {
        val schedules = db.from(TripsSchedulesTable)
            .select()
            .where(TripsSchedulesTable.tripId eq tripId)
            .map { TripsSchedulesTable.createEntity(it) }

        return schedules.map { Pair(it, getScheduleEntries(it.id)) }
    }

    private fun getScheduleEntries(scheduleId: Int) = db.from(TripsSchedulesEntriesTable)
        .select()
        .where(TripsSchedulesEntriesTable.scheduleId eq scheduleId)
        .map { TripsSchedulesEntriesTable.createEntity(it) }
}