package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.accessibility.PlaceSubmission

/**
 * Valoracion viva del usuario (comentario + fotos) y "que cuenta la gente".
 * user_id sale del token. El PUT es get-or-create idempotente.
 */
object SubmissionApi {
    // get-or-create de la valoracion del usuario para el lugar; set comentario / anadir fotos.
    suspend fun save(request: SubmissionRequestDto): Result<Unit> = runCatching {
        httpClient.put("$API_BASE_URL/api/v1/submissions") {
            setBody(request)
        }.body<ApiResponse<SubmissionDto>>()
        Unit
    }

    // Comentarios + fotos de un lugar (solo submissions con algo que contar).
    suspend fun byPlace(placeId: Long): Result<List<PlaceSubmission>> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/places/$placeId/submissions")
            .body<ApiResponse<List<SubmissionDto>>>().data.orEmpty()
            .map { it.toDomain() }
    }
}
