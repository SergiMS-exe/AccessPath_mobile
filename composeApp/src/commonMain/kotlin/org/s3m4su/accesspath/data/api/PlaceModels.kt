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
    @SerialName("secondary_text") val secondaryText: String,
    val source: String = "",
    @SerialName("external_id")    val externalId: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    @SerialName("distance_meters") val distanceMeters: Double? = null,
    // Si viene relleno, el sitio ya existe en la base local: se puede abrir
    // directamente sin importarlo.
    @SerialName("local_place_id")  val localPlaceId: Long? = null
)

// Ficha del catalogo externo (Overture) devuelta por /places/resolve-link.
@Serializable
data class CatalogItemDto(
    val source: String,
    @SerialName("external_id") val externalId: String,
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val latitude: Double,
    val longitude: Double,
    val category: String? = null
)

// Datos que el backend puede prerellenar cuando el sitio no esta en el catalogo.
@Serializable
data class PlacePrefillDto(
    val name: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    @SerialName("google_ftid") val googleFtid: String? = null,
    @SerialName("source_url")  val sourceUrl: String? = null
)

@Serializable
data class ResolvedLinkDto(
    val matched: Boolean = false,
    val catalog: CatalogItemDto? = null,
    val prefill: PlacePrefillDto? = null,
    val message: String? = null
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
    @SerialName("created_by")      val createdBy: Long,
    @SerialName("created_at")      val createdAt: String,
    @SerialName("updated_at")      val updatedAt: String
)

// Convierte el DTO plano (importacion de catalogo / CRUD) al modelo de dominio.
// Un lugar recien importado no tiene contribuciones: estado NO_DATA (gris).
fun PlaceDto.toDomain(): Place = Place(
    id = id.toString(),
    name = name,
    address = address ?: "",
    latitude = latitude,
    longitude = longitude,
    category = PlaceCategory.OTHER,
    description = description
)
