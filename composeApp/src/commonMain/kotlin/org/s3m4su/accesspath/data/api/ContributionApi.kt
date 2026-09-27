package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.accessibility.ContributionResult
import org.s3m4su.accesspath.data.accessibility.NextQuestion

object ContributionApi {
    @Volatile internal var nextQuestionOverride: (suspend (Long) -> Result<NextQuestion>)? = null
    @Volatile internal var saveOverride: (suspend (ContributionRequestDto) -> Result<ContributionResult>)? = null
    @Volatile internal var deleteOverride: (suspend (Long) -> Result<ContributionResult>)? = null

    suspend fun nextQuestion(
        placeId: Long,
        client: HttpClient = httpClient
    ): Result<NextQuestion> {
        nextQuestionOverride?.let { return it(placeId) }
        return runCatching {
            client.get("$API_BASE_URL/api/v1/places/$placeId/next-question")
                .body<ApiResponse<NextQuestionDto>>().data?.toDomain()
                ?: NextQuestion(criterion = null, options = emptyList())
        }
    }

    suspend fun save(
        request: ContributionRequestDto,
        client: HttpClient = httpClient
    ): Result<ContributionResult> {
        saveOverride?.let { return it(request) }
        return runCatching {
            client.post("$API_BASE_URL/api/v1/contributions") {
                setBody(request)
            }.body<ApiResponse<ContributionResultDto>>().data!!.toDomain()
        }
    }

    suspend fun delete(
        contributionId: Long,
        client: HttpClient = httpClient
    ): Result<ContributionResult> {
        deleteOverride?.let { return it(contributionId) }
        return runCatching {
            client.delete("$API_BASE_URL/api/v1/contributions/$contributionId")
                .body<ApiResponse<ContributionResultDto>>().data!!.toDomain()
        }
    }
}