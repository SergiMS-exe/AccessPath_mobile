package org.s3m4su.accesspath.data

import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.accessibility.DimensionScore

/**
 * Lugar tal y como lo pinta el mapa y las listas.
 *
 * La accesibilidad es el semaforo de 4 estados por dimension (modelo v3):
 * [overallState] es el estado global (peor dimension con datos) para el color
 * del marcador, y [dimensions] el resumen por dimension. NUNCA se colapsa a una
 * media ni a un numero global.
 */
data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: PlaceCategory = PlaceCategory.OTHER,
    val imageUrl: String? = null,
    val description: String? = null,
    val overallState: AccessibilityState = AccessibilityState.NO_DATA,
    val dimensions: List<DimensionScore> = emptyList()
)

/**
 * Categorías de lugares
 */
enum class PlaceCategory {
    RESTAURANT,
    CAFE,
    HOTEL,
    MUSEUM,
    THEATER,
    LIBRARY,
    SHOP,
    MALL,
    HOSPITAL,
    PHARMACY,
    PARK,
    TRANSPORT,
    OTHER;

    fun getDisplayName(): String = when (this) {
        RESTAURANT -> "Restaurante"
        CAFE -> "Cafetería"
        HOTEL -> "Hotel"
        MUSEUM -> "Museo"
        THEATER -> "Teatro"
        LIBRARY -> "Biblioteca"
        SHOP -> "Tienda"
        MALL -> "Centro Comercial"
        HOSPITAL -> "Hospital"
        PHARMACY -> "Farmacia"
        PARK -> "Parque"
        TRANSPORT -> "Transporte"
        OTHER -> "Otro"
    }
}
