package com.example.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityListRequest(
    @SerialName("activities_ids") val activitiesIds: List<Int>
)