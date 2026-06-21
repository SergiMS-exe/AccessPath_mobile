package org.s3m4su.accesspath.data.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.data.api.AuthApi

private const val KEY_TOKEN = "auth_token"
private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
private const val KEY_USER = "auth_user"

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val token: String, val user: AuthUser) : AuthState
}

object AuthRepository {
    private val settings = Settings()
    private val _state: MutableStateFlow<AuthState>
    val state: StateFlow<AuthState>

    init {
        val token = settings.getStringOrNull(KEY_TOKEN)
        val userJson = settings.getStringOrNull(KEY_USER)
        val initial: AuthState = if (token != null && userJson != null) {
            try {
                AuthState.Authenticated(token, Json.decodeFromString(userJson))
            } catch (_: Exception) {
                clearSettings()
                AuthState.Unauthenticated
            }
        } else {
            AuthState.Unauthenticated
        }
        _state = MutableStateFlow(initial)
        state = _state.asStateFlow()
    }

    val token: String?
        get() = (_state.value as? AuthState.Authenticated)?.token

    val refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    suspend fun login(email: String, password: String): Result<Unit> =
        AuthApi.login(email, password).map { response ->
            persist(response.token, response.refreshToken, response.user)
            _state.value = AuthState.Authenticated(response.token, response.user)
        }

    suspend fun register(username: String, email: String, password: String): Result<Unit> =
        AuthApi.register(username, email, password).map {
            AuthApi.login(email, password).getOrThrow().let { response ->
                persist(response.token, response.refreshToken, response.user)
                _state.value = AuthState.Authenticated(response.token, response.user)
            }
        }

    fun updateTokens(newToken: String, newRefreshToken: String) {
        val user = (_state.value as? AuthState.Authenticated)?.user ?: return
        settings.putString(KEY_TOKEN, newToken)
        settings.putString(KEY_REFRESH_TOKEN, newRefreshToken)
        _state.value = AuthState.Authenticated(newToken, user)
    }

    fun logout() {
        clearSettings()
        _state.value = AuthState.Unauthenticated
    }

    private fun persist(token: String, refreshToken: String, user: AuthUser) {
        settings.putString(KEY_TOKEN, token)
        settings.putString(KEY_REFRESH_TOKEN, refreshToken)
        settings.putString(KEY_USER, Json.encodeToString(user))
    }

    private fun clearSettings() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER)
    }
}
