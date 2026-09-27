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

class SubmissionApiTest {

    @Test
    fun `save sends PUT with place_id and comment`() = runTest {
        var method: String? = null
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            method = request.method.value
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"id":1,"comment":"hola","username":"u"},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = SubmissionApi.save(SubmissionRequestDto(placeId = 1L, comment = "hola"), client)
        assertTrue(r.isSuccess)
        assertEquals(true, method == "PUT")
        assertEquals(true, capturedBody!!.contains("\"place_id\":1"))
        assertEquals(true, capturedBody!!.contains("\"comment\":\"hola\""))
    }

    @Test
    fun `save failure returns ApiException`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"submission bloqueada"}""",
                HttpStatusCode.UnprocessableEntity,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = SubmissionApi.save(SubmissionRequestDto(1L, "x"), client)
        assertTrue(r.isFailure)
    }
}