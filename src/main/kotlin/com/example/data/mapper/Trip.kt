package com.example.data.mapper

import com.example.data.database.entity.TripEntity
import com.example.data.response.TripResponse
import java.time.format.DateTimeFormatter

fun TripEntity.toResponse(): TripResponse {
    return TripResponse(
        id,
        title,
        description,
        startDate.format(DateTimeFormatter.ISO_DATE_TIME),
        finishDate.format(DateTimeFormatter.ISO_DATE_TIME)
    )
}