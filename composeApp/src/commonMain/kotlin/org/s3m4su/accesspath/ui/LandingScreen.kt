package org.s3m4su.accesspath.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
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
import org.s3m4su.accesspath.map.MapView
import org.s3m4su.accesspath.ui.components.DrawerMenu
import org.s3m4su.accesspath.ui.components.DrawerMenuItem
import org.s3m4su.accesspath.ui.components.MapControlButtons
import org.s3m4su.accesspath.ui.components.PlaceBottomSheet
import org.s3m4su.accesspath.ui.components.TopSearchBar
import org.s3m4su.accesspath.ui.components.UserProfile
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

@Composable
fun LandingScreen(
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRequestPermission: (suspend () -> Boolean)? = null
) {
    // Acceder al tema desde el contexto (como useContext en React)
    val isDarkMode = AccessPathTheme.isDark
    val onDarkModeToggle = AccessPathTheme.toggleDarkMode
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var zoom by remember { mutableStateOf(15f) }
    var mapCenter by remember { mutableStateOf<Location?>(null) }
    var selectedMenuItem by remember { mutableStateOf(DrawerMenuItem.HOME) }

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

    LaunchedEffect(Unit) {
        places = MockPlaces.getMockPlaces()

        val hasPermission = locationService.hasLocationPermission() ||
            onRequestPermission?.invoke() ?: false

        if (hasPermission) {
            currentLocation = locationService.getCurrentLocation()
            mapCenter = currentLocation
        } else {
            mapCenter = Location(40.4168, -3.7038)
        }
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
        gesturesEnabled = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Map
            mapCenter?.let { center ->
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    latitude = center.latitude,
                    longitude = center.longitude,
                    zoom = zoom
                )
            }

            // Search bar at top
            TopSearchBar(
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onSearchClick = onSearchClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )

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
                        onDismiss = { selectedPlace = null },
                        onDetailsClick = {
                            println("Navigate to details for ${place.name}")
                        },
                        onNavigateClick = {
                            println("Navigate to ${place.name}")
                        }
                    )
                }
            }
        }
    }

    // Auto-select first place for demo purposes
    LaunchedEffect(places) {
        if (places.isNotEmpty() && selectedPlace == null) {
            kotlinx.coroutines.delay(1000)
            selectedPlace = places.first()
        }
    }
}
