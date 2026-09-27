package org.s3m4su.accesspath.location

// Open + miembros open para permitir dobles de prueba en commonTest.
// La API publica no cambia: sigue siendo el mismo punto de extension por plataforma.
// El constructor sin parametros es deliberado: en Android el Context se inyecta
// via initLocationService(context) + createLocationService(), no por constructor,
// para que los tests en commonTest puedan hacer `object : LocationService { ... }`
// sin necesitar un Context real.
expect open class LocationService() {
    open suspend fun getCurrentLocation(): Location?
    open fun hasLocationPermission(): Boolean
    open suspend fun requestLocationPermission(): Boolean

    // True si los servicios de ubicacion (GPS) estan activados en el dispositivo.
    open fun isLocationEnabled(): Boolean
}

expect fun createLocationService(): LocationService
