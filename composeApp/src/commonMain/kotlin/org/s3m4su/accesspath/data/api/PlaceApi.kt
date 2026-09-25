package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
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
        val url = "$API_BASE_URL/api/v1/places/search?q=$query&session=$sessionToken"
        println("[PlaceApi.search] GET $url")
        val response: HttpResponse = httpClient.get("$API_BASE_URL/api/v1/places/search") {
            parameter("q", query)
            parameter("session", sessionToken)
        }
        val requestId = response.headers[REQUEST_ID_HEADER]
        val body = response.body<ApiResponse<List<AutocompleteItemDto>>>()
        val items = body.data ?: emptyList()
        println(
            "[PlaceApi.search] <- ${response.status.value} ${response.request.method.value} " +
            "$url | request_id=$requestId error=${body.error} items=${items.size} " +
            "preview=${items.take(3).map { it.placeId }}"
        )
        items
    }.onFailure { e ->
        logApiFailure("search", "q=\"$query\"", e)
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
        val url = "$API_BASE_URL/api/v1/places/from-google"
        println("[PlaceApi.importFromGoogle] POST $url body={google_place_id=$googlePlaceId, session_token=$sessionToken}")
        val response = httpClient.post(url) {
            setBody(ImportFromGoogleBody(googlePlaceId, sessionToken))
        }
        val body = response.body<ApiResponse<PlaceDto>>()
        val requestId = response.headers[REQUEST_ID_HEADER]
        println(
            "[PlaceApi.importFromGoogle] <- ${response.status.value} " +
            "request_id=$requestId error=${body.error} placeId=${body.data?.id} name=${body.data?.name}"
        )
        // Propagar el mensaje del backend (p.ej. "cerrado permanentemente", cuota).
        body.data ?: throw IllegalStateException(body.error ?: "No se pudo añadir el lugar")
    }.onFailure { e ->
        logApiFailure("importFromGoogle", "googlePlaceId=\"$googlePlaceId\"", e)
    }
}

private fun logApiFailure(op: String, ctx: String, e: Throwable) {
    val (status, requestId) = when (e) {
        is ApiException -> e.httpStatus to (e.requestId ?: "?")
        else -> "?" to "?"
    }
    println("[PlaceApi.$op] !! fallo httpStatus=$status request_id=$requestId $ctx: ${e::class.simpleName} ${e.message}")
}