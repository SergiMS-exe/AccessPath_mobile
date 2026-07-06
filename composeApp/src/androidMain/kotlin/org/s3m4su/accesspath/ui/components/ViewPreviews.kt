package org.s3m4su.accesspath.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.s3m4su.accesspath.ui.auth.AuthScreen
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

// =============================================================================
// PREVIEWS DE VISTAS — pantallas completas de la app.
// (LandingScreen no se incluye: depende del mapa nativo de Google, que no
//  renderiza en el panel de previews. PlaceDetail/Contribute/Profile tampoco:
//  cargan datos por red via ViewModel y el panel no tiene ViewModelStoreOwner.)
// =============================================================================

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
