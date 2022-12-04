package com.example.data.request

import kotlinx.serialization.Serializable

@Serializable
data class SetActivitiesRequest(
    val activities: List<Int>
)