package org.s3m4su.accesspath.data.api

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.s3m4su.accesspath.data.auth.AuthUser
import org.s3m4su.accesspath.data.auth.LoginRequest
import org.s3m4su.accesspath.data.auth.LoginResponse
import org.s3m4su.accesspath.data.auth.RegisterRequest

object AuthApi {
    suspend fun login(email: String, password: String): Result<LoginResponse> = runCatching {
        httpClient.post("$API_BASE_URL/api/v1/auth/login") {
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun register(username: String, email: String, password: String): Result<AuthUser> = runCatching {
        httpClient.post("$API_BASE_URL/api/v1/auth/register") {
            setBody(RegisterRequest(username, email, password))
        }.body()
    }
}
