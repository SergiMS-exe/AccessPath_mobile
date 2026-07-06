package org.s3m4su.accesspath.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.accessibility.PlaceDetail
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.ProfileApi

/** Estado de la pantalla de detalle de un lugar. */
data class PlaceDetailUiState(
    val loading: Boolean = true,
    val detail: PlaceDetail? = null,
    // Necesidades del perfil funcional del usuario, para agrupar el desglose
    // ("para ti" primero). Vacio = sin perfil -> todo es igual de relevante.
    val userNeeds: Set<String> = emptySet(),
    val error: String? = null
)

/** Carga GET /places/:id (desglose por dimension/criterio + comentarios + fotos). */
class PlaceDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow(PlaceDetailUiState())
    val state: StateFlow<PlaceDetailUiState> = _state

    fun load(placeId: Long) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            // El perfil es opcional: si falla (sin red, sin perfil), seguimos sin el.
            val needs = ProfileApi.get().getOrNull()?.needs?.toSet() ?: emptySet()
            PlaceApi.detail(placeId)
                .onSuccess {
                    _state.value = PlaceDetailUiState(loading = false, detail = it, userNeeds = needs)
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        userNeeds = needs,
                        error = "No se pudo cargar el detalle. Comprueba tu conexión."
                    )
                }
        }
    }
}
