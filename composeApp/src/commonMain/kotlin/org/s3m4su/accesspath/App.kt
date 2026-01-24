package org.s3m4su.accesspath

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.s3m4su.accesspath.ui.LandingScreen
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

@Composable
fun App(
    onRequestPermission: (suspend () -> Boolean)? = null
) {
    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkTheme) }

    AccessPathTheme(
        darkTheme = isDarkMode,
        onDarkThemeChange = { isDarkMode = it }
    ) {
        LandingScreen(
            onMenuClick = {
                println("Menu clicked")
            },
            onSearchClick = {
                println("Search clicked")
            },
            onRequestPermission = onRequestPermission
        )
    }
}
