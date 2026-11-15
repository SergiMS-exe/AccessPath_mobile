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
private data class ImportFromCatalogBody(
    val source: String,
    @SerialName("external_id") val externalId: String,
    @SerialName("place_id")    val placeId: Long? = null
)

@Serializable
private data class ResolveLinkBody(val url: String)

@Serializable
private data class CreateCommunityBody(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: String? = null,
    val description: String? = null,
    @SerialName("source_url")  val sourceUrl: String? = null,
    @SerialName("google_ftid") val googleFtid: String? = null
)

object PlaceApi {

    @Volatile internal var searchOverride: (suspend (String, String) -> Result<List<AutocompleteItemDto>>)? = null
    @Volatile internal var mapPinsOverride: (suspend (Double, Double, Double, Double) -> Result<List<Place>>)? = null
    @Volatile internal var detailOverride: (suspend (Long) -> Result<PlaceDetail>)? = null
    @Volatile internal var importFromCatalogOverride: (suspend (String, String, Long?) -> Result<PlaceDto>)? = null
    @Volatile internal var resolveLinkOverride: (suspend (String) -> Result<ResolvedLinkDto>)? = null
    @Volatile internal var createCommunityOverride:
        (suspend (String, String, Double, Double) -> Result<PlaceDto>)? = null

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

    /** Importa una ficha del catalogo (Overture) sin llamar a Google. */
    suspend fun importFromCatalog(
        source: String = "",
        externalId: String = "",
        placeId: Long? = null,
        client: HttpClient = httpClient
    ): Result<PlaceDto> {
        importFromCatalogOverride?.let { return it(source, externalId, placeId) }
        return runCatching {
            val response = client.post("$API_BASE_URL/api/v1/places/from-catalog") {
                setBody(ImportFromCatalogBody(source, externalId, placeId))
            }
            val body = response.body<ApiResponse<PlaceDto>>()
            body.data ?: throw IllegalStateException(body.error ?: "No se pudo anyadir el lugar")
        }
    }

    /**
     * Lee un enlace de Google Maps y busca el sitio en el catalogo.
     * No scrapea la ficha de Google: solo extrae nombre y coordenadas del enlace.
     */
    suspend fun resolveLink(
        url: String,
        client: HttpClient = httpClient
    ): Result<ResolvedLinkDto> {
        resolveLinkOverride?.let { return it(url) }
        return runCatching {
            val response = client.post("$API_BASE_URL/api/v1/places/resolve-link") {
                setBody(ResolveLinkBody(url))
            }
            val body = response.body<ApiResponse<ResolvedLinkDto>>()
            body.data ?: throw IllegalStateException(body.error ?: "No se pudo leer el enlace")
        }
    }

    /**
     * Crea un sitio que no esta en el catalogo. Nace pendiente de moderacion:
     * no aparece en el mapa publico hasta que se verifica.
     */
    suspend fun createCommunity(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        category: String? = null,
        description: String? = null,
        sourceUrl: String? = null,
        googleFtid: String? = null,
        client: HttpClient = httpClient
    ): Result<PlaceDto> {
        createCommunityOverride?.let { return it(name, address, latitude, longitude) }
        return runCatching {
            val response = client.post("$API_BASE_URL/api/v1/places/community") {
                setBody(
                    CreateCommunityBody(
                        name = name,
                        address = address,
                        latitude = latitude,
                        longitude = longitude,
                        category = category,
                        description = description,
                        sourceUrl = sourceUrl,
                        googleFtid = googleFtid
                    )
                )
            }
            val body = response.body<ApiResponse<PlaceDto>>()
            body.data ?: throw IllegalStateException(body.error ?: "No se pudo crear el lugar")
        }
    }
}