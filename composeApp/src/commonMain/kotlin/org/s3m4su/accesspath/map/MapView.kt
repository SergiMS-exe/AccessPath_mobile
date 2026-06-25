package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.s3m4su.accesspath.data.Place

/** Region visible del mapa, usada para pedir al backend solo los lugares en pantalla. */
data class MapBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)

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
    onPlaceClick: (Place) -> Unit,
    // Se invoca cuando la camara deja de moverse, con la region visible resultante.
    onCameraIdle: ((MapBounds) -> Unit)? = null
)
