package org.s3m4su.accesspath.test

import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
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
import org.s3m4su.accesspath.data.auth.AuthUser

object Fixtures {

    val testUser = AuthUser(
        id = 1L,
        code = "TEST001",
        username = "ana",
        email = "ana@example.com",
        createdAt = "2026-01-01T00:00:00Z"
    )

    fun place(
        id: String = "1",
        name: String = "Cafe Central",
        address: String = "Av. Principal 123, Madrid",
        latitude: Double = 40.4168,
        longitude: Double = -3.7038,
        category: PlaceCategory = PlaceCategory.CAFE,
        overallState: AccessibilityState = AccessibilityState.YELLOW,
        dimensions: List<DimensionScore> = emptyList(),
        description: String? = null
    ) = Place(
        id = id, name = name, address = address,
        latitude = latitude, longitude = longitude,
        category = category, description = description,
        overallState = overallState, dimensions = dimensions
    )

    fun dimension(
        id: Long = 1,
        key: String = "acceso",
        name: String = "Llegada y acceso",
        state: AccessibilityState = AccessibilityState.GREEN,
        conflict: Boolean = false,
        criteria: List<CriterionScore> = emptyList(),
        nContributions: Int = 0
    ) = DimensionScore(
        dimensionId = id, key = key, name = name,
        state = state, conflict = conflict, criteria = criteria,
        nContributions = nContributions
    )

    fun criterionScore(
        id: Long = 1,
        key: String = "rampa",
        prompt: String = "?Tiene rampa de acceso?",
        state: AccessibilityState = AccessibilityState.GREEN,
        nYes: Int = 3, nNo: Int = 0, nUnsure: Int = 0,
        profileTags: List<String> = emptyList(),
        isBlocking: Boolean = true,
        conflict: Boolean = false,
        confidence: Confidence = Confidence(level = "medium", nDefined = 3, nUnsure = 0)
    ) = CriterionScore(
        criterionId = id, key = key, prompt = prompt,
        isBlocking = isBlocking, profileTags = profileTags,
        state = state, conflict = conflict,
        nYes = nYes, nNo = nNo, nUnsure = nUnsure,
        qualityP50 = 4.0, confidence = confidence
    )

    fun confidence(
        level: String = "medium",
        nDefined: Int = 3,
        nUnsure: Int = 0,
        photoRatio: Double = 0.0,
        lastContributionAt: String? = "2026-09-01T10:00:00Z"
    ) = Confidence(level, nDefined, nUnsure, photoRatio, lastContributionAt)

    fun criterion(
        id: Long = 1,
        dimensionId: Long = 1,
        key: String = "rampa",
        prompt: String = "?Tiene rampa de acceso?",
        isBlocking: Boolean = true,
        isStarter: Boolean = true
    ) = Criterion(id, dimensionId, key, prompt, isBlocking, isStarter)

    fun answerOption(
        id: Long = 100,
        criterionId: Long = 1,
        label: String = "Si",
        existsValue: Boolean? = true,
        qualityValue: Int? = 5,
        isUnsure: Boolean = false
    ) = AnswerOption(id, criterionId, label, existsValue, qualityValue, isUnsure)

    fun nextQuestion(
        criterion: Criterion? = criterion(),
        options: List<AnswerOption> = listOf(
            answerOption(id = 101, label = "Si"),
            answerOption(id = 102, label = "No"),
            answerOption(id = 103, label = "No lo se", isUnsure = true)
        )
    ) = NextQuestion(criterion, options)

    fun placeDetail(
        id: Long = 1,
        name: String = "Cafe Central",
        address: String = "Av. Principal 123, Madrid",
        description: String? = "Cafe con rampa lateral",
        accessibility: PlaceAccessibility = PlaceAccessibility(
            dimensions = listOf(dimension()),
            overallState = AccessibilityState.GREEN
        ),
        submissions: List<PlaceSubmission> = emptyList()
    ) = PlaceDetail(
        id = id, name = name, address = address,
        latitude = 40.4168, longitude = -3.7038,
        description = description,
        accessibility = accessibility,
        submissions = submissions
    )

    fun submission(
        id: Long = 1,
        authorName: String = "ana",
        comment: String? = "Hay rampa lateral",
        createdAt: String? = "2026-09-15T10:00:00Z",
        photos: List<SubmissionPhoto> = emptyList()
    ) = PlaceSubmission(id, authorName, comment, createdAt, photos)

    fun dimensionCatalog(
        id: Long = 1,
        key: String = "acceso",
        name: String = "Llegada y acceso",
        description: String? = "Como se llega al sitio",
        criteria: List<Criterion> = listOf(criterion(id = id * 10))
    ) = DimensionCatalog(id, key, name, description, criteria)
}