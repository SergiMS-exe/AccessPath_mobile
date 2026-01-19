package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float
) {
    val coordinate = remember(latitude, longitude) {
        CLLocationCoordinate2DMake(latitude, longitude)
    }

    // Convert zoom level to meters (approximate)
    val metersPerZoom = remember(zoom) {
        // Rough approximation: at zoom 15, show about 1km radius
        val baseMeters = 1000.0
        baseMeters * (15.0 / zoom)
    }

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                showsUserLocation = true
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    metersPerZoom,
                    metersPerZoom
                )
                setRegion(region, animated = false)
            }
        },
        update = { mapView ->
            val region = MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(latitude, longitude),
                metersPerZoom,
                metersPerZoom
            )
            mapView.setRegion(region, animated = true)
        }
    )
}
