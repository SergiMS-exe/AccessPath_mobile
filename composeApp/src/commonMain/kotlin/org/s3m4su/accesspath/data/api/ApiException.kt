package org.s3m4su.accesspath.data.api

/**
 * Excepcion que representa un fallo controlado de la API: el backend devolvio
// un codigo 4xx/5xx con un envelope { "error": "..." } y propagamos ese mensaje
// tal cual a la UI.
 *
 * Antes de este tipo, los errores se convertian en una excepcion de Ktor
// generica (`ResponseException` con `message = "HTTP 500 Search failed"`) y
// el mensaje real del backend se perdia. Ahora la UI ve literalmente lo que
// el backend envio, y opcionalmente puede agrupar por httpStatus o mostrar el
 * requestId al usuario para soporte.
 */
class ApiException(
    val httpStatus: Int,
    val requestId: String?,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    /** true si el backend marco el error como transitorio (5xx, 429). */
    val isRetryable: Boolean
        get() = httpStatus >= 500 || httpStatus == 429
}