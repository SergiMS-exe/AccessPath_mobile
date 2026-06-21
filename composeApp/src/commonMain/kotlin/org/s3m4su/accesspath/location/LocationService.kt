package org.s3m4su.accesspath.location

expect class LocationService {
    suspend fun getCurrentLocation(): Location?
    fun hasLocationPermission(): Boolean
    suspend fun requestLocationPermission(): Boolean

    // True si los servicios de ubicacion (GPS) estan activados en el dispositivo.
    fun isLocationEnabled(): Boolean
}

expect fun createLocationService(): LocationService
