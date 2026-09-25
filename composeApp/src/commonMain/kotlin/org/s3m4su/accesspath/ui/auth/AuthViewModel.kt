package org.s3m4su.accesspath.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.auth.AuthRepository

enum class AuthTab { LOGIN, REGISTER }

/**
 * Estado de la pantalla de autenticacion. Todos los campos del formulario viven
 * aqui para sobrevivir recomposiciones y poder ser testeados sin UI.
 */
data class AuthUiState(
    val tab: AuthTab = AuthTab.LOGIN,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val regUsername: String = "",
    val regEmail: String = "",
    val regPassword: String = "",
    val regPasswordConfirm: String = ""
)

/** Eventos one-shot que la UI debe consumir para navegar o mostrar mensajes. */
sealed interface AuthEvent {
    data object Authenticated : AuthEvent
}

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    // ---------- Tab ----------

    fun switchTab(tab: AuthTab) =
        _state.update { it.copy(tab = tab, errorMessage = null) }

    // ---------- Cambios en los campos ----------

    fun onLoginEmailChange(value: String) =
        _state.update { it.copy(loginEmail = value, errorMessage = null) }

    fun onLoginPasswordChange(value: String) =
        _state.update { it.copy(loginPassword = value, errorMessage = null) }

    fun onRegUsernameChange(value: String) =
        _state.update { it.copy(regUsername = value, errorMessage = null) }

    fun onRegEmailChange(value: String) =
        _state.update { it.copy(regEmail = value, errorMessage = null) }

    fun onRegPasswordChange(value: String) =
        _state.update { it.copy(regPassword = value, errorMessage = null) }

    fun onRegPasswordConfirmChange(value: String) =
        _state.update { it.copy(regPasswordConfirm = value, errorMessage = null) }

    // ---------- Submit ----------

    fun submit() {
        val current = _state.value
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (current.tab == AuthTab.LOGIN) {
                AuthRepository.login(current.loginEmail.trim(), current.loginPassword)
            } else {
                if (current.regPassword != current.regPasswordConfirm) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Las contrasenas no coinciden"
                        )
                    }
                    return@launch
                }
                AuthRepository.register(
                    current.regUsername.trim(),
                    current.regEmail.trim(),
                    current.regPassword
                )
            }
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, errorMessage = null) }
                    _events.tryEmit(AuthEvent.Authenticated)
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Error desconocido"
                        )
                    }
                }
            )
        }
    }
}