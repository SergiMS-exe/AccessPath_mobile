package org.s3m4su.accesspath.data.api

import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.data.auth.AuthUser
import org.s3m4su.accesspath.data.auth.LoginResponse
import org.s3m4su.accesspath.test.testHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthApiTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val user = AuthUser(1, "A", "ana", "a@b.com", "2026-01-01T00:00:00Z")

    @Test
    fun `login success returns LoginResponse`() = runTest {
        val resp = LoginResponse(token = "t", refreshToken = "r", user = user)
        val client = testHttpClient({
            respond(
                json.encodeToString(resp),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = AuthApi.login("a@b.com", "1234", client)
        assertTrue(r.isSuccess)
        assertEquals("t", r.getOrNull()?.token)
        assertEquals("ana", r.getOrNull()?.user?.username)
    }

    @Test
    fun `login posts LoginRequest as JSON body`() = runTest {
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                json.encodeToString(LoginResponse("t", "r", user)),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        AuthApi.login("a@b.com", "1234", client)
        assertTrue(capturedBody!!.contains("\"email\":\"a@b.com\""))
        assertTrue(capturedBody!!.contains("\"password\":\"1234\""))
    }

    @Test
    fun `login failure returns failure with ApiException`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"credenciales invalidas"}""",
                HttpStatusCode.Unauthorized,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = AuthApi.login("a@b.com", "wrong", client)
        assertTrue(r.isFailure)
        val ex = r.exceptionOrNull() as ApiException
        assertEquals(401, ex.httpStatus)
        assertEquals("credenciales invalidas", ex.message)
    }

    @Test
    fun `login 500 marks as retryable`() = runTest {
        val client = testHttpClient({ respondError(HttpStatusCode.InternalServerError) })
        val r = AuthApi.login("a@b.com", "x", client)
        val ex = r.exceptionOrNull() as ApiException
        assertEquals(true, ex.isRetryable)
    }

    @Test
    fun `register success returns LoginResponse`() = runTest {
        val client = testHttpClient({
            respond(
                json.encodeToString(LoginResponse("t", "r", user)),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = AuthApi.register("ana", "a@b.com", "1234", client)
        assertTrue(r.isSuccess)
        assertEquals("t", r.getOrNull()?.token)
    }

    @Test
    fun `register posts RegisterRequest as JSON body`() = runTest {
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                json.encodeToString(LoginResponse("t", "r", user)),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        AuthApi.register("ana", "a@b.com", "1234", client)
        assertTrue(capturedBody!!.contains("\"username\":\"ana\""))
        assertTrue(capturedBody!!.contains("\"email\":\"a@b.com\""))
        assertTrue(capturedBody!!.contains("\"password\":\"1234\""))
    }

    @Test
    fun `register 409 returns ApiException with backend message`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"email duplicado"}""",
                HttpStatusCode.Conflict,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = AuthApi.register("ana", "a@b.com", "1234", client)
        assertEquals("email duplicado", (r.exceptionOrNull() as ApiException).message)
    }
}