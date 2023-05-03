package com.example.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripListResponse(
    val trips: List<TripResponse>
)

@Serializable()
data class TripResponse(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("finish_date") val finishDate: String
)