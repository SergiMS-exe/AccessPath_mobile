package org.s3m4su.accesspath.data.api

import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.test.Fixtures
import org.s3m4su.accesspath.test.testHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaceApiTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun `search hits places-search with q and session params`() = runTest {
        var capturedPath: String? = null
        var capturedQ: String? = null
        var capturedSession: String? = null
        val client = testHttpClient({ request ->
            capturedPath = request.url.encodedPath
            capturedQ = request.url.parameters["q"]
            capturedSession = request.url.parameters["session"]
            respond(
                """{"data":[{"place_id":"p1","description":"d","main_text":"M","secondary_text":"S"}],"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.search("cafe", "session-1", client)
        assertTrue(r.isSuccess)
        assertEquals("/api/v1/places/search", capturedPath)
        assertEquals("cafe", capturedQ)
        assertEquals("session-1", capturedSession)
        assertEquals(1, r.getOrNull()?.size)
    }

    @Test
    fun `search returns empty list when backend data is null`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":null,"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.search("cafe", "session", client)
        assertEquals(emptyList(), r.getOrNull())
    }

    @Test
    fun `search failure returns ApiException with backend message`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"cuota alcanzada"}""",
                HttpStatusCode.TooManyRequests,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.search("cafe", "session", client)
        val ex = r.exceptionOrNull() as ApiException
        assertEquals(429, ex.httpStatus)
        assertEquals("cuota alcanzada", ex.message)
    }

    @Test
    fun `mapPins hits places-map with bounding box params`() = runTest {
        var capturedMinLat: String? = null
        var capturedMaxLng: String? = null
        val client = testHttpClient({ request ->
            capturedMinLat = request.url.parameters["min_lat"]
            capturedMaxLng = request.url.parameters["max_lng"]
            respond(
                """{"data":[{"id":1,"name":"X","latitude":0,"longitude":0}],"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.mapPins(1.0, 2.0, 3.0, 4.0, client)
        assertTrue(r.isSuccess)
        assertEquals("1.0", capturedMinLat)
        assertEquals("4.0", capturedMaxLng)
    }

    @Test
    fun `mapPins maps DTO to domain Place with overallState`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":[{"id":7,"name":"Museo","latitude":40.0,"longitude":-3.0,"overall_state":"green"}],"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.mapPins(0.0, 1.0, 0.0, 1.0, client)
        val place = r.getOrNull()!!.first()
        assertEquals("7", place.id)
        assertEquals(org.s3m4su.accesspath.data.accessibility.AccessibilityState.GREEN, place.overallState)
    }

    @Test
    fun `detail returns PlaceDetail with dimensions and submissions`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":{"id":1,"name":"X","address":"a","latitude":0,"longitude":0,"accessibility":{"dimensions":[{"dimension_id":1,"key":"k","name":"n","state":"green","criteria":[]}],"overall_state":"green"},"submissions":[{"id":1,"comment":"c","username":"u"}]},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.detail(1L, client)
        val d = r.getOrNull()!!
        assertEquals(1L, d.id)
        assertEquals(1, d.accessibility.dimensions.size)
        assertEquals(1, d.submissions.size)
    }

    @Test
    fun `importFromGoogle posts body with snake_case fields`() = runTest {
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"id":99,"code":"X","name":"N","latitude":0,"longitude":0,"created_by":1,"created_at":"x","updated_at":"x"},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.importFromGoogle("place-id-1", "session-1", client)
        assertTrue(r.isSuccess)
        assertEquals(true, capturedBody!!.contains("\"google_place_id\":\"place-id-1\""))
        assertEquals(true, capturedBody!!.contains("\"session_token\":\"session-1\""))
    }

    @Test
    fun `importFromGoogle propagates backend error message`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":null,"error":"Sitio cerrado permanentemente"}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.importFromGoogle("place-id", "session", client)
        assertTrue(r.isFailure)
        assertEquals(true, r.exceptionOrNull()!!.message!!.contains("Sitio cerrado"))
    }
}