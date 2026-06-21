package org.s3m4su.accesspath.data

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val category: PlaceCategory,
    val imageUrl: String? = null,
    val description: String? = null,
    val physicalAccessibility: AccessibilityScore? = null,
    val sensoryAccessibility: AccessibilityScore? = null,
    val cognitiveAccessibility: AccessibilityScore? = null,
    val features: List<AccessibilityFeature> = emptyList()
) {
    /**
     * Calcula la puntuación promedio de accesibilidad considerando solo
     * las dimensiones que tienen valor (no null)
     */
    val averageAccessibility: AccessibilityScore?
        get() {
            val scores = listOfNotNull(
                physicalAccessibility,
                sensoryAccessibility,
                cognitiveAccessibility
            )

            if (scores.isEmpty()) return null

            val average = scores.map { it.score }.average()
            return AccessibilityScore(
                score = average,
                level = AccessibilityLevel.fromScore(average)
            )
        }
}

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

    // Devuelve el nombre del icono de Material Icons
    fun getIconName(): String = when (this) {
        RESTAURANT -> "Restaurant"
        CAFE -> "LocalCafe"
        HOTEL -> "Hotel"
        MUSEUM -> "Museum"
        THEATER -> "Theaters"
        LIBRARY -> "LocalLibrary"
        SHOP -> "Store"
        MALL -> "ShoppingBag"
        HOSPITAL -> "LocalHospital"
        PHARMACY -> "LocalPharmacy"
        PARK -> "Park"
        TRANSPORT -> "DirectionsTransit"
        OTHER -> "Place"
    }
}

/**
 * Representa una puntuación de accesibilidad (0.0 a 5.0)
 */
data class AccessibilityScore(
    val score: Double,
    val level: AccessibilityLevel
) {
    companion object {
        fun fromScore(score: Double): AccessibilityScore {
            return AccessibilityScore(
                score = score,
                level = AccessibilityLevel.fromScore(score)
            )
        }
    }
}

/**
 * Nivel de accesibilidad basado en el score numérico
 */
enum class AccessibilityLevel {
    VERY_EASY,    // 4.0 - 5.0
    EASY,         // 3.0 - 3.99
    MODERATE,     // 2.0 - 2.99
    DIFFICULT;    // 0.0 - 1.99

    companion object {
        fun fromScore(score: Double): AccessibilityLevel {
            return when {
                score >= 4.0 -> VERY_EASY
                score >= 3.0 -> EASY
                score >= 2.0 -> MODERATE
                else -> DIFFICULT
            }
        }
    }

    fun getDisplayName(): String = when (this) {
        VERY_EASY -> "Muy Fácil"
        EASY -> "Fácil"
        MODERATE -> "Moderado"
        DIFFICULT -> "Difícil"
    }
}

enum class AccessibilityDimension {
    PHYSICAL,
    SENSORY,
    COGNITIVE;

    fun getDisplayName(): String = when (this) {
        PHYSICAL -> "Fisica"
        SENSORY -> "Sensorial"
        COGNITIVE -> "Cognitiva"
    }
}

enum class AccessibilityFeature {
    // Físico
    WHEELCHAIR_ACCESS,
    RAMP,
    ELEVATOR,
    WIDE_DOORS,
    ACCESSIBLE_BATHROOM,
    PARKING,
    HANDRAILS,

    // Sensorial
    BRAILLE_SIGNAGE,
    HEARING_LOOP,
    SIGN_LANGUAGE,
    TACTILE_PAVING,
    AUDIO_GUIDES,

    // Cognitivo/Psíquico
    EASY_READING,
    PICTOGRAMS,
    QUIET_SPACE,
    CLEAR_SIGNAGE;

    val dimension: AccessibilityDimension
        get() = when (this) {
            WHEELCHAIR_ACCESS, RAMP, ELEVATOR, WIDE_DOORS,
            ACCESSIBLE_BATHROOM, PARKING, HANDRAILS -> AccessibilityDimension.PHYSICAL

            BRAILLE_SIGNAGE, HEARING_LOOP, SIGN_LANGUAGE,
            TACTILE_PAVING, AUDIO_GUIDES -> AccessibilityDimension.SENSORY

            EASY_READING, PICTOGRAMS, QUIET_SPACE,
            CLEAR_SIGNAGE -> AccessibilityDimension.COGNITIVE
        }

    fun getDisplayName(): String = when (this) {
        WHEELCHAIR_ACCESS -> "Acceso silla de ruedas"
        RAMP -> "Rampa de acceso"
        ELEVATOR -> "Ascensor"
        WIDE_DOORS -> "Puertas anchas"
        ACCESSIBLE_BATHROOM -> "Baño adaptado"
        PARKING -> "Aparcamiento adaptado"
        HANDRAILS -> "Pasamanos"
        BRAILLE_SIGNAGE -> "Señalización en Braille"
        HEARING_LOOP -> "Bucle magnético"
        SIGN_LANGUAGE -> "Lengua de signos"
        TACTILE_PAVING -> "Pavimento táctil"
        AUDIO_GUIDES -> "Audioguías"
        EASY_READING -> "Lectura fácil"
        PICTOGRAMS -> "Pictogramas"
        QUIET_SPACE -> "Espacio tranquilo"
        CLEAR_SIGNAGE -> "Señalización clara"
    }
}

