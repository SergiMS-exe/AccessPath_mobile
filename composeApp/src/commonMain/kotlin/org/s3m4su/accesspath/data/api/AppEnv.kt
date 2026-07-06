package org.s3m4su.accesspath.data.api

/**
 * Configuracion por entorno del cliente. Sustituye la URL base antes hardcodeada
 * en ApiClient: cada plataforma provee su valor (actual), de modo que dev/prod
 * puedan divergir sin tocar codigo comun.
 */
expect val apiBaseUrl: String
