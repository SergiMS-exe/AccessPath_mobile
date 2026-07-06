package org.s3m4su.accesspath.ui.components

import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.accessibility.Confidence
import org.s3m4su.accesspath.data.accessibility.CriterionScore
import org.s3m4su.accesspath.data.accessibility.DimensionScore

// Datos de muestra compartidos por las previews de vistas y de componentes.
// `internal` para poder usarlos desde varios ficheros del mismo modulo.

private fun previewCriterion(
    id: Long,
    key: String,
    prompt: String,
    state: AccessibilityState,
    nYes: Int = 3,
    nNo: Int = 0
) = CriterionScore(
    criterionId = id,
    key = key,
    prompt = prompt,
    isBlocking = false,
    state = state,
    conflict = false,
    nYes = nYes,
    nNo = nNo,
    nUnsure = 1,
    qualityP50 = 4.0,
    confidence = Confidence(level = "medium", nDefined = nYes + nNo, nUnsure = 1)
)

private fun previewDimension(
    id: Long,
    key: String,
    name: String,
    state: AccessibilityState
) = DimensionScore(
    dimensionId = id,
    key = key,
    name = name,
    state = state,
    conflict = false,
    criteria = listOf(
        previewCriterion(id * 10 + 1, "$key.uno", "¿Criterio de ejemplo uno?", state),
        previewCriterion(id * 10 + 2, "$key.dos", "¿Criterio de ejemplo dos?", AccessibilityState.NO_DATA, nYes = 0)
    )
)

internal val previewPlaceCafe = Place(
    id = "1",
    name = "Café Central",
    address = "Av. Principal 123, Madrid",
    latitude = 40.4168,
    longitude = -3.7038,
    category = PlaceCategory.CAFE,
    description = "Café acogedor en el centro con rampa de acceso y baño adaptado.",
    overallState = AccessibilityState.YELLOW,
    dimensions = listOf(
        previewDimension(1, "acceso", "Llegada y acceso", AccessibilityState.GREEN),
        previewDimension(2, "aseos", "Aseos adaptados", AccessibilityState.YELLOW)
    )
)

internal val previewPlaceMuseum = Place(
    id = "3",
    name = "Museo del Prado",
    address = "Paseo del Prado s/n, Madrid",
    latitude = 40.4138,
    longitude = -3.6921,
    category = PlaceCategory.MUSEUM,
    description = "Uno de los museos mas importantes del mundo, completamente accesible.",
    overallState = AccessibilityState.GREEN,
    dimensions = listOf(
        previewDimension(1, "acceso", "Llegada y acceso", AccessibilityState.GREEN),
        previewDimension(2, "aseos", "Aseos adaptados", AccessibilityState.GREEN),
        previewDimension(3, "sensorial_env", "Entorno sensorial", AccessibilityState.GREEN)
    )
)

internal val previewPlaceLibrary = Place(
    id = "5",
    name = "Biblioteca Nacional",
    address = "Paseo de Recoletos 20, Madrid",
    latitude = 40.4230,
    longitude = -3.6920,
    category = PlaceCategory.LIBRARY,
    description = "Biblioteca historica con todos los servicios de accesibilidad disponibles.",
    overallState = AccessibilityState.GREEN,
    dimensions = listOf(
        previewDimension(1, "acceso", "Llegada y acceso", AccessibilityState.GREEN),
        previewDimension(4, "auditiva", "Comunicacion auditiva", AccessibilityState.GREEN)
    )
)

internal val previewPlaceDifficult = Place(
    id = "6",
    name = "Centro Comercial Norte",
    address = "Calle Serrano 89, Madrid",
    latitude = 40.4198,
    longitude = -3.7068,
    category = PlaceCategory.MALL,
    overallState = AccessibilityState.RED,
    dimensions = listOf(
        previewDimension(1, "acceso", "Llegada y acceso", AccessibilityState.RED)
    )
)

internal val previewPlaceNoData = Place(
    id = "99",
    name = "Bar El Rincon",
    address = "Calle Alcala 88, Madrid",
    latitude = 40.4200,
    longitude = -3.6950,
    category = PlaceCategory.RESTAURANT
)

internal val previewPlaces = listOf(
    previewPlaceCafe,
    previewPlaceMuseum,
    previewPlaceLibrary,
    previewPlaceDifficult,
    previewPlaceNoData
)

internal val previewUserContributor = UserProfile(
    name = "Ana Garcia",
    badge = "Colaboradora",
    contributorLevel = 3,
    reviewCount = 47
)

internal val previewUserBasic = UserProfile(
    name = "Carlos M.",
    contributorLevel = 1,
    reviewCount = 4
)

// --- Fotos (seeds del placeholder) -------------------------------------------

internal val previewPhotos = listOf("prev-a", "prev-b", "prev-c", "prev-d")
