package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.accessibility.PlaceDetail

@Serializable
private data class ImportFromGoogleBody(
    @SerialName("google_place_id") val googlePlaceId: String,
    @SerialName("session_token")   val sessionToken: String
)

object PlaceApi {
    suspend fun search(query: String, sessionToken: String): Result<List<AutocompleteItemDto>> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/places/search") {
            parameter("q", query)
            parameter("session", sessionToken)
        }.body<ApiResponse<List<AutocompleteItemDto>>>().data ?: emptyList()
    }

    // Lugares publicados (con al menos una contribucion) en la region visible del
    // mapa, con su resumen de accesibilidad por dimension + estado global.
    suspend fun mapPins(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
    ): Result<List<Place>> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/places/map") {
            parameter("min_lat", minLat)
            parameter("max_lat", maxLat)
            parameter("min_lng", minLng)
            parameter("max_lng", maxLng)
        }.body<ApiResponse<List<PlaceMapItemDto>>>().data.orEmpty()
            .map { it.toDomain() }
    }

    // Detalle completo de un lugar: desglose por dimension/criterio + comentarios + fotos.
    suspend fun detail(placeId: Long): Result<PlaceDetail> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/places/$placeId")
            .body<ApiResponse<PlaceDetailDto>>().data!!.toDomain()
    }

    suspend fun importFromGoogle(googlePlaceId: String, sessionToken: String): Result<PlaceDto> = runCatching {
        val response = httpClient.post("$API_BASE_URL/api/v1/places/from-google") {
            setBody(ImportFromGoogleBody(googlePlaceId, sessionToken))
        }.body<ApiResponse<PlaceDto>>()
        // Propagar el mensaje del backend (p.ej. "cerrado permanentemente", cuota).
        response.data ?: throw IllegalStateException(response.error ?: "No se pudo añadir el lugar")
    }
}
