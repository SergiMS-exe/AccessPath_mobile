package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.accessibility.ContributionResult
import org.s3m4su.accesspath.data.accessibility.NextQuestion

/**
 * Contribucion atomica: siguiente pregunta, crear/editar (upsert) y Deshacer.
 * user_id sale del token (Auth plugin del httpClient); nunca del body.
 */
object ContributionApi {
    // Pregunta elegida por el backend para (place, user). criterion == null => nada util.
    suspend fun nextQuestion(placeId: Long): Result<NextQuestion> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/places/$placeId/next-question")
            .body<ApiResponse<NextQuestionDto>>().data?.toDomain()
            ?: NextQuestion(criterion = null, options = emptyList())
    }

    // Crea o edita in-place la respuesta; devuelve el semaforo en vivo del criterio + dimension.
    suspend fun save(request: ContributionRequestDto): Result<ContributionResult> = runCatching {
        httpClient.post("$API_BASE_URL/api/v1/contributions") {
            setBody(request)
        }.body<ApiResponse<ContributionResultDto>>().data!!.toDomain()
    }

    // Deshacer: soft delete + recalculo; devuelve el semaforo en vivo tras el borrado.
    suspend fun delete(contributionId: Long): Result<ContributionResult> = runCatching {
        httpClient.delete("$API_BASE_URL/api/v1/contributions/$contributionId")
            .body<ApiResponse<ContributionResultDto>>().data!!.toDomain()
    }
}
