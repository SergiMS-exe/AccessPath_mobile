package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.data.auth.RefreshResponse

// IP del servidor de desarrollo. Cambiar si se mueve el backend.
const val API_BASE_URL = "http://192.168.1.130:8080"

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
    }
    install(Auth) {
        bearer {
            loadTokens {
                val token = AuthRepository.token ?: return@loadTokens null
                val refreshToken = AuthRepository.refreshToken ?: return@loadTokens null
                BearerTokens(token, refreshToken)
            }
            refreshTokens {
                val refreshToken = oldTokens?.refreshToken ?: run {
                    AuthRepository.logout()
                    return@refreshTokens null
                }
                val response = client.post("$API_BASE_URL/api/v1/auth/refresh") {
                    markAsRefreshTokenRequest()
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("refresh_token" to refreshToken))
                }
                if (response.status == HttpStatusCode.OK) {
                    val refreshResponse = response.body<RefreshResponse>()
                    AuthRepository.updateTokens(refreshResponse.token, refreshResponse.refreshToken)
                    BearerTokens(refreshResponse.token, refreshResponse.refreshToken)
                } else {
                    AuthRepository.logout()
                    null
                }
            }
            sendWithoutRequest { true }
        }
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
    }
}
