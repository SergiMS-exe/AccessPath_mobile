package org.s3m4su.accesspath.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthUser(
    val id: Long,
    val code: String,
    val username: String,
    val email: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class LoginResponse(
    val token: String,
    @SerialName("refresh_token") val refreshToken: String,
    val user: AuthUser
)

@Serializable
data class RefreshResponse(
    val token: String,
    @SerialName("refresh_token") val refreshToken: String
)
