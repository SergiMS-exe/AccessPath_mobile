package org.s3m4su.accesspath.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class LocationService {
    private val locationManager = CLLocationManager()

    actual fun hasLocationPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
               status == kCLAuthorizationStatusAuthorizedAlways
    }

    actual suspend fun requestLocationPermission(): Boolean {
        if (hasLocationPermission()) {
            return true
        }

        return suspendCancellableCoroutine { continuation ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val status = CLLocationManager.authorizationStatus()
                    if (status != platform.CoreLocation.kCLAuthorizationStatusNotDetermined) {
                        continuation.resume(
                            status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                            status == kCLAuthorizationStatusAuthorizedAlways
                        )
                    }
                }
            }
            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()
        }
    }

    actual suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            locationManager.desiredAccuracy = kCLLocationAccuracyBest

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(
                    manager: CLLocationManager,
                    didUpdateLocations: List<*>
                ) {
                    val location = didUpdateLocations.lastOrNull() as? platform.CoreLocation.CLLocation
                    if (location != null) {
                        manager.stopUpdatingLocation()
                        val coord = location.coordinate.useContents {
                            Location(latitude = latitude, longitude = longitude)
                        }
                        continuation.resume(coord)
                    }
                }

                override fun locationManager(
                    manager: CLLocationManager,
                    didFailWithError: platform.Foundation.NSError
                ) {
                    manager.stopUpdatingLocation()
                    continuation.resume(null)
                }
            }

            locationManager.delegate = delegate
            locationManager.startUpdatingLocation()

            continuation.invokeOnCancellation {
                locationManager.stopUpdatingLocation()
            }
        }
    }
}

actual fun createLocationService(): LocationService = LocationService()
