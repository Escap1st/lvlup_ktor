package com.example.data.response

import kotlinx.serialization.Serializable

@Serializable
data class TripListResponse(
    val trips: List<TripResponse>
)

@Serializable
data class TripResponse(
    val id: Int,
    val title: String,
    val description: String,
    val startDate: String,
    val finishDate: String
)