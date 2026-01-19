package org.s3m4su.accesspath

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.s3m4su.accesspath.ui.LandingScreen

@Composable
fun App(
    onRequestPermission: (suspend () -> Boolean)? = null
) {
    MaterialTheme {
        LandingScreen(
            onMenuClick = { /* TODO: Open drawer menu */ },
            onSearchClick = { /* TODO: Open search */ },
            onRequestPermission = onRequestPermission
        )
    }
}