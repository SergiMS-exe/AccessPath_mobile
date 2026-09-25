package org.s3m4su.accesspath.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.BuildKonfig
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.data.auth.RefreshResponse

val API_BASE_URL: String = BuildKonfig.API_BASE_URL

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
    install(Logging) {
        // INFO registra solo las lineas de request/response (metodo, url, status).
        // NUNCA BODY ni HEADERS: el header Authorization y los cuerpos filtrarian
        // tokens al log. Para depurar puntualmente, subir el nivel en local.
        level = LogLevel.INFO
    }

    // Validador global: convierte cualquier respuesta no-2xx en ApiException,
    // extrayendo el mensaje { "error": "..." } del envelope del backend.
    // Asi las APIs no tienen que repetir la misma boilerplate de Try/catch.
    HttpResponseValidator {
        validateResponse { response: HttpResponse ->
            if (!response.status.isSuccess()) {
                val status = response.status.value
                val requestId = response.headers[REQUEST_ID_HEADER]

                // Intentamos leer el body como envelope para extraer el mensaje
                // user-friendly. Si falla la deserializacion caemos al mensaje
                // por defecto de Ktor.
                val userMessage = readErrorMessage(response)
                    ?: "HTTP $status"

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
        // Propagamos X-Request-Id para que el backend agrupe todos los reintentos
        // de un mismo intento logico del cliente en una sola linea con ese id.
        header(REQUEST_ID_HEADER, newRequestId())
    }
}

// Constantes expuestas para que los APIs (p.ej. PlaceApi) puedan leer la cabecera
// de respuesta y loguearla junto al error.
const val REQUEST_ID_HEADER = "X-Request-Id"

/** Forma minima del envelope para extraer el campo `error` en respuestas 4xx/5xx. */
@Serializable
private data class ErrorEnvelope(val error: String? = null)

/**
 * Lee el body de una respuesta de error y extrae el `error` del envelope.
 * El backend siempre responde con `{ "data": ..., "error": "..." }` en errores,
 * asi que deserializar con este envelope minimo (sin tocar el resto) cubre 4xx
 * y 5xx por igual.
 */
private suspend fun readErrorMessage(response: HttpResponse): String? {
    val raw = try {
        response.body<ErrorEnvelope>().error
    } catch (_: Throwable) {
        return null
    }
    return raw?.takeIf { it.isNotBlank() }
}

/** Genera un id unico para esta peticion. Suficiente para correlacionar logs. */
private fun newRequestId(): String {
    // 8 bytes hex = 16 chars. Suficiente para logs sin levantar temas de crypto.
    // Usamos toString(16) + padding manual en lugar de String.format("%02x") para
    // mantenernos en commonMain (String.format con formato varia entre targets).
    val bytes = ByteArray(8)
    kotlin.random.Random.nextBytes(bytes)
    return bytes.joinToString("") { byte ->
        // Byte es signed (-128..127); and 0xff lo deja en 0..255 antes de
        // pasarlo a Int (que toString(16) espera).
        val unsigned = byte.toInt() and 0xff
        val hex = unsigned.toString(16)
        if (hex.length < 2) "0$hex" else hex
    }
}
