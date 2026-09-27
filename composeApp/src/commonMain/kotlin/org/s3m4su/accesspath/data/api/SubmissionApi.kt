package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.accessibility.PlaceSubmission

object SubmissionApi {
    @Volatile internal var saveOverride: (suspend (SubmissionRequestDto) -> Result<Unit>)? = null

    suspend fun save(
        request: SubmissionRequestDto,
        client: HttpClient = httpClient
    ): Result<Unit> {
        saveOverride?.let { return it(request) }
        return runCatching {
            client.put("$API_BASE_URL/api/v1/submissions") {
                setBody(request)
            }.body<ApiResponse<SubmissionDto>>()
            Unit
        }
    }
}