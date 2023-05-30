package com.example.data.database.table

import com.example.data.database.entity.*
import org.ktorm.schema.*

object TripTable : Table<TripEntity>("trips") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val title = varchar("title").bindTo { it.title }
    val activityId = int("activity_id").bindTo { it.activityId }
    val place = varchar("place").bindTo { it.place }
    val descriptionShort = text("description_short").bindTo { it.descriptionShort }
    val descriptionFull = text("description_full").bindTo { it.descriptionFull }
    val dateFrom = datetime("date_from").bindTo { it.dateFrom }
    val dateTo = datetime("date_to").bindTo { it.dateTo }
    val price = double("price").bindTo { it.price }
    val accommodation = varchar("accommodation").bindTo { it.accommodation }
}

object TripsParticipantsTable : Table<TripParticipantEntity>("trips_participants") {
    val userId = varchar("user_id").bindTo { it.userId }
    val tripId = varchar("trip_id").bindTo { it.tripId }
}

object TripsLikesTable : Table<TripLikeEntity>("trips_likes") {
    val userId = varchar("user_id").bindTo { it.userId }
    val tripId = varchar("trip_id").bindTo { it.tripId }
}

object TripsRatingsTable : Table<TripRatingEntity>("trips_ratings") {
    val userId = varchar("user_id").bindTo { it.userId }
    val tripId = varchar("trip_id").bindTo { it.tripId }
    val rating = int("rating").bindTo { it.rating }
}

object TripsProvisionTable: Table<TripProvisionEntity>("trips_provisions") {
    val id = int("id").primaryKey().bindTo { it.id }
    val tripId = varchar("trip_id").bindTo { it.tripId }
    val position = int("position").bindTo { it.position }
    val title = varchar("title").bindTo { it.title }
    val included = boolean("included").bindTo { it.included }
}

object TripsSchedulesTable: Table<TripScheduleEntity>("trips_schedules") {
    val id = int("id").primaryKey().bindTo { it.id }
    val tripId = varchar("trip_id").bindTo { it.tripId }
    val position = int("position").bindTo { it.position }
    val title = varchar("title").bindTo { it.title }
}

object TripsSchedulesEntriesTable: Table<TripScheduleEntryEntity>("trips_schedules_entries") {
    val id = int("id").primaryKey().bindTo { it.id }
    val scheduleId = int("schedule_id").bindTo { it.scheduleId }
    val position = int("position").bindTo { it.position }
    val timeFrom = time("time_from").bindTo { it.timeFrom }
    val timeTo = time("time_to").bindTo { it.timeTo }
}