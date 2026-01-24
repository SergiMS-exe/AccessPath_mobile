package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.s3m4su.accesspath.data.Place

@Composable
expect fun MapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float = 15f
)

@Composable
expect fun MapViewWithMarkers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float = 15f,
    places: List<Place>,
    onPlaceClick: (Place) -> Unit
)
