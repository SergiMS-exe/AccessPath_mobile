package org.s3m4su.accesspath.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory

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

// Convierte el DTO de la API al modelo de dominio que pinta el mapa/UI.
// Categoria y accesibilidad aun no llegan en este endpoint: defaults neutros.
fun PlaceDto.toDomain(): Place = Place(
    id = id.toString(),
    name = name,
    address = address ?: "",
    latitude = latitude,
    longitude = longitude,
    rating = 0f,
    category = PlaceCategory.OTHER,
    description = description
)
