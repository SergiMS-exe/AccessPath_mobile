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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContributionApiTest {

    @Test
    fun `nextQuestion returns NextQuestion with criterion and options`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":{"criterion":{"id":1,"dimension_id":1,"key":"rampa","prompt":"?Tiene rampa?","is_blocking":true,"is_starter":true,"options":[]},"options":[{"id":10,"criterion_id":1,"label":"Si"},{"id":11,"criterion_id":1,"label":"No"}]},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ContributionApi.nextQuestion(1L, client)
        assertTrue(r.isSuccess)
        val q = r.getOrNull()!!
        assertEquals(1L, q.criterion?.id)
        assertEquals(2, q.options.size)
    }

    @Test
    fun `nextQuestion with null criterion returns empty NextQuestion`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":{"criterion":null,"options":[]},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ContributionApi.nextQuestion(1L, client)
        assertTrue(r.isSuccess)
        assertNull(r.getOrNull()!!.criterion)
    }

    @Test
    fun `nextQuestion with null data returns empty NextQuestion`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":null,"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ContributionApi.nextQuestion(1L, client)
        assertNull(r.getOrNull()!!.criterion)
    }

    @Test
    fun `save posts snake_case body`() = runTest {
        var capturedBody: String? = null
        val client = testHttpClient({ request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                """{"data":{"contribution_id":42,"criterion":{"criterion_id":1,"key":"k","prompt":"p","state":"green"},"dimension":{"dimension_id":1,"key":"d","name":"n","state":"green"}},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ContributionApi.save(ContributionRequestDto(1L, 2L, 3L), client)
        assertTrue(r.isSuccess)
        assertEquals(42L, r.getOrNull()!!.contributionId)
        assertEquals(true, capturedBody!!.contains("\"place_id\":1"))
        assertEquals(true, capturedBody!!.contains("\"criterion_id\":2"))
        assertEquals(true, capturedBody!!.contains("\"answer_option_id\":3"))
    }

    @Test
    fun `save failure returns ApiException`() = runTest {
        val client = testHttpClient({
            respond(
                """{"error":"rate limit"}""",
                HttpStatusCode.TooManyRequests,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ContributionApi.save(ContributionRequestDto(1L, 2L, 3L), client)
        assertTrue(r.isFailure)
        assertEquals(429, (r.exceptionOrNull() as ApiException).httpStatus)
    }

    @Test
    fun `delete sends DELETE and returns contribution with null id`() = runTest {
        var method: String? = null
        val client = testHttpClient({ request ->
            method = request.method.value
            respond(
                """{"data":{"contribution_id":null,"criterion":{"criterion_id":1,"key":"k","prompt":"p","state":"green"},"dimension":{"dimension_id":1,"key":"d","name":"n","state":"green"}},"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = ContributionApi.delete(42L, client)
        assertTrue(r.isSuccess)
        assertNull(r.getOrNull()!!.contributionId)
        assertEquals("DELETE", method)
    }
}