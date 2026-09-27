package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.accessibility.UserProfile

object ProfileApi {
    @Volatile internal var getOverride: (suspend () -> Result<UserProfile>)? = null
    @Volatile internal var setOverride: (suspend (ProfileRequestDto) -> Result<UserProfile>)? = null
    @Volatile internal var deleteOverride: (suspend () -> Result<Unit>)? = null

    suspend fun get(client: HttpClient = httpClient): Result<UserProfile> {
        getOverride?.let { return it() }
        return runCatching {
            client.get("$API_BASE_URL/api/v1/me/profile")
                .body<ApiResponse<ProfileDto>>().data?.toDomain() ?: UserProfile()
        }
    }

    suspend fun set(
        request: ProfileRequestDto,
        client: HttpClient = httpClient
    ): Result<UserProfile> {
        setOverride?.let { return it(request) }
        return runCatching {
            client.put("$API_BASE_URL/api/v1/me/profile") {
                setBody(request)
            }.body<ApiResponse<ProfileDto>>().data?.toDomain() ?: UserProfile()
        }
    }

    suspend fun delete(client: HttpClient = httpClient): Result<Unit> {
        deleteOverride?.let { return it() }
        return runCatching {
            client.delete("$API_BASE_URL/api/v1/me/profile")
            Unit
        }
    }
}