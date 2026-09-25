package org.s3m4su.accesspath.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.PlaceDto
import org.s3m4su.accesspath.data.api.toDomain
import org.s3m4su.accesspath.data.auth.AuthState
import org.s3m4su.accesspath.location.Location
import org.s3m4su.accesspath.location.LocationService
import org.s3m4su.accesspath.map.MapBounds
import org.s3m4su.accesspath.ui.components.DrawerMenuItem
import org.s3m4su.accesspath.ui.landing.PlaceFilter

/**
 * Estado de la pantalla principal. Sobrevive a la navegacion (Detail/Contribute
 * y vuelta): al volver del detalle no se repite la peticion al backend; el
 * usuario recarga con "Buscar en esta zona" si quiere datos frescos.
 */
data class LandingUiState(
    val places: List<Place> = emptyList(),
    val loadedBounds: MapBounds? = null,
    val mapBounds: MapBounds? = null,
    val mapCenter: Location? = null,
    val zoom: Float = 15f,
    val currentLocation: Location? = null,
    val locationDisabled: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val filter: PlaceFilter = PlaceFilter(),
    val searchActive: Boolean = false,
    val selectedMenuItem: DrawerMenuItem = DrawerMenuItem.HOME,
    val username: String = ""
)

class LandingViewModel : ViewModel() {

    private val _state = MutableStateFlow(LandingUiState())
    val state: StateFlow<LandingUiState> = _state.asStateFlow()

    // ---------- Mapa: region visible y carga ----------

    /** Actualiza la region visible reportada por la camara del mapa. */
    fun onCameraIdle(bounds: MapBounds) {
        _state.update { it.copy(mapBounds = bounds) }
    }

    /**
     * Carga lugares de la region visible y la marca como cargada. Si ya estaba
     * cargada la misma region, no repite la peticion.
     */
    fun loadArea(bounds: MapBounds) {
        if (_state.value.loadedBounds == bounds) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            PlaceApi.mapPins(bounds.minLat, bounds.maxLat, bounds.minLng, bounds.maxLng)
                .onSuccess { pins ->
                    _state.update {
                        it.copy(
                            loading = false,
                            places = pins,
                            loadedBounds = bounds
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = e.message ?: "No se pudo cargar la zona."
                        )
                    }
                }
        }
    }

    /**
     * Pide permiso y resuelve la ubicacion actual del dispositivo. [requestPermission]
     * se invoca si no hay permiso todavia (null si no hay forma de pedirlo).
     */
    fun refreshLocation(
        service: LocationService,
        requestPermission: (suspend () -> Boolean)?
    ) {
        _state.update { it.copy(locationDisabled = !service.isLocationEnabled()) }
        viewModelScope.launch {
            val granted = service.hasLocationPermission() ||
                (requestPermission?.invoke() ?: false)
            val loc = if (granted) service.getCurrentLocation() else null
            _state.update {
                it.copy(
                    currentLocation = loc,
                    mapCenter = loc ?: it.currentLocation ?: Location(40.4168, -3.7038)
                )
            }
        }
    }

    /** Centra el mapa en la ubicacion del dispositivo si la tenemos. */
    fun onMyLocationClick() {
        val loc = _state.value.currentLocation ?: return
        _state.update { it.copy(mapCenter = loc, zoom = 16f) }
    }

    fun onZoomIn() = _state.update { it.copy(zoom = (it.zoom + 1f).coerceAtMost(20f)) }
    fun onZoomOut() = _state.update { it.copy(zoom = (it.zoom - 1f).coerceAtLeast(5f)) }

    /**
     * Bootstrap de entrada: si volvemos del detalle con un lugar seleccionado,
     * centramos en el; si es arranque en frio, pedimos ubicacion.
     */
    fun bootstrap(
        selectedPlace: Place?,
        service: LocationService,
        requestPermission: (suspend () -> Boolean)?
    ) {
        val sp = selectedPlace
        if (sp != null) {
            _state.update { it.copy(mapCenter = Location(sp.latitude, sp.longitude)) }
        } else if (_state.value.mapCenter == null) {
            refreshLocation(service, requestPermission)
        }
    }

    // ---------- Seleccion y busqueda ----------

    fun onPlaceClicked(place: Place) {
        _state.update {
            it.copy(mapCenter = Location(place.latitude, place.longitude))
        }
    }

    fun onPlaceSelectedFromSearch(place: Place) {
        _state.update {
            it.copy(
                mapCenter = Location(place.latitude, place.longitude),
                filter = it.filter.copy(query = "")
            )
        }
    }

    /**
     * Sitio recien importado de Google: aun no esta publicado (sin valoracion),
     * asi que no vendra del endpoint del mapa. Lo anadimos localmente para poder
     * seleccionarlo y valorarlo.
     */
    fun onPlaceAdded(dto: PlaceDto) {
        val newPlace = dto.toDomain()
        _state.update {
            it.copy(
                places = it.places + newPlace,
                mapCenter = Location(dto.latitude, dto.longitude),
                filter = it.filter.copy(query = "")
            )
        }
    }

    fun onQueryChange(query: String) =
        _state.update { it.copy(filter = it.filter.copy(query = query)) }

    fun onCategoriesChange(categories: Set<PlaceCategory>) =
        _state.update { it.copy(filter = it.filter.copy(categories = categories)) }

    fun onStatesChange(states: Set<AccessibilityState>) =
        _state.update { it.copy(filter = it.filter.copy(states = states)) }

    fun onClearFilters() =
        _state.update {
            it.copy(filter = it.filter.copy(categories = emptySet(), states = emptySet()))
        }

    fun onSearchActiveChange(active: Boolean) =
        _state.update { it.copy(searchActive = active) }

    // ---------- Drawer ----------

    fun onMenuItemSelected(item: DrawerMenuItem) =
        _state.update { it.copy(selectedMenuItem = item) }

    fun onAuthStateChanged(authState: AuthState) {
        val username = (authState as? AuthState.Authenticated)?.user?.username ?: ""
        _state.update { it.copy(username = username) }
    }
}