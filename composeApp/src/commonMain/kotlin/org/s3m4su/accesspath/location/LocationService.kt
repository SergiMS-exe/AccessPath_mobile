package org.s3m4su.accesspath.location

expect class LocationService {
    suspend fun getCurrentLocation(): Location?
    fun hasLocationPermission(): Boolean
    suspend fun requestLocationPermission(): Boolean
}

expect fun createLocationService(): LocationService
