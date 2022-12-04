package com.example.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityListResponse(
    val activities: List<ActivityResponse>
)

@Serializable()
data class ActivityResponse(
    val id: Int,
    val name: String,
    @SerialName("display_name") val displayName: String,
    val image: String
)