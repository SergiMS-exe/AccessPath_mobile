package org.s3m4su.accesspath.ui.landing

import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState

/**
 * Estado de filtrado de la pantalla principal. Combina la busqueda por texto,
 * el filtro por categorias y el filtro por estado de accesibilidad (semaforo).
 * No hay filtro por "nota media": la accesibilidad nunca se colapsa a un numero.
 *
 * Es inmutable: la UI crea copias con [copy] al cambiar cualquier criterio.
 */
data class PlaceFilter(
    val query: String = "",
    val categories: Set<PlaceCategory> = emptySet(),
    val states: Set<AccessibilityState> = emptySet()
) {
    /** Numero de filtros activos sin contar la busqueda por texto (para el badge). */
    val activeCount: Int
        get() = (if (categories.isNotEmpty()) 1 else 0) +
            (if (states.isNotEmpty()) 1 else 0)

    fun matches(place: Place): Boolean {
        val q = query.trim()
        if (q.isNotEmpty() &&
            !place.name.contains(q, ignoreCase = true) &&
            !place.address.contains(q, ignoreCase = true)
        ) {
            return false
        }

        if (categories.isNotEmpty() && place.category !in categories) {
            return false
        }

        if (states.isNotEmpty() && place.overallState !in states) {
            return false
        }

        return true
    }
}

fun List<Place>.applyFilter(filter: PlaceFilter): List<Place> =
    filter(filter::matches)
