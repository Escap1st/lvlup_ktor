package com.example.data.model.dto

data class TripRatingDto(
    val overall: Double,
    val count: Int,
    val fromCurrentUser: Int?,
)