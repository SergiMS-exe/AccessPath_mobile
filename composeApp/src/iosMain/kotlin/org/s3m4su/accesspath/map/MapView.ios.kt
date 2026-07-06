package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.*
import platform.darwin.NSObject
import platform.UIKit.UIColor
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.accessibility.AccessibilityState

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapViewWithMarkers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    places: List<Place>,
    onPlaceClick: (Place) -> Unit,
    onCameraIdle: ((MapBounds) -> Unit)?
) {
    val metersPerZoom = remember(zoom) {
        val baseMeters = 1000.0
        baseMeters * (15.0 / zoom)
    }

    // Estado "vivo" que el delegate lee en cada callback: asi los closures no se
    // quedan con la lista de lugares de la primera composicion.
    val currentPlaces = rememberUpdatedState(places)
    val currentOnPlaceClick = rememberUpdatedState(onPlaceClick)
    val currentOnCameraIdle = rememberUpdatedState(onCameraIdle)

    // El delegate se crea UNA vez y se recuerda: si se creara dentro de factory
    // sin referencia fuerte, el GC de Kotlin/Native podria liberarlo (MKMapView
    // solo guarda una referencia debil a su delegate).
    val mapDelegate = remember {
        object : NSObject(), MKMapViewDelegateProtocol {
            // Cuando el usuario deja de mover el mapa, reporta la region visible.
            override fun mapView(
                mapView: MKMapView,
                regionDidChangeAnimated: Boolean
            ) {
                mapView.region.useContents {
                    currentOnCameraIdle.value?.invoke(
                        MapBounds(
                            minLat = center.latitude - span.latitudeDelta / 2.0,
                            maxLat = center.latitude + span.latitudeDelta / 2.0,
                            minLng = center.longitude - span.longitudeDelta / 2.0,
                            maxLng = center.longitude + span.longitudeDelta / 2.0
                        )
                    )
                }
            }

            override fun mapView(
                mapView: MKMapView,
                didSelectAnnotation: MKAnnotationProtocol
            ) {
                val annotation = didSelectAnnotation as? MKPointAnnotation
                annotation?.let {
                    currentPlaces.value.find { place ->
                        place.name == annotation.title()
                    }?.let { place ->
                        currentOnPlaceClick.value(place)
                    }
                }
            }

            override fun mapView(
                mapView: MKMapView,
                viewForAnnotation: MKAnnotationProtocol
            ): MKAnnotationView? {
                if (viewForAnnotation is MKUserLocation) {
                    return null
                }

                val identifier = "PlaceMarker"
                var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier)

                if (annotationView == null) {
                    annotationView = MKMarkerAnnotationView(viewForAnnotation, identifier)
                    annotationView.canShowCallout = true
                } else {
                    annotationView.annotation = viewForAnnotation
                }

                // Colorea por el estado global del semaforo (4 estados).
                val markerView = annotationView as? MKMarkerAnnotationView
                val annotation = viewForAnnotation as? MKPointAnnotation
                annotation?.title()?.let { title ->
                    currentPlaces.value.find { it.name == title }?.let { place ->
                        markerView?.markerTintColor = when (place.overallState) {
                            AccessibilityState.GREEN -> UIColor.systemGreenColor
                            AccessibilityState.YELLOW -> UIColor.systemOrangeColor
                            AccessibilityState.RED -> UIColor.systemRedColor
                            AccessibilityState.NO_DATA -> UIColor.systemBlueColor
                        }
                    }
                }

                return annotationView
            }
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                showsUserLocation = true

                val region = MKCoordinateRegionMakeWithDistance(
                    CLLocationCoordinate2DMake(latitude, longitude),
                    metersPerZoom,
                    metersPerZoom
                )
                setRegion(region, animated = false)
                setDelegate(mapDelegate)
            }
        },
        update = { mapView ->
            // Centrar/zoom en cada actualizacion de estado.
            val region = MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(latitude, longitude),
                metersPerZoom,
                metersPerZoom
            )
            mapView.setRegion(region, animated = true)

            // Sincronizar los marcadores con la lista actual (no solo en factory:
            // los lugares llegan async despues de crear el mapa).
            val existing = mapView.annotations.filterIsInstance<MKPointAnnotation>()
            mapView.removeAnnotations(existing)
            places.forEach { place ->
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(CLLocationCoordinate2DMake(place.latitude, place.longitude))
                    setTitle(place.name)
                    setSubtitle(place.address)
                }
                mapView.addAnnotation(annotation)
            }
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
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
        onPlaceClick = {},
        onCameraIdle = null
    )
}
