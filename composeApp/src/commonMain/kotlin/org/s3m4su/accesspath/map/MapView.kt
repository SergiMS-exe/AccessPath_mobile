package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float = 15f
)
