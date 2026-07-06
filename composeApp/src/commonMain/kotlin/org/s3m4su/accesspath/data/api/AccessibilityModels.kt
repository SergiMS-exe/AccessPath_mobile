package org.s3m4su.accesspath.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.accessibility.AnswerOption
import org.s3m4su.accesspath.data.accessibility.Confidence
import org.s3m4su.accesspath.data.accessibility.Criterion
import org.s3m4su.accesspath.data.accessibility.CriterionScore
import org.s3m4su.accesspath.data.accessibility.DimensionCatalog
import org.s3m4su.accesspath.data.accessibility.DimensionScore
import org.s3m4su.accesspath.data.accessibility.NextQuestion
import org.s3m4su.accesspath.data.accessibility.PlaceAccessibility
import org.s3m4su.accesspath.data.accessibility.PlaceDetail
import org.s3m4su.accesspath.data.accessibility.PlaceSubmission
import org.s3m4su.accesspath.data.accessibility.SubmissionPhoto

// =============================================================================
// Semaforo / agregacion
// =============================================================================

@Serializable
data class ConfidenceDto(
    val level: String = "none",
    @SerialName("n_defined") val nDefined: Int = 0,
    @SerialName("n_unsure") val nUnsure: Int = 0,
    @SerialName("photo_ratio") val photoRatio: Double = 0.0,
    // Informativo ("ultima valoracion: hace X"); nunca penaliza ni decae el estado.
    @SerialName("last_contribution_at") val lastContributionAt: String? = null
)

@Serializable
data class CriterionScoreDto(
    @SerialName("criterion_id") val criterionId: Long,
    val key: String,
    val prompt: String,
    @SerialName("is_blocking") val isBlocking: Boolean = false,
    @SerialName("profile_tags") val profileTags: List<String> = emptyList(),
    val state: String = "no_data",
    val conflict: Boolean = false,
    @SerialName("n_yes") val nYes: Int = 0,
    @SerialName("n_no") val nNo: Int = 0,
    @SerialName("n_unsure") val nUnsure: Int = 0,
    @SerialName("quality_p50") val qualityP50: Double? = null,
    val confidence: ConfidenceDto = ConfidenceDto()
)

@Serializable
data class DimensionScoreDto(
    @SerialName("dimension_id") val dimensionId: Long,
    val key: String,
    val name: String,
    val state: String = "no_data",
    val conflict: Boolean = false,
    val criteria: List<CriterionScoreDto> = emptyList()
)

@Serializable
data class PlaceAccessibilityDto(
    val dimensions: List<DimensionScoreDto> = emptyList(),
    @SerialName("overall_state") val overallState: String = "no_data"
)

fun ConfidenceDto.toDomain() = Confidence(level, nDefined, nUnsure, photoRatio, lastContributionAt)

fun CriterionScoreDto.toDomain() = CriterionScore(
    criterionId = criterionId,
    key = key,
    prompt = prompt,
    isBlocking = isBlocking,
    profileTags = profileTags,
    state = AccessibilityState.fromApi(state),
    conflict = conflict,
    nYes = nYes,
    nNo = nNo,
    nUnsure = nUnsure,
    qualityP50 = qualityP50,
    confidence = confidence.toDomain()
)

fun DimensionScoreDto.toDomain() = DimensionScore(
    dimensionId = dimensionId,
    key = key,
    name = name,
    state = AccessibilityState.fromApi(state),
    conflict = conflict,
    criteria = criteria.map { it.toDomain() }
)

fun PlaceAccessibilityDto.toDomain() = PlaceAccessibility(
    dimensions = dimensions.map { it.toDomain() },
    overallState = AccessibilityState.fromApi(overallState)
)

// =============================================================================
// Catalogo del formulario (GET /dimensions) y next-question
// =============================================================================

@Serializable
data class AnswerOptionDto(
    val id: Long,
    @SerialName("criterion_id") val criterionId: Long = 0,
    val label: String,
    @SerialName("exists_value") val existsValue: Boolean? = null,
    @SerialName("quality_value") val qualityValue: Int? = null,
    @SerialName("is_unsure") val isUnsure: Boolean = false
)

@Serializable
data class CriterionDto(
    val id: Long,
    @SerialName("dimension_id") val dimensionId: Long = 0,
    val key: String,
    val prompt: String,
    @SerialName("is_blocking") val isBlocking: Boolean = false,
    @SerialName("is_starter") val isStarter: Boolean = false,
    val options: List<AnswerOptionDto> = emptyList()
)

