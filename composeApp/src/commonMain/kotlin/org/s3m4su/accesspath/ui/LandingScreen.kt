package org.s3m4su.accesspath.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.s3m4su.accesspath.location.Location
import org.s3m4su.accesspath.location.LocationService
import org.s3m4su.accesspath.location.createLocationService
import org.s3m4su.accesspath.map.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRequestPermission: (suspend () -> Boolean)? = null
) {
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val locationService = remember { createLocationService() }

    LaunchedEffect(permissionRequested) {
        hasPermission = locationService.hasLocationPermission()

        if (!hasPermission && !permissionRequested) {
            // Permission will be requested via platform-specific mechanism
            hasPermission = onRequestPermission?.invoke() ?: locationService.requestLocationPermission()
            permissionRequested = true
        }

        if (hasPermission) {
            currentLocation = locationService.getCurrentLocation()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AccessPath",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                !hasPermission -> {
                    Text(
                        text = "Location permission is required to show the map",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                currentLocation != null -> {
                    MapView(
                        modifier = Modifier.fillMaxSize(),
                        latitude = currentLocation!!.latitude,
                        longitude = currentLocation!!.longitude
                    )
                }
                else -> {
                    // Default location (fallback) - Bucharest, Romania
                    MapView(
                        modifier = Modifier.fillMaxSize(),
                        latitude = 44.4268,
                        longitude = 26.1025
                    )
                }
            }
        }
    }
}
