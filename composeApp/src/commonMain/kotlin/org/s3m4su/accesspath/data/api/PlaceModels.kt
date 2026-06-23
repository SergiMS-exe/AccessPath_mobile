package org.s3m4su.accesspath.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class AutocompleteItemDto(
    @SerialName("place_id")       val placeId: String,
    val description: String,
    @SerialName("main_text")      val mainText: String,
    @SerialName("secondary_text") val secondaryText: String
)

@Serializable
data class PlaceDto(
    val id: Long,
    val code: String,
    val name: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    @SerialName("google_place_id") val googlePlaceId: String? = null,
    @SerialName("created_by")      val createdBy: Long,
    @SerialName("created_at")      val createdAt: String,
    @SerialName("updated_at")      val updatedAt: String
)
