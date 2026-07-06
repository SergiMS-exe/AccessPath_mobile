package org.s3m4su.accesspath

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.data.auth.AuthState
import org.s3m4su.accesspath.ui.LandingScreen
import org.s3m4su.accesspath.ui.PlatformBackHandler
import org.s3m4su.accesspath.ui.auth.AuthScreen
import org.s3m4su.accesspath.ui.place.ContributeScreen
import org.s3m4su.accesspath.ui.place.PlaceDetailScreen
import org.s3m4su.accesspath.ui.profile.ProfileScreen
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

/** Pantallas registradas en el stack de navegacion. */
private sealed interface Screen {
    data object Auth : Screen
    data object Landing : Screen
    data class Detail(val place: Place) : Screen
    data class Contribute(val place: Place) : Screen
    data object Profile : Screen
}

/** Pantallas que entran deslizando de abajo arriba (modal sobre la anterior). */
private val Screen.isModal: Boolean
    get() = this is Screen.Detail || this is Screen.Contribute || this is Screen.Profile

@Composable
fun App(
    onRequestPermission: (suspend () -> Boolean)? = null
) {
    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkTheme) }

    // Stack de navegacion: el ultimo elemento es la pantalla activa.
    // Se inicializa segun el estado de auth ya restaurado desde Settings,
    // evitando pedir login innecesariamente al volver a la app.
    var stack by remember {
        val initial = if (AuthRepository.state.value is AuthState.Authenticated)
            listOf(Screen.Landing) else listOf(Screen.Auth)
        mutableStateOf(initial)
    }

    // Estado que debe sobrevivir el viaje Detail -> Landing (back).
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    val authState by AuthRepository.state.collectAsState()

    // Si el usuario cierra sesion, resetear el stack a Auth.
    if (authState is AuthState.Unauthenticated && stack.last() != Screen.Auth) {
        stack = listOf(Screen.Auth)
    }

    fun push(screen: Screen) { stack = stack + screen }
    fun pop() { if (stack.size > 1) stack = stack.dropLast(1) }

    AccessPathTheme(
        darkTheme = isDarkMode,
        onDarkThemeChange = { isDarkMode = it }
    ) {
        // Back button fisico/gesto solo activo cuando hay algo a lo que volver.
        PlatformBackHandler(enabled = stack.size > 1) { pop() }

        AnimatedContent(
            targetState = stack.last(),
            transitionSpec = {
                // Avanzar a una pantalla modal: entra deslizando hacia arriba.
                // Retroceder desde ella: la modal sale deslizando hacia abajo.
                if (targetState.isModal) {
                    (slideInVertically { it } + fadeIn()) togetherWith fadeOut()
                } else {
                    fadeIn() togetherWith (slideOutVertically { it } + fadeOut())
                }
            },
            label = "nav"
        ) { current ->
            when (current) {
                is Screen.Auth -> AuthScreen(
                    onAuthenticated = { stack = listOf(Screen.Landing) }
                )

                is Screen.Landing -> LandingScreen(
                    onRequestPermission = onRequestPermission,
                    selectedPlace = selectedPlace,
                    onSelectedPlaceChange = { selectedPlace = it },
                    onPlaceDetails = { place ->
                        selectedPlace = place
                        push(Screen.Detail(place))
                    },
                    onOpenProfile = { push(Screen.Profile) }
                )

                is Screen.Detail -> PlaceDetailScreen(
                    place = current.place,
                    onBack = ::pop,
                    onContribute = { place -> push(Screen.Contribute(place)) }
                )

                is Screen.Contribute -> ContributeScreen(
                    place = current.place,
                    onClose = ::pop
                )

                is Screen.Profile -> ProfileScreen(
                    onBack = ::pop
                )
            }
        }
    }
}
