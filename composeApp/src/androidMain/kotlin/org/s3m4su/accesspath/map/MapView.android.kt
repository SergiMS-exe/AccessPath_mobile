package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.AccessibilityLevel

@Composable
actual fun MapViewWithMarkers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    places: List<Place>,
    onPlaceClick: (Place) -> Unit
) {
    val position = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, zoom)
    }

    LaunchedEffect(latitude, longitude, zoom) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom)
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    ) {
        // Add markers for all places
        places.forEach { place ->
            val markerPosition = LatLng(place.latitude, place.longitude)
            val markerColor = place.averageAccessibility?.level?.let { level ->
                when (level) {
                    AccessibilityLevel.VERY_EASY -> BitmapDescriptorFactory.HUE_GREEN
                    AccessibilityLevel.EASY -> BitmapDescriptorFactory.HUE_YELLOW
                    AccessibilityLevel.MODERATE -> BitmapDescriptorFactory.HUE_ORANGE
                    AccessibilityLevel.DIFFICULT -> BitmapDescriptorFactory.HUE_RED
                }
            } ?: BitmapDescriptorFactory.HUE_AZURE

            Marker(
                state = MarkerState(position = markerPosition),
                title = place.name,
                snippet = place.address,
                icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                onClick = {
                    onPlaceClick(place)
                    true
                }
            )
        }
    }
}

// Keep the original MapView for backward compatibility
@Composable
actual fun MapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float
) {
    MapViewWithMarkers(
        modifier = modifier,
        latitude = latitude,
        longitude = longitude,
        zoom = zoom,
        places = emptyList(),
        onPlaceClick = {}
    )
}
