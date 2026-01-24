package org.s3m4su.accesspath.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.CValue
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.*
import platform.darwin.NSObject
import platform.UIKit.UIColor
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.AccessibilityLevel

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapViewWithMarkers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    places: List<Place>,
    onPlaceClick: (Place) -> Unit
) {
    val coordinate = remember(latitude, longitude) {
        CLLocationCoordinate2DMake(latitude, longitude)
    }

    val metersPerZoom = remember(zoom) {
        val baseMeters = 1000.0
        baseMeters * (15.0 / zoom)
    }

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                showsUserLocation = true
                
                // Set initial region
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    metersPerZoom,
                    metersPerZoom
                )
                setRegion(region, animated = false)
                
                // Add annotations for places
                places.forEach { place ->
                    val annotation = MKPointAnnotation().apply {
                        setCoordinate(CLLocationCoordinate2DMake(place.latitude, place.longitude))
                        setTitle(place.name)
                        setSubtitle(place.address)
                    }
                    addAnnotation(annotation)
                }
                
                // Set up delegate to handle marker taps
                val mapDelegate = object : NSObject(), MKMapViewDelegateProtocol {
                    override fun mapView(
                        mapView: MKMapView,
                        didSelectAnnotation: MKAnnotationProtocol
                    ) {
                        val annotation = didSelectAnnotation as? MKPointAnnotation
                        annotation?.let {
                            // Find the place that matches this annotation
                            places.find { place ->
                                place.name == annotation.title()
                            }?.let { place ->
                                onPlaceClick(place)
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
                        
                        // Color code markers based on accessibility
                        val markerView = annotationView as? MKMarkerAnnotationView
                        val annotation = viewForAnnotation as? MKPointAnnotation
                        annotation?.title()?.let { title ->
                            places.find { it.name == title }?.let { place ->
                                markerView?.markerTintColor = when (place.accessibilityLevel) {
                                    AccessibilityLevel.VERY_EASY -> UIColor.systemGreenColor
                                    AccessibilityLevel.EASY -> UIColor.systemYellowColor
                                    AccessibilityLevel.MODERATE -> UIColor.systemOrangeColor
                                    AccessibilityLevel.DIFFICULT -> UIColor.systemRedColor
                                }
                            }
                        }
                        
                        return annotationView
                    }
                }
                
                setDelegate(mapDelegate)
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
        onPlaceClick = {}
    )
}