@Serializable
data class DimensionDto(
    val id: Long,
    val key: String,
    val name: String,
    val description: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    val criteria: List<CriterionDto> = emptyList()
)

@Serializable
data class NextQuestionDto(
    val criterion: CriterionDto? = null,
    val options: List<AnswerOptionDto> = emptyList()
)

fun AnswerOptionDto.toDomain() = AnswerOption(id, criterionId, label, existsValue, qualityValue, isUnsure)

fun CriterionDto.toDomain() = Criterion(id, dimensionId, key, prompt, isBlocking, isStarter)

fun DimensionDto.toDomain() = DimensionCatalog(
    id = id,
    key = key,
    name = name,
    description = description,
    criteria = criteria.map { it.toDomain() }
)

fun NextQuestionDto.toDomain() = NextQuestion(
    criterion = criterion?.toDomain(),
    options = options.map { it.toDomain() }
)

// =============================================================================
// Contribuciones
// =============================================================================

@Serializable
data class ContributionRequestDto(
    @SerialName("place_id") val placeId: Long,
    @SerialName("criterion_id") val criterionId: Long,
    @SerialName("answer_option_id") val answerOptionId: Long
)

@Serializable
data class ContributionResultDto(
    @SerialName("contribution_id") val contributionId: Long? = null,
    val criterion: CriterionScoreDto,
    val dimension: DimensionScoreDto
)

fun ContributionResultDto.toDomain() = org.s3m4su.accesspath.data.accessibility.ContributionResult(
    contributionId = contributionId,
    criterion = criterion.toDomain(),
    dimension = dimension.toDomain()
)

// =============================================================================
// Submissions y fotos
// =============================================================================

@Serializable
data class PhotoDto(
    val id: Long,
    val url: String,
    @SerialName("contribution_id") val contributionId: Long? = null,
    @SerialName("suggested_slot") val suggestedSlot: String? = null
)

@Serializable
data class SubmissionDto(
    val id: Long,
    val comment: String? = null,
    val username: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val photos: List<PhotoDto> = emptyList()
)

@Serializable
data class PhotoInputDto(
    val data: String,
    @SerialName("contribution_id") val contributionId: Long? = null,
    @SerialName("suggested_slot") val suggestedSlot: String? = null
)

@Serializable
data class SubmissionRequestDto(
    @SerialName("place_id") val placeId: Long,
    val comment: String? = null,
    val photos: List<PhotoInputDto>? = null
)

fun PhotoDto.toDomain() = SubmissionPhoto(id, url, contributionId, suggestedSlot)

fun SubmissionDto.toDomain() = PlaceSubmission(
    id = id,
    authorName = username ?: "Anonimo",
    comment = comment,
    createdAt = createdAt,
    photos = photos.map { it.toDomain() }
)

// =============================================================================
// Detalle de lugar y pin de mapa (place fields aplanados por el backend)
// =============================================================================

@Serializable
data class PlaceDetailDto(
    val id: Long,
    val name: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val accessibility: PlaceAccessibilityDto = PlaceAccessibilityDto(),
    val submissions: List<SubmissionDto> = emptyList()
)

@Serializable
data class PlaceMapItemDto(
    val id: Long,
    val name: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val dimensions: List<DimensionScoreDto> = emptyList(),
    @SerialName("overall_state") val overallState: String = "no_data"
)

fun PlaceDetailDto.toDomain() = PlaceDetail(
    id = id,
    name = name,
    address = address ?: "",
    latitude = latitude,
    longitude = longitude,
    description = description,
    accessibility = accessibility.toDomain(),
    submissions = submissions.map { it.toDomain() }
)

fun PlaceMapItemDto.toDomain() = org.s3m4su.accesspath.data.Place(
    id = id.toString(),
    name = name,
    address = address ?: "",
    latitude = latitude,
    longitude = longitude,
    description = description,
    dimensions = dimensions.map { it.toDomain() },
    overallState = AccessibilityState.fromApi(overallState)
)

// =============================================================================
// Perfil funcional
// =============================================================================

@Serializable
data class ProfileDto(
    val needs: List<String> = emptyList(),
    @SerialName("consent_at") val consentAt: String? = null,
    @SerialName("has_consent") val hasConsent: Boolean = false
)

@Serializable
data class ProfileRequestDto(
    val needs: List<String>,
    val consent: Boolean
)

fun ProfileDto.toDomain() = org.s3m4su.accesspath.data.accessibility.UserProfile(
    needs = needs,
    hasConsent = hasConsent,
    consentAt = consentAt
)
