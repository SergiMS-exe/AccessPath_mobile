package org.s3m4su.accesspath.ui.components

import org.s3m4su.accesspath.data.AccessibilityFeature
import org.s3m4su.accesspath.data.AccessibilityScore
import org.s3m4su.accesspath.data.MockPlaces
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory

// Datos de muestra compartidos por las previews de vistas y de componentes.
// `internal` para poder usarlos desde varios ficheros del mismo modulo.

internal val previewPlaceCafe = Place(
    id = "1",
    name = "Café Central",
    address = "Av. Principal 123, Madrid",
    latitude = 40.4168,
    longitude = -3.7038,
    rating = 4.8f,
    category = PlaceCategory.CAFE,
    description = "Café acogedor en el centro con rampa de acceso y baño adaptado.",
    physicalAccessibility = AccessibilityScore.fromScore(4.5),
    sensoryAccessibility = AccessibilityScore.fromScore(3.8),
    cognitiveAccessibility = AccessibilityScore.fromScore(4.2),
    features = listOf(
        AccessibilityFeature.WHEELCHAIR_ACCESS,
        AccessibilityFeature.RAMP,
        AccessibilityFeature.ACCESSIBLE_BATHROOM,
        AccessibilityFeature.CLEAR_SIGNAGE
    )
)

internal val previewPlaceMuseum = Place(
    id = "3",
    name = "Museo del Prado",
    address = "Paseo del Prado s/n, Madrid",
    latitude = 40.4138,
    longitude = -3.6921,
    rating = 4.9f,
    category = PlaceCategory.MUSEUM,
    description = "Uno de los museos mas importantes del mundo, completamente accesible.",
    physicalAccessibility = AccessibilityScore.fromScore(5.0),
    sensoryAccessibility = AccessibilityScore.fromScore(4.8),
    cognitiveAccessibility = AccessibilityScore.fromScore(4.5),
    features = listOf(
        AccessibilityFeature.WHEELCHAIR_ACCESS,
        AccessibilityFeature.ELEVATOR,
        AccessibilityFeature.ACCESSIBLE_BATHROOM,
        AccessibilityFeature.BRAILLE_SIGNAGE,
        AccessibilityFeature.AUDIO_GUIDES,
        AccessibilityFeature.PICTOGRAMS,
        AccessibilityFeature.SIGN_LANGUAGE
    )
)

internal val previewPlaceLibrary = Place(
    id = "5",
    name = "Biblioteca Nacional",
    address = "Paseo de Recoletos 20, Madrid",
    latitude = 40.4230,
    longitude = -3.6920,
    rating = 4.7f,
    category = PlaceCategory.LIBRARY,
    description = "Biblioteca historica con todos los servicios de accesibilidad disponibles.",
    physicalAccessibility = AccessibilityScore.fromScore(4.8),
    sensoryAccessibility = AccessibilityScore.fromScore(4.6),
    cognitiveAccessibility = AccessibilityScore.fromScore(4.9),
    features = listOf(
        AccessibilityFeature.WHEELCHAIR_ACCESS,
        AccessibilityFeature.ELEVATOR,
        AccessibilityFeature.ACCESSIBLE_BATHROOM,
        AccessibilityFeature.HEARING_LOOP,
        AccessibilityFeature.BRAILLE_SIGNAGE,
        AccessibilityFeature.EASY_READING,
        AccessibilityFeature.QUIET_SPACE,
        AccessibilityFeature.TACTILE_PAVING
    )
)

internal val previewPlaceDifficult = Place(
    id = "6",
    name = "Centro Comercial Norte",
    address = "Calle Serrano 89, Madrid",
    latitude = 40.4198,
    longitude = -3.7068,
    rating = 4.3f,
    category = PlaceCategory.MALL,
    physicalAccessibility = AccessibilityScore.fromScore(1.5),
    sensoryAccessibility = AccessibilityScore.fromScore(1.8),
    cognitiveAccessibility = AccessibilityScore.fromScore(2.2),
    features = listOf(AccessibilityFeature.ELEVATOR)
)

internal val previewPlaceNoData = Place(
    id = "99",
    name = "Bar El Rincon",
    address = "Calle Alcala 88, Madrid",
    latitude = 40.4200,
    longitude = -3.6950,
    rating = 3.2f,
    category = PlaceCategory.RESTAURANT
)

internal val previewPlaces = MockPlaces.getMockPlaces()

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
