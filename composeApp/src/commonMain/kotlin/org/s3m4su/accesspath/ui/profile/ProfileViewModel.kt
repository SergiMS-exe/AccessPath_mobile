package org.s3m4su.accesspath.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.api.ProfileApi
import org.s3m4su.accesspath.data.api.ProfileRequestDto

/**
 * Estado del perfil funcional (opt-in). Las necesidades seleccionadas se editan
 * en local y solo se envian al guardar con consentimiento explicito.
 */
data class ProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val selectedNeeds: Set<String> = emptySet(),
    val hasConsent: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            ProfileApi.get()
                .onSuccess { profile ->
                    _state.value = ProfileUiState(
                        loading = false,
                        selectedNeeds = profile.needs.toSet(),
                        hasConsent = profile.hasConsent
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "No se pudo cargar el perfil."
                    )
                }
        }
    }

    fun toggleNeed(key: String) {
        val next = _state.value.selectedNeeds.toMutableSet().apply {
            if (key in this) remove(key) else add(key)
        }
        _state.value = _state.value.copy(selectedNeeds = next, saved = false)
    }

    /** Guarda necesidades + consentimiento explicito (opt-in). */
    fun save() {
        val needs = _state.value.selectedNeeds.toList()
        _state.value = _state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            ProfileApi.set(ProfileRequestDto(needs = needs, consent = true))
                .onSuccess { profile ->
                    _state.value = _state.value.copy(
                        saving = false,
                        saved = true,
                        hasConsent = profile.hasConsent,
                        selectedNeeds = profile.needs.toSet()
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(saving = false, error = "No se pudo guardar.")
                }
        }
    }

    /** Borra el perfil entero y retira el consentimiento. */
    fun delete() {
        _state.value = _state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            ProfileApi.delete()
                .onSuccess {
                    _state.value = ProfileUiState(
                        loading = false,
                        selectedNeeds = emptySet(),
                        hasConsent = false
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(saving = false, error = "No se pudo borrar.")
                }
        }
    }
}
