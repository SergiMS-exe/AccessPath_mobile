package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.test.testHttpClient
import org.s3m4su.accesspath.data.auth.AuthUser
import org.s3m4su.accesspath.data.auth.LoginRequest
import org.s3m4su.accesspath.data.auth.LoginResponse
import org.s3m4su.accesspath.data.auth.RefreshResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApiClientTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun `GET request sends X-Request-Id header on every call`() = runTest {
        var capturedRequestId: String? = null
        val client = testHttpClient({ request ->
            capturedRequestId = request.headers["X-Request-Id"]
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        client.get("/api/v1/test")
        assertTrue(capturedRequestId != null && capturedRequestId!!.isNotBlank())
    }

    @Test
    fun `Bearer token is attached to requests when loaded`() = runTest {
        var capturedAuth: String? = null
        val client = testHttpClient(
            handler = { request ->
                capturedAuth = request.headers["Authorization"]
                respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
            initialTokens = BearerTokens("access-1", "refresh-1")
        )
        client.get("/api/v1/test")
        assertEquals("Bearer access-1", capturedAuth)
    }

    @Test
    fun `401 triggers refresh flow and retries the request`() = runTest {
        var attempt = 0
        var refreshCalled = false
        val client = testHttpClient(
            handler = { request ->
                when {
                    request.url.encodedPath.endsWith("/refresh") -> {
                        refreshCalled = true
                        respond(
                            json.encodeToString(RefreshResponse("new-token", "new-refresh")),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> {
                        attempt++
                        if (attempt == 1) respond("{}", HttpStatusCode.Unauthorized)
                        else {
                            capturedAuthAfterRefresh = request.headers["Authorization"]
                            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                        }
                    }
                }
            },
            initialTokens = BearerTokens("old-token", "old-refresh"),
            onTokenRefresh = {
                refreshCalled = true
                BearerTokens("new-token", "new-refresh")
            }
        )
        client.get("/api/v1/protected")
        assertTrue(refreshCalled)
        assertEquals(2, attempt)
    }

    private var capturedAuthAfterRefresh: String? = null

    @Test
    fun `non-2xx response with envelope error extracts message and throws ApiException`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"email duplicado"}""",
                HttpStatusCode.Conflict,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val ex = assertFailsWith<ApiException> {
            client.get("/api/v1/test")
        }
        assertEquals(409, ex.httpStatus)
        assertEquals("email duplicado", ex.message)
        assertEquals(true, ex.isRetryable == false)
    }

    @Test
    fun `5xx response is marked retryable`() = runTest {
        val client = testHttpClient({
            respond("{}", HttpStatusCode.InternalServerError)
        })
        val ex = assertFailsWith<ApiException> {
            client.get("/api/v1/test")
        }
        assertEquals(500, ex.httpStatus)
        assertEquals(true, ex.isRetryable)
    }

    @Test
    fun `429 Too Many Requests is marked retryable`() = runTest {
        val client = testHttpClient({
            respond("{}", HttpStatusCode.TooManyRequests)
        })
        val ex = assertFailsWith<ApiException> { client.get("/api/v1/test") }
        assertEquals(429, ex.httpStatus)
        assertEquals(true, ex.isRetryable)
    }

    @Test
    fun `envelope without error falls back to default message`() = runTest {
        val client = testHttpClient({
            respond("{}", HttpStatusCode.BadRequest)
        })
        val ex = assertFailsWith<ApiException> { client.get("/api/v1/test") }
        assertEquals(400, ex.httpStatus)
        assertEquals("HTTP 400", ex.message)
    }

    @Test
    fun `unparseable error body still produces ApiException with default message`() = runTest {
        val client = testHttpClient({
            respond("esto no es JSON", HttpStatusCode.BadRequest)
        })
        val ex = assertFailsWith<ApiException> { client.get("/api/v1/test") }
        assertEquals(400, ex.httpStatus)
        assertEquals("HTTP 400", ex.message)
    }

    @Test
    fun `ApiException carries request id from response header`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"x"}""",
                HttpStatusCode.InternalServerError,
                headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    "X-Request-Id" to listOf("abc-123")
                )
            )
        })
        val ex = assertFailsWith<ApiException> { client.get("/api/v1/test") }
        assertEquals("abc-123", ex.requestId)
    }
}