package org.s3m4su.accesspath

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.ui.LandingScreen
import org.s3m4su.accesspath.ui.PlatformBackHandler
import org.s3m4su.accesspath.ui.place.PlaceDetailScreen
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

/** Pantallas registradas en el stack de navegacion. */
private sealed interface Screen {
    data object Landing : Screen
    data class Detail(val place: Place) : Screen
}

@Composable
fun App(
    onRequestPermission: (suspend () -> Boolean)? = null
) {
    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkTheme) }

    // Stack de navegacion: el ultimo elemento es la pantalla activa.
    var stack by remember { mutableStateOf(listOf<Screen>(Screen.Landing)) }

    // Estado que debe sobrevivir el viaje Detail -> Landing (back).
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    fun push(screen: Screen) { stack = stack + screen }
    fun pop() { if (stack.size > 1) stack = stack.dropLast(1) }

    AccessPathTheme(
        darkTheme = isDarkMode,
        onDarkThemeChange = { isDarkMode = it }
    ) {
        // Back button fisico/gesto solo activo cuando hay algo a lo que volver.
        PlatformBackHandler(enabled = stack.size > 1) { pop() }

        when (val current = stack.last()) {
            is Screen.Landing -> LandingScreen(
                onRequestPermission = onRequestPermission,
                selectedPlace = selectedPlace,
                onSelectedPlaceChange = { selectedPlace = it },
                onPlaceDetails = { place ->
                    selectedPlace = place
                    push(Screen.Detail(place))
                }
            )

            is Screen.Detail -> PlaceDetailScreen(
                place = current.place,
                onBack = ::pop
            )
        }
    }
}
