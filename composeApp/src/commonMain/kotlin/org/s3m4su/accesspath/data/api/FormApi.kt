package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.s3m4su.accesspath.data.accessibility.DimensionCatalog

/** Catalogo del formulario: GET /dimensions. */
object FormApi {
    suspend fun dimensions(): Result<List<DimensionCatalog>> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/dimensions")
            .body<ApiResponse<List<DimensionDto>>>().data.orEmpty()
            .map { it.toDomain() }
    }
}
