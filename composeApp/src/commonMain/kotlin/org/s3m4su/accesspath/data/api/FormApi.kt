package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.s3m4su.accesspath.data.accessibility.DimensionCatalog

object FormApi {
    suspend fun dimensions(
        client: HttpClient = httpClient
    ): Result<List<DimensionCatalog>> = runCatching {
        client.get("$API_BASE_URL/api/v1/dimensions")
            .body<ApiResponse<List<DimensionDto>>>().data.orEmpty()
            .map { it.toDomain() }
    }
}
