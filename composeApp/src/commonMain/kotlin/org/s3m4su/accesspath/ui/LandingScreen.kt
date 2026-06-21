package org.s3m4su.accesspath.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.MockPlaces
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.location.Location
import org.s3m4su.accesspath.location.createLocationService
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val locationService = remember { createLocationService() }

    // Mock user profile
    val userProfile = remember {
        UserProfile(
            name = "Juan Pérez",
            badge = "GUÍA LOCAL",
            contributorLevel = 3,
            reviewCount = 45
        )
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

    LaunchedEffect(Unit) {
        places = MockPlaces.getMockPlaces()
        refreshLocation()
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
                    // TODO: Handle logout
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
                    onPlaceClick = { onSelectedPlaceChange(it) }
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
                    onMenuClick = { scope.launch { drawerState.open() } },
                    filter = filter,
                    onCategoriesChange = { filter = filter.copy(categories = it) },
                    onMinAccessibilityChange = { filter = filter.copy(minAccessibility = it) },
                    onClearFilters = { filter = filter.copy(categories = emptySet(), minAccessibility = 0f) }
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

            // Map control buttons
            MapControlButtons(
                onMyLocationClick = {
                    currentLocation?.let { location ->
                        mapCenter = location
                        zoom = 16f
                    }
                },
                onZoomInClick = {
                    if (zoom < 20f) zoom += 1f
                },
                onZoomOutClick = {
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
