package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

    suspend fun importFromGoogle(googlePlaceId: String, sessionToken: String): Result<PlaceDto> = runCatching {
        httpClient.post("$API_BASE_URL/api/v1/places/from-google") {
            setBody(ImportFromGoogleBody(googlePlaceId, sessionToken))
        }.body<ApiResponse<PlaceDto>>().data!!
    }
}
