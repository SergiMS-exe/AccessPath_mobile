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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.api.toDomain
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.location.createLocationService
import org.s3m4su.accesspath.map.MapBounds
import org.s3m4su.accesspath.map.MapViewWithMarkers
import org.s3m4su.accesspath.ui.components.DrawerMenu
import org.s3m4su.accesspath.ui.components.DrawerMenuItem
import org.s3m4su.accesspath.ui.components.MapControlButtons
import org.s3m4su.accesspath.ui.components.PlaceBottomSheet
import org.s3m4su.accesspath.ui.components.SearchBar
import org.s3m4su.accesspath.ui.components.UserProfile
import org.s3m4su.accesspath.ui.landing.applyFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

@Composable
fun LandingScreen(
    onRequestPermission: (suspend () -> Boolean)? = null,
    selectedPlace: Place? = null,
    onSelectedPlaceChange: (Place?) -> Unit = {},
    onPlaceDetails: (Place) -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val isDarkMode = AccessPathTheme.isDark
    val onDarkModeToggle = AccessPathTheme.toggleDarkMode

    val viewModel: LandingViewModel = viewModel(key = "landing") { LandingViewModel() }
    val state by viewModel.state.collectAsState()

    // Propaga el username autenticado al estado del VM para la cabecera del drawer.
    val authState by AuthRepository.state.collectAsState()
    LaunchedEffect(authState) { viewModel.onAuthStateChanged(authState) }

    val locationService = remember { createLocationService() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Lista visible tras aplicar busqueda + filtros sobre el dataset cargado.
    val visiblePlaces = remember(state.places, state.filter) {
        state.places.applyFilter(state.filter)
    }

    val userProfile = remember(state.username) { UserProfile(name = state.username) }

    // Bootstrap: al volver del detalle centra en selectedPlace; en arranque en
    // frio pide ubicacion. Lo dispara el VM, no la UI.
    LaunchedEffect(Unit) {
        viewModel.bootstrap(selectedPlace, locationService, onRequestPermission)
    }

    // Primera carga: en cuanto el mapa reporta su primera region visible.
    LaunchedEffect(state.mapBounds) {
        val b = state.mapBounds
        if (b != null && state.loadedBounds == null) viewModel.loadArea(b)
    }

    val showSearchArea = remember(state.mapBounds, state.loadedBounds) {
        val current = state.mapBounds
        val loaded = state.loadedBounds
        current != null && loaded != null && movedEnough(loaded, current)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                userProfile = userProfile,
                selectedItem = state.selectedMenuItem,
                isDarkMode = isDarkMode,
                onItemSelected = { item ->
                    scope.launch { drawerState.close() }
                    if (item == DrawerMenuItem.ACCESSIBILITY_PROFILE) {
                        onOpenProfile()
                    } else {
                        viewModel.onMenuItemSelected(item)
                    }
                },
                onDarkModeToggle = onDarkModeToggle,
                onLogout = {
                    scope.launch { drawerState.close() }
                    AuthRepository.logout()
                }
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            state.mapCenter?.let { center ->
                MapViewWithMarkers(
                    modifier = Modifier.fillMaxSize(),
                    latitude = center.latitude,
                    longitude = center.longitude,
                    zoom = state.zoom,
                    places = visiblePlaces,
                    onPlaceClick = { place ->
                        onSelectedPlaceChange(place)
                        viewModel.onPlaceClicked(place)
                    },
                    onCameraIdle = { viewModel.onCameraIdle(it) }
                )
            }

            if (state.searchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { focusManager.clearFocus() }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                SearchBar(
                    query = state.filter.query,
                    onQueryChange = { viewModel.onQueryChange(it) },
                    onMenuClick = {
                        focusManager.clearFocus()
                        scope.launch { drawerState.open() }
                    },
                    filter = state.filter,
                    onCategoriesChange = { viewModel.onCategoriesChange(it) },
                    onStatesChange = { viewModel.onStatesChange(it) },
                    onClearFilters = { viewModel.onClearFilters() },
                    places = state.places,
                    onActiveChange = { viewModel.onSearchActiveChange(it) },
                    onPlaceSelected = { place ->
                        onSelectedPlaceChange(place)
                        viewModel.onPlaceSelectedFromSearch(place)
                    },
                    onPlaceAdded = { dto ->
                        val newPlace = dto.toDomain()
                        viewModel.onPlaceAdded(dto)
                        onSelectedPlaceChange(newPlace)
                    }
                )

                if (state.locationDisabled) {
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
                                onClick = {
                                    viewModel.refreshLocation(locationService, onRequestPermission)
                                }
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
                        state.mapBounds?.let { b -> viewModel.loadArea(b) }
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

            MapControlButtons(
                onMyLocationClick = {
                    focusManager.clearFocus()
                    viewModel.onMyLocationClick()
                },
                onZoomInClick = {
                    focusManager.clearFocus()
                    viewModel.onZoomIn()
                },
                onZoomOutClick = {
                    focusManager.clearFocus()
                    viewModel.onZoomOut()
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
            )

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