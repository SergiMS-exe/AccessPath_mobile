package org.s3m4su.accesspath.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.data.auth.AuthState
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.toDomain
import org.s3m4su.accesspath.location.Location
import org.s3m4su.accesspath.location.createLocationService
import org.s3m4su.accesspath.map.MapBounds
import org.s3m4su.accesspath.map.MapViewWithMarkers
import org.s3m4su.accesspath.ui.components.DrawerMenu
import org.s3m4su.accesspath.ui.components.DrawerMenuItem
import org.s3m4su.accesspath.ui.components.MapControlButtons
import org.s3m4su.accesspath.ui.components.PlaceBottomSheet
import org.s3m4su.accesspath.ui.components.SearchBar
import org.s3m4su.accesspath.ui.components.UserProfile
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import org.s3m4su.accesspath.ui.landing.applyFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

@Composable
fun LandingScreen(
    onRequestPermission: (suspend () -> Boolean)? = null,
    selectedPlace: Place? = null,
    onSelectedPlaceChange: (Place?) -> Unit = {},
    onPlaceDetails: (Place) -> Unit = {}
) {
    // Acceder al tema desde el contexto (como useContext en React)
    val isDarkMode = AccessPathTheme.isDark
    val onDarkModeToggle = AccessPathTheme.toggleDarkMode
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var zoom by remember { mutableStateOf(15f) }
    var mapCenter by remember { mutableStateOf<Location?>(null) }
    var locationDisabled by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf(DrawerMenuItem.HOME) }
    var filter by remember { mutableStateOf(PlaceFilter()) }

    // Lista visible tras aplicar busqueda + filtros sobre el dataset cargado.
    val visiblePlaces = remember(places, filter) { places.applyFilter(filter) }

    var searchActive by remember { mutableStateOf(false) }
    var mapBounds by remember { mutableStateOf<MapBounds?>(null) }   // region visible actual
    var loadedBounds by remember { mutableStateOf<MapBounds?>(null) } // region ya cargada
    val focusManager = LocalFocusManager.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val locationService = remember { createLocationService() }

    val authState by AuthRepository.state.collectAsState()
    val userProfile = remember(authState) {
        val username = (authState as? AuthState.Authenticated)?.user?.username ?: ""
        UserProfile(name = username)
    }

    // Reejecutable desde el boton de recargar del banner: detecta el estado de
    // ubicacion, pide el fix si hay permiso y centra el mapa.
    val refreshLocation: suspend () -> Unit = {
        locationDisabled = !locationService.isLocationEnabled()

        val hasPermission = locationService.hasLocationPermission() ||
            onRequestPermission?.invoke() ?: false

        if (hasPermission) {
            currentLocation = locationService.getCurrentLocation()
        }
        // Siempre centrar el mapa: si no hay ubicacion (permiso denegado o
        // GPS apagado / sin fix), caer a Madrid como fallback.
        mapCenter = currentLocation ?: Location(40.4168, -3.7038)
    }

    // Trae los lugares publicados de una region y la marca como cargada.
    val loadArea: suspend (MapBounds) -> Unit = { b ->
        PlaceApi.mapPlaces(b.minLat, b.maxLat, b.minLng, b.maxLng)
            .onSuccess { dtos -> places = dtos.map { it.toDomain() } }
        loadedBounds = b
    }

    LaunchedEffect(Unit) {
        // Al volver del detalle selectedPlace ya tiene valor: centrar en el lugar.
        // En el arranque inicial no hay lugar seleccionado: centrar en el usuario.
        if (selectedPlace != null) {
            mapCenter = Location(selectedPlace.latitude, selectedPlace.longitude)
        } else {
            refreshLocation()
        }
    }

    // Carga inicial: en cuanto el mapa reporta su primera region visible, cargarla.
    // Despues NO recargamos solos al panear; eso lo decide el usuario con el boton.
    LaunchedEffect(mapBounds) {
        val b = mapBounds
        if (b != null && loadedBounds == null) loadArea(b)
    }

    // El boton "Buscar en esta zona" aparece cuando la region se ha movido
    // lo bastante respecto a la ultima cargada.
    val showSearchArea = remember(mapBounds, loadedBounds) {
        val current = mapBounds
        val loaded = loadedBounds
        current != null && loaded != null && movedEnough(loaded, current)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                userProfile = userProfile,
                selectedItem = selectedMenuItem,
                isDarkMode = isDarkMode,
                onItemSelected = { item ->
                    selectedMenuItem = item
                    scope.launch { drawerState.close() }
                },
                onDarkModeToggle = onDarkModeToggle,
                onLogout = {
                    scope.launch { drawerState.close() }
                    AuthRepository.logout()
                }
            )
        },
        // Permitir cerrar deslizando cuando el drawer esta abierto, pero evitar
        // que el gesto de panear el mapa lo abra cuando esta cerrado.
        gesturesEnabled = drawerState.isOpen
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Map
            mapCenter?.let { center ->
                MapViewWithMarkers(
                    modifier = Modifier.fillMaxSize(),
                    latitude = center.latitude,
                    longitude = center.longitude,
                    zoom = zoom,
                    places = visiblePlaces,
                    onPlaceClick = { place ->
                        onSelectedPlaceChange(place)
                        mapCenter = Location(place.latitude, place.longitude)
                    },
                    onCameraIdle = { mapBounds = it }
                )
            }

            if (searchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { focusManager.clearFocus() }
                )
            }

            // Bloque superior: barra de busqueda + filtros + aviso de ubicacion
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                SearchBar(
                    query = filter.query,
                    onQueryChange = { filter = filter.copy(query = it) },
                    onMenuClick = {
                        focusManager.clearFocus()
                        scope.launch { drawerState.open() }
                    },
                    filter = filter,
                    onCategoriesChange = { filter = filter.copy(categories = it) },
                    onMinAccessibilityChange = { filter = filter.copy(minAccessibility = it) },
                    onClearFilters = { filter = filter.copy(categories = emptySet(), minAccessibility = 0f) },
                    places = places,
                    onActiveChange = { searchActive = it },
                    onPlaceSelected = { place ->
                        onSelectedPlaceChange(place)
                        mapCenter = Location(place.latitude, place.longitude)
                        filter = filter.copy(query = "")
                    },
                    onPlaceAdded = { dto ->
                        // Sitio recien importado de Google: aun no esta publicado
                        // (sin valoracion), asi que no vendra del endpoint del mapa.
                        // Lo anadimos localmente para poder seleccionarlo y valorarlo.
                        val newPlace = dto.toDomain()
                        places = places + newPlace
                        onSelectedPlaceChange(newPlace)
                        mapCenter = Location(dto.latitude, dto.longitude)
                        filter = filter.copy(query = "")
                    }
                )

                // Aviso cuando la ubicacion del dispositivo esta apagada
                if (locationDisabled) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "La ubicacion esta desactivada. Activala en los ajustes y pulsa recargar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { scope.launch { refreshLocation() } }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Recargar ubicacion",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Boton "Buscar en esta zona": recarga los lugares de la region visible
            // solo cuando el usuario lo pide, tras haber movido el mapa.
            AnimatedVisibility(
                visible = showSearchArea,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 88.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AccessPathTheme.colors.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier.clickable {
                        mapBounds?.let { b -> scope.launch { loadArea(b) } }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Buscar en esta zona",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            // Map control buttons
            MapControlButtons(
                onMyLocationClick = {
                    focusManager.clearFocus()
                    currentLocation?.let { location ->
                        mapCenter = location
                        zoom = 16f
                    }
                },
                onZoomInClick = {
                    focusManager.clearFocus()
                    if (zoom < 20f) zoom += 1f
                },
                onZoomOutClick = {
                    focusManager.clearFocus()
                    if (zoom > 5f) zoom -= 1f
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
            )

            // Bottom sheet for selected place
            selectedPlace?.let { place ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    PlaceBottomSheet(
                        place = place,
                        onDismiss = { onSelectedPlaceChange(null) },
                        onDetailsClick = { onPlaceDetails(place) },
                        onNavigateClick = {
                            println("Navigate to ${place.name}")
                        }
                    )
                }
            }

        }
    }
}

/**
 * Decide si la region visible se ha alejado lo suficiente de la ya cargada como
 * para ofrecer recargar. Umbral: 30% del ancho/alto, o un cambio de zoom similar.
 */
private fun movedEnough(loaded: MapBounds, current: MapBounds): Boolean {
    val latSpan = loaded.maxLat - loaded.minLat
    val lngSpan = loaded.maxLng - loaded.minLng
    if (latSpan <= 0.0 || lngSpan <= 0.0) return false

    val loadedCenterLat = (loaded.minLat + loaded.maxLat) / 2
    val loadedCenterLng = (loaded.minLng + loaded.maxLng) / 2
    val currentCenterLat = (current.minLat + current.maxLat) / 2
    val currentCenterLng = (current.minLng + current.maxLng) / 2

    val movedLat = abs(currentCenterLat - loadedCenterLat) > latSpan * 0.3
    val movedLng = abs(currentCenterLng - loadedCenterLng) > lngSpan * 0.3
    val zoomed = abs((current.maxLat - current.minLat) - latSpan) > latSpan * 0.3

    return movedLat || movedLng || zoomed
}
