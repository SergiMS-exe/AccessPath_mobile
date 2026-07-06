package org.s3m4su.accesspath.data.accessibility

/**
 * Semaforo de accesibilidad de 4 estados. NO_DATA (gris) es "sin datos",
 * distinto de RED. Nunca se colapsa a una media o numero global.
 * Reemplaza al antiguo AccessibilityLevel de 4 niveles por score.
 */
enum class AccessibilityState {
    GREEN, YELLOW, RED, NO_DATA;

    companion object {
        fun fromApi(value: String?): AccessibilityState = when (value) {
            "green" -> GREEN
            "yellow" -> YELLOW
            "red" -> RED
            else -> NO_DATA
        }
    }

    /** Etiqueta de texto (no depender solo del color, requisito de accesibilidad). */
    val label: String
        get() = when (this) {
            GREEN -> "Accesible"
            YELLOW -> "Con salvedades"
            RED -> "No accesible"
            NO_DATA -> "Sin datos"
        }
}

/**
 * Eje de confianza, SEPARADO del estado. No altera el color.
 * lastContributionAt es informativo ("ultima valoracion: hace X"): la recencia
 * se muestra para que el usuario juzgue, nunca penaliza ni decae el estado.
 */
data class Confidence(
    val level: String = "none", // none | low | medium | high
    val nDefined: Int = 0,
    val nUnsure: Int = 0,
    val photoRatio: Double = 0.0,
    val lastContributionAt: String? = null
)

/** Estado derivado de un criterio en un lugar. profileTags casa con need_key del perfil. */
data class CriterionScore(
    val criterionId: Long,
    val key: String,
    val prompt: String,
    val isBlocking: Boolean,
    val profileTags: List<String> = emptyList(),
    val state: AccessibilityState,
    val conflict: Boolean,
    val nYes: Int,
    val nNo: Int,
    val nUnsure: Int,
    val qualityP50: Double?,
    val confidence: Confidence
)

/** Rollup (NO media) de una dimension y su desglose criterio a criterio. */
data class DimensionScore(
    val dimensionId: Long,
    val key: String,
    val name: String,
    val state: AccessibilityState,
    val conflict: Boolean,
    val criteria: List<CriterionScore> = emptyList()
)

/** Bloque de accesibilidad del detalle de un lugar. */
data class PlaceAccessibility(
    val dimensions: List<DimensionScore> = emptyList(),
    val overallState: AccessibilityState = AccessibilityState.NO_DATA
)

/** Opcion de respuesta de un criterio (hecho + calidad codificados). */
data class AnswerOption(
    val id: Long,
    val criterionId: Long,
    val label: String,
    val existsValue: Boolean?,
    val qualityValue: Int?,
    val isUnsure: Boolean
)

/** Criterio del formulario (una pregunta). */
data class Criterion(
    val id: Long,
    val dimensionId: Long,
    val key: String,
    val prompt: String,
    val isBlocking: Boolean,
    val isStarter: Boolean
)

/** Pregunta elegida por el backend; criterion == null significa "nada util". */
data class NextQuestion(
    val criterion: Criterion?,
    val options: List<AnswerOption>
)

/** Resultado de una contribucion: semaforo en vivo del criterio y su dimension. */
data class ContributionResult(
    val contributionId: Long?,
    val criterion: CriterionScore,
    val dimension: DimensionScore
)

/** Foto asociada a una submission (evidencia opcional de un hecho). */
data class SubmissionPhoto(
    val id: Long,
    val url: String,
    val contributionId: Long?,
    val suggestedSlot: String?
)

/** Comentario + fotos de un usuario sobre un lugar ("que cuenta la gente"). */
data class PlaceSubmission(
    val id: Long,
    val authorName: String,
    val comment: String?,
    val createdAt: String?,
    val photos: List<SubmissionPhoto>
)

/** Detalle completo de un lugar (consumo de GET /places/:id). */
data class PlaceDetail(
    val id: Long,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val accessibility: PlaceAccessibility,
    val submissions: List<PlaceSubmission>
)

/** Una dimension del catalogo con sus criterios (GET /dimensions). */
data class DimensionCatalog(
    val id: Long,
    val key: String,
    val name: String,
    val description: String?,
    val criteria: List<Criterion>
)

/** Perfil funcional del usuario (opt-in, no diagnostico). */
data class UserProfile(
    val needs: List<String> = emptyList(),
    val hasConsent: Boolean = false,
    val consentAt: String? = null
)

/** Necesidades funcionales disponibles. Casan con criterion.profile_tags. */
enum class FunctionalNeed(val key: String, val label: String) {
    SILLA("silla", "Uso silla de ruedas"),
    MOVILIDAD_REDUCIDA("movilidad_reducida", "Movilidad reducida"),
    BAJA_VISION("baja_vision", "Baja vision"),
    CEGUERA("ceguera", "Ceguera"),
    AUDITIVA("auditiva", "Discapacidad auditiva"),
    COGNITIVA("cognitiva", "Necesidades cognitivas"),
    SENSORIAL("sensorial", "Sensibilidad sensorial");

    companion object {
        fun fromKey(key: String): FunctionalNeed? = entries.firstOrNull { it.key == key }
    }
}
