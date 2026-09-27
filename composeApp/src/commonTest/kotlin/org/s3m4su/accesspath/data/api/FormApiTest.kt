package org.s3m4su.accesspath.data.api

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.s3m4su.accesspath.test.testHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormApiTest {

    @Test
    fun `dimensions returns List of DimensionCatalog`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":[{"id":1,"key":"acceso","name":"Acceso","description":"Como se llega","sort_order":1,"criteria":[{"id":11,"dimension_id":1,"key":"rampa","prompt":"?Tiene rampa?","is_blocking":true,"is_starter":true,"options":[]}]}],"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = FormApi.dimensions(client)
        assertTrue(r.isSuccess)
        val list = r.getOrNull()!!
        assertEquals(1, list.size)
        assertEquals(1, list[0].criteria.size)
    }

    @Test
    fun `dimensions with null data returns empty list`() = runTest {
        val client = testHttpClient({
            respond(
                """{"data":null,"error":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val r = FormApi.dimensions(client)
        assertEquals(emptyList(), r.getOrNull())
    }
}