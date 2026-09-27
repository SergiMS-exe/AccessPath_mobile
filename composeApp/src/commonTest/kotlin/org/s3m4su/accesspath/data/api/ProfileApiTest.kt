package org.s3m4su.accesspath.data.api

import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.s3m4su.accesspath.test.testHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileApiTest {

    @Test
    fun `get returns UserProfile with needs and consent`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":{"needs":["silla","ceguera"],"consent_at":"2026-09-01","has_consent":true},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ProfileApi.get(client)
        assertTrue(r.isSuccess)
        val p = r.getOrNull()!!
        assertEquals(listOf("silla", "ceguera"), p.needs)
        assertEquals(true, p.hasConsent)
        assertEquals("2026-09-01", p.consentAt)
    }

    @Test
    fun `get with null data falls back to empty UserProfile`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":null,"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ProfileApi.get(client)
        assertEquals(emptyList(), r.getOrNull()!!.needs)
        assertEquals(false, r.getOrNull()!!.hasConsent)
    }

    @Test
    fun `set sends PUT with needs and consent in snake_case body`() = runTest {
        var method: String? = null
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            method = request.method.value
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"needs":["silla"],"has_consent":true},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ProfileApi.set(ProfileRequestDto(needs = listOf("silla"), consent = true), client)
        assertTrue(r.isSuccess)
        assertEquals("PUT", method)
        assertEquals(true, capturedBody!!.contains("\"needs\":[\"silla\"]"))
        assertEquals(true, capturedBody!!.contains("\"consent\":true"))
    }

    @Test
    fun `delete sends DELETE and returns success`() = runTest {
        var method: String? = null
        val client = testHttpClient({ request ->
            method = request.method.value
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val r = ProfileApi.delete(client)
        assertTrue(r.isSuccess)
        assertEquals("DELETE", method)
    }

    @Test
    fun `set failure returns ApiException with backend message`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"consent required"}""",
                HttpStatusCode.BadRequest,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ProfileApi.set(ProfileRequestDto(emptyList(), consent = false), client)
        assertEquals("consent required", (r.exceptionOrNull() as ApiException).message)
    }
}