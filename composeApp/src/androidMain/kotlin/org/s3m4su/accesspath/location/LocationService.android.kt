package org.s3m4su.accesspath.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// El Context no se inyecta por constructor para que los tests en commonTest
// puedan hacer `object : LocationService { ... }` (la firma del expect no tiene
// parametros). En produccion, el Context se inyecta una sola vez via
// `initLocationService(context)` (llamado desde MainActivity.onCreate) y la
// factoria `createLocationService()` devuelve una instancia lista para usar.
actual open class LocationService {

    private val context: Context
        get() = appContext
            ?: error("LocationService not initialized. Call initLocationService(context) before using it.")

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    actual open fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual open suspend fun requestLocationPermission(): Boolean {
        // Permission request must be handled at the UI level
        // This is just a check, actual request is done via rememberLauncherForActivityResult
        return hasLocationPermission()
    }

    actual open fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    @SuppressLint("MissingPermission")
    actual open suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(Location(location.latitude, location.longitude))
                } else {
                    continuation.resume(null)
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}

private var appContext: Context? = null

fun initLocationService(context: Context) {
    appContext = context.applicationContext
}

actual fun createLocationService(): LocationService = LocationService()
