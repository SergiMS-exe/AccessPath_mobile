package org.s3m4su.accesspath.test

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.data.api.ApiException

fun testHttpClient(
    handler: MockRequestHandler,
    initialTokens: BearerTokens? = null,
    onTokenRefresh: (suspend (BearerTokens?) -> BearerTokens?)? = null
): HttpClient = HttpClient(MockEngine(handler)) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }

    HttpResponseValidator {
        validateResponse { response: HttpResponse ->
            if (!response.status.isSuccess()) {
                val status = response.status.value
                val requestId = response.headers["X-Request-Id"]
                val userMessage = readErrorMessage(response) ?: "HTTP $status"
                throw ApiException(
                    httpStatus = status,
                    requestId = requestId,
                    message = userMessage
                )
            }
        }
    }

    install(Auth) {
        bearer {
            loadTokens { initialTokens }
            refreshTokens {
                if (onTokenRefresh != null) onTokenRefresh(oldTokens) else null
            }
            sendWithoutRequest { true }
        }
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
        header("X-Request-Id", "test-${kotlin.random.Random.nextInt()}")
    }
}

@Serializable
private data class ErrorEnvelope(val error: String? = null)

private suspend fun readErrorMessage(response: HttpResponse): String? {
    val raw = try {
        Json { ignoreUnknownKeys = true }
            .decodeFromString(ErrorEnvelope.serializer(), response.bodyAsText())
            .error
    } catch (_: Throwable) {
        return null
    }
    return raw?.takeIf { it.isNotBlank() }
}