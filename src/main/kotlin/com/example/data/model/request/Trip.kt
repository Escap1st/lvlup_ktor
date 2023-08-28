package com.example.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripLikeRequest(
    @SerialName("trip_id") val tripId: String,
    val liked: Boolean,
)