object MockPlaces {
    fun getMockPlaces(): List<Place> = listOf(
        Place(
            id = "1",
            name = "Café Central",
            address = "Av. Principal 123",
            latitude = 40.4168,
            longitude = -3.7038,
            rating = 4.8f,
            category = PlaceCategory.CAFE,
            physicalAccessibility = AccessibilityScore.fromScore(4.5),
            sensoryAccessibility = AccessibilityScore.fromScore(3.5),
            cognitiveAccessibility = AccessibilityScore.fromScore(4.0),
            features = listOf(
                AccessibilityFeature.WHEELCHAIR_ACCESS,
                AccessibilityFeature.RAMP,
                AccessibilityFeature.ACCESSIBLE_BATHROOM,
                AccessibilityFeature.CLEAR_SIGNAGE
            )
        ),
        Place(
            id = "2",
            name = "Restaurante La Plaza",
            address = "Calle Mayor 45",
            latitude = 40.4178,
            longitude = -3.7048,
            rating = 4.5f,
            category = PlaceCategory.RESTAURANT,
            physicalAccessibility = AccessibilityScore.fromScore(3.2),
            sensoryAccessibility = null,
            cognitiveAccessibility = AccessibilityScore.fromScore(3.8),
            features = listOf(
                AccessibilityFeature.WHEELCHAIR_ACCESS,
                AccessibilityFeature.WIDE_DOORS
            )
        ),
        Place(
            id = "3",
            name = "Museo de Arte",
            address = "Paseo del Arte 78",
            latitude = 40.4158,
            longitude = -3.7028,
            rating = 4.9f,
            category = PlaceCategory.MUSEUM,
            physicalAccessibility = AccessibilityScore.fromScore(5.0),
            sensoryAccessibility = AccessibilityScore.fromScore(4.8),
            cognitiveAccessibility = AccessibilityScore.fromScore(4.2),
            features = listOf(
                AccessibilityFeature.WHEELCHAIR_ACCESS,
                AccessibilityFeature.ELEVATOR,
                AccessibilityFeature.ACCESSIBLE_BATHROOM,
                AccessibilityFeature.BRAILLE_SIGNAGE,
                AccessibilityFeature.AUDIO_GUIDES,
                AccessibilityFeature.PICTOGRAMS
            )
        ),
        Place(
            id = "4",
            name = "Tienda Gourmet",
            address = "Gran Vía 234",
            latitude = 40.4188,
            longitude = -3.7058,
            rating = 4.2f,
            category = PlaceCategory.SHOP,
            physicalAccessibility = AccessibilityScore.fromScore(2.5),
            sensoryAccessibility = AccessibilityScore.fromScore(3.0),
            cognitiveAccessibility = AccessibilityScore.fromScore(2.0),
            features = listOf(
                AccessibilityFeature.WIDE_DOORS
            )
        ),
        Place(
            id = "5",
            name = "Biblioteca Central",
            address = "Plaza de España 12",
            latitude = 40.4148,
            longitude = -3.7018,
            rating = 4.7f,
            category = PlaceCategory.LIBRARY,
            physicalAccessibility = AccessibilityScore.fromScore(4.8),
            sensoryAccessibility = AccessibilityScore.fromScore(4.5),
            cognitiveAccessibility = AccessibilityScore.fromScore(4.7),
            features = listOf(
                AccessibilityFeature.WHEELCHAIR_ACCESS,
                AccessibilityFeature.ELEVATOR,
                AccessibilityFeature.ACCESSIBLE_BATHROOM,
                AccessibilityFeature.HEARING_LOOP,
                AccessibilityFeature.BRAILLE_SIGNAGE,
                AccessibilityFeature.EASY_READING,
                AccessibilityFeature.QUIET_SPACE
            )
        ),
        Place(
            id = "6",
            name = "Centro Comercial Norte",
            address = "Calle Serrano 89",
            latitude = 40.4198,
            longitude = -3.7068,
            rating = 4.3f,
            category = PlaceCategory.MALL,
            physicalAccessibility = AccessibilityScore.fromScore(1.5),
            sensoryAccessibility = null,
            cognitiveAccessibility = AccessibilityScore.fromScore(2.2),
            features = listOf(
                AccessibilityFeature.ELEVATOR
            )
        ),
        Place(
            id = "7",
            name = "Teatro Principal",
            address = "Plaza del Teatro 5",
            latitude = 40.4138,
            longitude = -3.7008,
            rating = 4.6f,
            category = PlaceCategory.THEATER,
            physicalAccessibility = AccessibilityScore.fromScore(3.8),
            sensoryAccessibility = AccessibilityScore.fromScore(4.5),
            cognitiveAccessibility = null,
            features = listOf(
                AccessibilityFeature.WHEELCHAIR_ACCESS,
                AccessibilityFeature.ELEVATOR,
                AccessibilityFeature.HEARING_LOOP,
                AccessibilityFeature.SIGN_LANGUAGE
            )
        )
    )
}
