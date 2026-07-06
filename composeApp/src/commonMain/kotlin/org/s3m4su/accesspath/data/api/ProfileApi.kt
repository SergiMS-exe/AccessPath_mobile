package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.accessibility.UserProfile

/**
 * Perfil funcional del usuario (opt-in, no diagnostico). Necesidades + consentimiento.
 * user_id sale del token.
 */
object ProfileApi {
    suspend fun get(): Result<UserProfile> = runCatching {
        httpClient.get("$API_BASE_URL/api/v1/me/profile")
            .body<ApiResponse<ProfileDto>>().data?.toDomain() ?: UserProfile()
    }

    // Fija necesidades + consentimiento (opt-in explicito).
    suspend fun set(request: ProfileRequestDto): Result<UserProfile> = runCatching {
        httpClient.put("$API_BASE_URL/api/v1/me/profile") {
            setBody(request)
        }.body<ApiResponse<ProfileDto>>().data?.toDomain() ?: UserProfile()
    }

    // Borra el perfil + retira el consentimiento.
    suspend fun delete(): Result<Unit> = runCatching {
        httpClient.delete("$API_BASE_URL/api/v1/me/profile")
        Unit
    }
}
