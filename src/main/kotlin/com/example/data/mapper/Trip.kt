package com.example.data.mapper

import com.example.data.database.entity.*
import com.example.data.model.response.*
import java.time.format.DateTimeFormatter

fun TripEntity.toResponse(
    schedules: List<Pair<TripScheduleEntity, List<TripScheduleEntryEntity>>>,
    provisions: List<TripProvisionEntity>,
    overallRating: Double,
    ratingsCount: Int,
    currentUserRating: Int?,
    isFavoriteTrip: Boolean,
    isFavoritePlace: Boolean,
    recentParticipants: List<UsersEntity>,
    participantsCount: Int,
): TripResponse {
    return TripResponse(
        id,
        title,
        place,
        activityId,
        price,
        dateFrom.format(DateTimeFormatter.ISO_DATE_TIME),
        dateTo.format(DateTimeFormatter.ISO_DATE_TIME),
        isFavoriteTrip,
        "/static/mountains_view.jpg",
        TripRatingResponse(overallRating, ratingsCount, currentUserRating),
        descriptionShort,
        listOf("/static/mountains_view.jpg"),
        participantsCount,
        recentParticipants.map { it.toResponse() },
        isFavoritePlace,
        descriptionFull,
        provisions.map { it.toResponse() },
        accommodation,
        listOf("/static/mountains_view.jpg"),
        listOf("/static/hotel_room.jpg"),
        schedules.map { it.first.toResponse(it.second) },
    )
}

fun TripProvisionEntity.toResponse(): TripProvisionResponse {
    return TripProvisionResponse(
        title,
        included
    )
}

fun TripScheduleEntity.toResponse(entries: List<TripScheduleEntryEntity>): TripScheduleResponse {
    return TripScheduleResponse(
        title,
        entries.map { it.toResponse() }
    )
}

fun TripScheduleEntryEntity.toResponse(): TripScheduleEntryResponse {
    return TripScheduleEntryResponse(
        title,
        timeFrom?.format(DateTimeFormatter.ISO_DATE_TIME),
        timeTo?.format(DateTimeFormatter.ISO_DATE_TIME),
    )
}