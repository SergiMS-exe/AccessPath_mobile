package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
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

    @Volatile internal var searchOverride: (suspend (String, String) -> Result<List<AutocompleteItemDto>>)? = null
    @Volatile internal var mapPinsOverride: (suspend (Double, Double, Double, Double) -> Result<List<Place>>)? = null
    @Volatile internal var detailOverride: (suspend (Long) -> Result<PlaceDetail>)? = null
    @Volatile internal var importFromGoogleOverride: (suspend (String, String) -> Result<PlaceDto>)? = null

    suspend fun search(
        query: String,
        sessionToken: String,
        client: HttpClient = httpClient
    ): Result<List<AutocompleteItemDto>> {
        searchOverride?.let { return it(query, sessionToken) }
        return runCatching {
            val response: HttpResponse = client.get("$API_BASE_URL/api/v1/places/search") {
                parameter("q", query)
                parameter("session", sessionToken)
            }
            response.body<ApiResponse<List<AutocompleteItemDto>>>().data ?: emptyList()
        }
    }

    suspend fun mapPins(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        client: HttpClient = httpClient
    ): Result<List<Place>> {
        mapPinsOverride?.let { return it(minLat, maxLat, minLng, maxLng) }
        return runCatching {
            client.get("$API_BASE_URL/api/v1/places/map") {
                parameter("min_lat", minLat)
                parameter("max_lat", maxLat)
                parameter("min_lng", minLng)
                parameter("max_lng", maxLng)
            }.body<ApiResponse<List<PlaceMapItemDto>>>().data.orEmpty()
                .map { it.toDomain() }
        }
    }

    suspend fun detail(placeId: Long, client: HttpClient = httpClient): Result<PlaceDetail> {
        detailOverride?.let { return it(placeId) }
        return runCatching {
            client.get("$API_BASE_URL/api/v1/places/$placeId")
                .body<ApiResponse<PlaceDetailDto>>().data!!.toDomain()
        }
    }

    suspend fun importFromGoogle(
        googlePlaceId: String,
        sessionToken: String,
        client: HttpClient = httpClient
    ): Result<PlaceDto> {
        importFromGoogleOverride?.let { return it(googlePlaceId, sessionToken) }
        return runCatching {
            val response = client.post("$API_BASE_URL/api/v1/places/from-google") {
                setBody(ImportFromGoogleBody(googlePlaceId, sessionToken))
            }
            val body = response.body<ApiResponse<PlaceDto>>()
            body.data ?: throw IllegalStateException(body.error ?: "No se pudo anyadir el lugar")
        }
    }
}