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

    @Volatile internal var settingsOverride: Settings? = null
    @Volatile internal var loginOverride: (suspend (String, String) -> Result<LoginResponse>)? = null
    @Volatile internal var registerOverride: (suspend (String, String, String) -> Result<LoginResponse>)? = null

    private val settings: Settings get() = settingsOverride ?: Settings()

    // Lazy: el primer acceso ocurre DESPUES de que `reset(settings)` haya podido
    // inyectar el settingsOverride. Si fuera eager, el static init del companion
    // llamaria a Settings() en JVM (donde no hay Context) antes de que el
    // @BeforeTest del test pueda sustituirlo.
    private var _state: MutableStateFlow<AuthState>? = null
    private fun state0(): MutableStateFlow<AuthState> =
        _state ?: MutableStateFlow(loadInitial()).also { _state = it }
    val state: StateFlow<AuthState> get() = state0().asStateFlow()

    val token: String?
        get() = (state0().value as? AuthState.Authenticated)?.token

    val refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    suspend fun login(email: String, password: String): Result<Unit> {
        val ov = loginOverride
        val r = if (ov != null) ov(email, password) else AuthApi.login(email, password)
        return r.map { persist(it) }
    }

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        val ov = registerOverride
        val r = if (ov != null) ov(username, email, password) else AuthApi.register(username, email, password)
        return r.map { persist(it) }
    }

    fun updateTokens(newToken: String, newRefreshToken: String) {
        val user = (state0().value as? AuthState.Authenticated)?.user ?: return
        settings.putString(KEY_TOKEN, newToken)
        settings.putString(KEY_REFRESH_TOKEN, newRefreshToken)
        state0().value = AuthState.Authenticated(newToken, user)
    }

    fun logout() {
        // Conservamos KEY_REFRESH_TOKEN a proposito: aunque el usuario haga
        // logout, el refresh token sigue siendo valido para obtener un nuevo
        // access token en una sesion posterior sin pedir credenciales de nuevo.
        // El getter `refreshToken` lee siempre de settings (no de state), asi
        // que sigue accesible despues de logout.
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER)
        state0().value = AuthState.Unauthenticated
    }

    private fun persist(response: LoginResponse) {
        settings.putString(KEY_TOKEN, response.token)
        settings.putString(KEY_REFRESH_TOKEN, response.refreshToken)
        settings.putString(KEY_USER, Json.encodeToString(response.user))
        state0().value = AuthState.Authenticated(response.token, response.user)
    }

    private fun clearSettings() {
        // Reservado para "reset total" (no usado por logout). Logout es
        // selectivo y vive arriba.
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER)
    }

    private fun loadInitial(): AuthState {
        val s = settings
        val token = s.getStringOrNull(KEY_TOKEN)
        val userJson = s.getStringOrNull(KEY_USER)
        return if (token != null && userJson != null) {
            try {
                AuthState.Authenticated(token, Json.decodeFromString<AuthUser>(userJson))
            } catch (_: Exception) {
                s.remove(KEY_TOKEN); s.remove(KEY_REFRESH_TOKEN); s.remove(KEY_USER)
                AuthState.Unauthenticated
            }
        } else {
            AuthState.Unauthenticated
        }
    }

    internal fun reset(newSettings: Settings? = null) {
        // Si el test no pasa settings (caso comun: solo quiere limpiar overrides
        // y releer del settingsOverride existente), mantenemos el actual. Si
        // lo sobrescribimos con null, `loadInitial()` cae a `Settings()` que en
        // JVM (sin Context) revienta con NullPointerException.
        if (newSettings != null) settingsOverride = newSettings
        loginOverride = null
        registerOverride = null
        newSettings?.let { clearSettings() }
        _state = MutableStateFlow(loadInitial())
    }
}

internal fun setLoginResultForTest(result: Result<LoginResponse>) {
    AuthRepository.loginOverride = { _, _ -> result }
}

internal fun setRegisterResultForTest(result: Result<LoginResponse>) {
    AuthRepository.registerOverride = { _, _, _ -> result }
}