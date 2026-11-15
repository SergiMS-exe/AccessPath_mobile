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
    fun `importFromCatalog posts source and external id`() = runTest {
        var capturedPath: String? = null
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"id":5,"code":"X","name":"N","latitude":0,"longitude":0,"created_by":1,"created_at":"x","updated_at":"x","source":"overture","moderation_status":"verified"},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.importFromCatalog("overture", "gers-1", null, client)
        assertTrue(r.isSuccess)
        assertEquals("/api/v1/places/from-catalog", capturedPath)
        assertEquals(true, capturedBody!!.contains("\"source\":\"overture\""))
        assertEquals(true, capturedBody!!.contains("\"external_id\":\"gers-1\""))
    }

    @Test
    fun `importFromCatalog accepts a local place id`() = runTest {
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"id":12,"code":"X","name":"Local","latitude":0,"longitude":0,"created_by":1,"created_at":"x","updated_at":"x"},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.importFromCatalog(placeId = 12, client = client)
        assertTrue(r.isSuccess)
        assertEquals(true, capturedBody!!.contains("\"place_id\":12"))
    }

    @Test
    fun `resolveLink parses matched catalog response`() = runTest {
        var capturedPath: String? = null
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"matched":true,"catalog":{"source":"overture","external_id":"gers-1","name":"Cafeteria Eclipse","address":"Calle Leopoldo Lugones, 24","latitude":43.4021,"longitude":-5.804946}},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.resolveLink("https://maps.app.goo.gl/abc", client)
        assertEquals("/api/v1/places/resolve-link", capturedPath)
        assertEquals(true, capturedBody!!.contains("\"url\":\"https://maps.app.goo.gl/abc\""))
        val resolved = r.getOrNull()!!
        assertTrue(resolved.matched)
        assertEquals("gers-1", resolved.catalog!!.externalId)
        assertEquals("Cafeteria Eclipse", resolved.catalog!!.name)
    }

    @Test
    fun `resolveLink returns prefill when there is no match`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":{"matched":false,"prefill":{"name":"Bar Nuevo","latitude":43.4,"longitude":-5.8,"google_ftid":"0xaa:0xbb"}},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.resolveLink("https://maps.app.goo.gl/abc", client)
        val resolved = r.getOrNull()!!
        assertTrue(!resolved.matched)
        assertEquals("Bar Nuevo", resolved.prefill!!.name)
        assertEquals("0xaa:0xbb", resolved.prefill!!.googleFtid)
    }

    @Test
    fun `createCommunity posts the site and returns pending place`() = runTest {
        var capturedPath: String? = null
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"id":7,"code":"X","name":"Bar Nuevo","address":"Calle X, 1","latitude":43.4,"longitude":-5.8,"created_by":1,"created_at":"x","updated_at":"x","source":"community","moderation_status":"pending"},"error":null}""",
                HttpStatusCode.Created,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = PlaceApi.createCommunity(
            name = "Bar Nuevo",
            address = "Calle X, 1",
            latitude = 43.4,
            longitude = -5.8,
            googleFtid = "0xaa:0xbb",
            client = client
        )
        assertTrue(r.isSuccess)
        assertEquals("/api/v1/places/community", capturedPath)
        assertEquals(true, capturedBody!!.contains("\"name\":\"Bar Nuevo\""))
        assertEquals(true, capturedBody!!.contains("\"google_ftid\":\"0xaa:0xbb\""))
    }
}
