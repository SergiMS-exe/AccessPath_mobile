package org.s3m4su.accesspath.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.s3m4su.accesspath.ui.auth.AuthScreen
import org.s3m4su.accesspath.ui.place.PlaceDetailScreen
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

// =============================================================================
// PREVIEWS DE VISTAS — pantallas completas de la app.
// (LandingScreen no se incluye: depende del mapa nativo de Google, que no
//  renderiza en el panel de previews.)
// =============================================================================

// --- PlaceDetailScreen -------------------------------------------------------

@Preview(showSystemUi = true, name = "PlaceDetail — Biblioteca (Light)")
@Composable
fun PlaceDetailLightPreview() {
    AccessPathTheme(darkTheme = false) {
        PlaceDetailScreen(
            place = previewPlaceLibrary,
            onBack = {}
        )
    }
}

@Preview(showSystemUi = true, name = "PlaceDetail — Museo (Dark)", backgroundColor = 0xFF121212)
@Composable
fun PlaceDetailDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        PlaceDetailScreen(
            place = previewPlaceMuseum,
            onBack = {}
        )
    }
}

@Preview(showSystemUi = true, name = "PlaceDetail — sin datos de accesibilidad")
@Composable
fun PlaceDetailNoDataPreview() {
    AccessPathTheme(darkTheme = false) {
        PlaceDetailScreen(
            place = previewPlaceNoData,
            onBack = {}
        )
    }
}

// --- AuthScreen --------------------------------------------------------------

@Preview(showSystemUi = true, name = "Auth — Login (Light)")
@Composable
fun AuthScreenLoginLightPreview() {
    AccessPathTheme(darkTheme = false) {
        AuthScreen(onAuthenticated = {})
    }
}

@Preview(showSystemUi = true, name = "Auth — Login (Dark)", backgroundColor = 0xFF121212)
@Composable
fun AuthScreenLoginDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        AuthScreen(onAuthenticated = {})
    }
}
