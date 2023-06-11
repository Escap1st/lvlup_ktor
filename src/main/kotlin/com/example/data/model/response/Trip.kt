package com.example.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripListResponse(
    val trips: List<TripResponse>,
)

@Serializable
data class TripResponse(
    val id: String,
    val title: String,
    val place: String,
    @SerialName("activity_id") val activityId: Int,
    val price: Double,
    @SerialName("date_from") val dateFrom: String,
    @SerialName("date_to") val dateTo: String,
    @SerialName("is_favorite_trip") val isFavoriteTrip: Boolean,
    @SerialName("main_photo_endpoint") val mainPhotoEndpoint: String,
    val rating: TripRatingResponse,
    @SerialName("description_short") val descriptionShort: String,
    @SerialName("preview_photos_endpoints") val previewPhotosEndpoints: List<String>?,
    @SerialName("participants_count") val participantsCount: Int?,
    @SerialName("recent_participants") val recentParticipants: List<UserResponse>?,
    @SerialName("is_favorite_place") val isFavoritePlace: Boolean?,
    @SerialName("description_full") val descriptionFull: String?,
    val provisions: List<TripProvisionResponse>?,
    val accommodation: String?,
    @SerialName("gallery_photos_endpoints") val galleryPhotosEndpoints: List<String>?,
    @SerialName("accommodation_photos_endpoints") val accommodationPhotosEndpoints: List<String>?,
    val schedules: List<TripScheduleResponse>?,
)

@Serializable
data class TripProvisionResponse(
    val title: String,
    @SerialName("is_included") val isIncluded: Boolean,
)

@Serializable
data class TripRatingResponse(
    val overall: Double,
    val count: Int,
    @SerialName("from_current_user") val fromCurrentUser: Int?,
)

@Serializable
data class TripScheduleResponse(
    val title: String,
    val entries: List<TripScheduleEntryResponse>,
)

@Serializable
data class TripScheduleEntryResponse(
    val title: String,
    @SerialName("time_from") val timeFrom: String?,
    @SerialName("time_to") val timeTo: String?,
)