package com.example.data.request

import kotlinx.serialization.Serializable

@Serializable
data class ActivityListRequest(
    val activitiesIds: List<Int>
)