package org.s3m4su.accesspath.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.location.Location
import org.s3m4su.accesspath.map.MapBounds

/**
 * Estado del mapa que debe SOBREVIVIR a la navegacion (Detail/Contribute y
 * vuelta): lugares cargados, region ya cargada, centro y zoom. Al volver del
 * detalle no se repite la peticion al backend; el usuario recarga con
 * "Buscar en esta zona" si quiere datos frescos.
 */
class LandingViewModel : ViewModel() {
    var places by mutableStateOf<List<Place>>(emptyList())
    var loadedBounds by mutableStateOf<MapBounds?>(null)
    var mapCenter by mutableStateOf<Location?>(null)
    var zoom by mutableStateOf(15f)
    var currentLocation by mutableStateOf<Location?>(null)
    var locationDisabled by mutableStateOf(false)
}
