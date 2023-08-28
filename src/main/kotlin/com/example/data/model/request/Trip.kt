package com.example.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class TripLikeRequest(
    val tripId: String,
    val liked: Boolean,
)