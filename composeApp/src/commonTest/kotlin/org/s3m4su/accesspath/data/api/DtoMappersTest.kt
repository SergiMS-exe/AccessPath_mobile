package org.s3m4su.accesspath.data.api

import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.accessibility.Confidence
import org.s3m4su.accesspath.data.accessibility.CriterionScore
import org.s3m4su.accesspath.data.accessibility.DimensionScore
import org.s3m4su.accesspath.data.accessibility.PlaceAccessibility
import org.s3m4su.accesspath.data.accessibility.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DtoMappersTest {

    @Test
    fun `ConfidenceDto toDomain keeps all fields`() {
        val dto = ConfidenceDto(
            level = "high",
            nDefined = 12,
            nUnsure = 3,
            photoRatio = 0.5,
            lastContributionAt = "2026-09-15T10:00:00Z"
        )
        val d = dto.toDomain()
        assertEquals(Confidence("high", 12, 3, 0.5, "2026-09-15T10:00:00Z"), d)
    }

    @Test
    fun `CriterionScoreDto toDomain parses state string and tag list`() {
        val dto = CriterionScoreDto(
            criterionId = 1,
            key = "rampa",
            prompt = "?Tiene rampa?",
            isBlocking = true,
            profileTags = listOf("silla"),
            state = "yellow",
            conflict = true,
            nYes = 2, nNo = 1, nUnsure = 1,
            qualityP50 = 4.5,
            confidence = ConfidenceDto(level = "medium", nDefined = 4, nUnsure = 1)
        )
        val d = dto.toDomain()
        assertEquals(1L, d.criterionId)
        assertEquals(AccessibilityState.YELLOW, d.state)
        assertEquals(true, d.isBlocking)
        assertEquals(listOf("silla"), d.profileTags)
        assertEquals(true, d.conflict)
        assertEquals(4.5, d.qualityP50)
        assertEquals("medium", d.confidence.level)
    }

    @Test
    fun `DimensionScoreDto toDomain maps nested criteria`() {
        val dto = DimensionScoreDto(
            dimensionId = 1,
            key = "acceso",
            name = "Acceso",
            state = "green",
            conflict = false,
            criteria = listOf(
                CriterionScoreDto(criterionId = 11, key = "a", prompt = "p", state = "green")
            )
        )
        val d = dto.toDomain()
        assertEquals(1L, d.dimensionId)
        assertEquals(AccessibilityState.GREEN, d.state)
        assertEquals(1, d.criteria.size)
        assertEquals(CriterionScore::class, d.criteria[0]::class)
    }

    @Test
    fun `PlaceAccessibilityDto toDomain maps overall and dimensions`() {
        val dto = PlaceAccessibilityDto(
            dimensions = emptyList(),
            overallState = "red"
        )
        val d = dto.toDomain()
        assertEquals(emptyList(), d.dimensions)
        assertEquals(AccessibilityState.RED, d.overallState)
    }

    @Test
    fun `unknown state string maps to NO_DATA`() {
        val dto = PlaceAccessibilityDto(overallState = "???")
        assertEquals(AccessibilityState.NO_DATA, dto.toDomain().overallState)
    }

    @Test
    fun `PlaceDto toDomain fills defaults for missing fields`() {
        val dto = PlaceDto(
            id = 42, code = "X", name = "Sitio", address = null,
            latitude = 0.0, longitude = 0.0, description = null,
            createdBy = 1,
            createdAt = "2026-01-01", updatedAt = "2026-01-01"
        )
        val d = dto.toDomain()
        assertEquals("42", d.id)
        assertEquals("Sitio", d.name)
        assertEquals("", d.address)
        assertEquals(org.s3m4su.accesspath.data.PlaceCategory.OTHER, d.category)
    }

    @Test
    fun `PlaceMapItemDto toDomain uses overall state from dto`() {
        val dto = PlaceMapItemDto(
            id = 1, name = "X", latitude = 0.0, longitude = 0.0,
            overallState = "yellow"
        )
        val d = dto.toDomain()
        assertEquals(AccessibilityState.YELLOW, d.overallState)
    }

    @Test
    fun `PlaceDetailDto toDomain concatenates submissions`() {
        val dto = PlaceDetailDto(
            id = 1, name = "X", latitude = 0.0, longitude = 0.0,
            submissions = listOf(
                SubmissionDto(id = 1, comment = "uno"),
                SubmissionDto(id = 2, comment = "dos")
            )
        )
        val d = dto.toDomain()
        assertEquals(2, d.submissions.size)
        assertEquals("uno", d.submissions[0].comment)
    }

    @Test
    fun `SubmissionDto toDomain falls back to Anonimo when username is null`() {
        val dto = SubmissionDto(id = 1, comment = "x", username = null)
        val d = dto.toDomain()
        assertEquals("Anonimo", d.authorName)
    }

    @Test
    fun `PhotoDto toDomain preserves all fields`() {
        val dto = PhotoDto(id = 1, url = "u", contributionId = 99, suggestedSlot = "rampa")
        val d = dto.toDomain()
        assertEquals(1L, d.id)
        assertEquals("u", d.url)
        assertEquals(99L, d.contributionId)
        assertEquals("rampa", d.suggestedSlot)
    }

    @Test
    fun `ProfileDto toDomain maps all fields`() {
        val dto = ProfileDto(
            needs = listOf("silla"),
            consentAt = "2026-09-01",
            hasConsent = true
        )
        val d = dto.toDomain()
        assertEquals(UserProfile(needs = listOf("silla"), hasConsent = true, consentAt = "2026-09-01"), d)
    }

    @Test
    fun `CriterionDto toDomain preserves all fields`() {
        val dto = CriterionDto(
            id = 1, dimensionId = 2, key = "k", prompt = "p",
            isBlocking = true, isStarter = false,
            options = listOf(AnswerOptionDto(id = 10, label = "L"))
        )
        val d = dto.toDomain()
        assertEquals(1L, d.id)
        assertEquals(2L, d.dimensionId)
        assertEquals("k", d.key)
        assertEquals(true, d.isBlocking)
        assertEquals(false, d.isStarter)
    }

    @Test
    fun `NextQuestionDto toDomain returns empty NextQuestion when criterion is null`() {
        val dto = NextQuestionDto(criterion = null, options = emptyList())
        val d = dto.toDomain()
        assertNull(d.criterion)
        assertEquals(emptyList(), d.options)
    }

    @Test
    fun `DimensionDto toDomain nests criteria`() {
        val dto = DimensionDto(
            id = 1, key = "k", name = "N",
            criteria = listOf(CriterionDto(id = 11, dimensionId = 1, key = "c", prompt = "p"))
        )
        val d = dto.toDomain()
        assertEquals(1, d.criteria.size)
    }

    @Test
    fun `ContributionResultDto toDomain maps nested criterion and dimension`() {
        val dto = ContributionResultDto(
            contributionId = 1,
            criterion = CriterionScoreDto(criterionId = 11, key = "c", prompt = "p", state = "green"),
            dimension = DimensionScoreDto(dimensionId = 1, key = "d", name = "N", state = "green")
        )
        val d = dto.toDomain()
        assertEquals(1L, d.contributionId)
        assertEquals(AccessibilityState.GREEN, d.criterion.state)
        assertEquals(AccessibilityState.GREEN, d.dimension.state)
    }

    @Test
    fun `DimensionScoreDto toDomain keeps explicit nContributions from backend`() {
        val dto = DimensionScoreDto(
            dimensionId = 1, key = "k", name = "N", state = "green",
            nContributions = 12
        )
        val d = dto.toDomain()
        assertEquals(12, d.nContributions)
    }

    @Test
    fun `DimensionScoreDto toDomain derives nContributions from criteria when backend omits it`() {
        val dto = DimensionScoreDto(
            dimensionId = 1, key = "k", name = "N", state = "green",
            criteria = listOf(
                CriterionScoreDto(
                    criterionId = 11, key = "a", prompt = "p", state = "green",
                    confidence = ConfidenceDto(level = "medium", nDefined = 3, nUnsure = 1)
                ),
                CriterionScoreDto(
                    criterionId = 12, key = "b", prompt = "p", state = "green",
                    confidence = ConfidenceDto(level = "high", nDefined = 5, nUnsure = 0)
                )
            )
        )
        val d = dto.toDomain()
        assertEquals(9, d.nContributions)
    }

    @Test
    fun `PlaceDetailDto toDomain exposes hasMySubmission flag from backend`() {
        val dtoTrue = PlaceDetailDto(
            id = 1, name = "X", latitude = 0.0, longitude = 0.0,
            hasMySubmission = true
        )
        val dtoFalse = PlaceDetailDto(
            id = 2, name = "Y", latitude = 0.0, longitude = 0.0,
            hasMySubmission = false
        )
        assertEquals(true, dtoTrue.toDomain().hasMySubmission)
        assertEquals(false, dtoFalse.toDomain().hasMySubmission)
    }
}