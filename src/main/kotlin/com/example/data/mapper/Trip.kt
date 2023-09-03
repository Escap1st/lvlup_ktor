package com.example.data.mapper

import com.example.data.database.entity.*
import com.example.data.model.dto.TripRatingDto
import com.example.data.model.response.*
import java.time.format.DateTimeFormatter

fun TripEntity.toResponse(
    rating: TripRatingDto,
    isFavoriteTrip: Boolean = false,
    isFavoritePlace: Boolean? = null,
    recentParticipants: List<UsersEntity>? = null,
    participantsCount: Int? = null,
    schedules: List<Pair<TripScheduleEntity, List<TripScheduleEntryEntity>>>? = null,
    provisions: List<TripProvisionEntity>? = null,
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
        "/static/mountains_view_1.jpg",
        rating.toResponse(),
        descriptionShort,
        listOf("/static/mountains_view_1.jpg", "/static/mountains_view_2.jpg"),
        participantsCount,
        recentParticipants?.map { it.toResponse() },
        isFavoritePlace,
        descriptionFull,
        provisions?.map { it.toResponse() },
        accommodation,
        listOf("/static/mountains_view_1.jpg", "/static/mountains_view_2.jpg"),
        listOf("/static/hotel_room_1.jpg", "/static/hotel_room_2.jpg"),
        schedules?.map { it.first.toResponse(it.second) },
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
        timeFrom?.format(DateTimeFormatter.ISO_TIME),
        timeTo?.format(DateTimeFormatter.ISO_TIME),
    )
}

fun TripRatingDto.toResponse(): TripRatingResponse {
    return TripRatingResponse(
        overall,
        count,
        fromCurrentUser,
    )
}