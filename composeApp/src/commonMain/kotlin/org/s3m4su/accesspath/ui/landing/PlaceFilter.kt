package org.s3m4su.accesspath.ui.landing

import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory

/**
 * Estado de filtrado de la pantalla principal. Combina la busqueda por texto,
 * el filtro por categorias y un minimo de accesibilidad media.
 *
 * Es inmutable: la UI crea copias con [copy] al cambiar cualquier criterio.
 */
data class PlaceFilter(
    val query: String = "",
    val categories: Set<PlaceCategory> = emptySet(),
    val minAccessibility: Float = 0f
) {
    /** Numero de filtros activos sin contar la busqueda por texto (para el badge). */
    val activeCount: Int
        get() = (if (categories.isNotEmpty()) 1 else 0) +
            (if (minAccessibility > 0f) 1 else 0)

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

        if (minAccessibility > 0f) {
            val score = place.averageAccessibility?.score ?: return false
            if (score < minAccessibility) return false
        }

        return true
    }
}

fun List<Place>.applyFilter(filter: PlaceFilter): List<Place> =
    filter(filter::matches)
