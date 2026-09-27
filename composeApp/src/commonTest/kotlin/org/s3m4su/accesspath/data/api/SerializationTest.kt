package org.s3m4su.accesspath.data.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.auth.AuthUser
import org.s3m4su.accesspath.data.auth.LoginRequest
import org.s3m4su.accesspath.data.auth.LoginResponse
import org.s3m4su.accesspath.data.auth.RefreshResponse
import org.s3m4su.accesspath.data.auth.RegisterRequest
import org.s3m4su.accesspath.test.Fixtures
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `LoginRequest roundtrips`() {
        val r = LoginRequest(email = "a@b.com", password = "1234")
        val s = json.encodeToString(r)
        val back = json.decodeFromString<LoginRequest>(s)
        assertEquals(r, back)
    }

    @Test
    fun `RegisterRequest roundtrips`() {
        val r = RegisterRequest(username = "ana", email = "a@b.com", password = "1234")
        val back = json.decodeFromString<RegisterRequest>(json.encodeToString(r))
        assertEquals(r, back)
    }

    @Test
    fun `AuthUser maps snake_case created_at`() {
        val raw = """{"id":1,"code":"A","username":"ana","email":"a@b.com","created_at":"2026-01-01T00:00:00Z"}"""
        val u = json.decodeFromString<AuthUser>(raw)
        assertEquals("2026-01-01T00:00:00Z", u.createdAt)
    }

    @Test
    fun `LoginResponse maps snake_case refresh_token`() {
        val raw = """{"token":"t","refresh_token":"r","user":{"id":1,"code":"A","username":"u","email":"e","created_at":"x"}}"""
        val r = json.decodeFromString<LoginResponse>(raw)
        assertEquals("t", r.token)
        assertEquals("r", r.refreshToken)
    }

    @Test
    fun `RefreshResponse roundtrips`() {
        val r = RefreshResponse(token = "t", refreshToken = "r")
        val back = json.decodeFromString<RefreshResponse>(json.encodeToString(r))
        assertEquals(r, back)
    }

    @Test
    fun `ApiResponse with null data is preserved`() {
        val raw = """{"data":null,"error":"bad"}"""
        val r = json.decodeFromString<ApiResponse<String>>(raw)
        assertEquals(null, r.data)
        assertEquals("bad", r.error)
    }

    @Test
    fun `PlaceDto ignores unknown keys`() {
        val raw = """{"id":1,"code":"X","name":"N","latitude":0,"longitude":0,"created_by":1,"created_at":"x","updated_at":"x","unknown":"foo"}"""
        val d = json.decodeFromString<PlaceDto>(raw)
        assertEquals(1L, d.id)
    }

    @Test
    fun `PlaceMapItemDto default state is no_data`() {
        val raw = """{"id":1,"name":"N","latitude":0,"longitude":0}"""
        val d = json.decodeFromString<PlaceMapItemDto>(raw)
        assertEquals(AccessibilityState.NO_DATA, AccessibilityState.fromApi(d.overallState.lowercase()))
    }

    @Test
    fun `ConfidenceDto roundtrips with snake_case names`() {
        val raw = """{"level":"high","n_defined":5,"n_unsure":2,"photo_ratio":0.4,"last_contribution_at":"2026-01-01"}"""
        val d = json.decodeFromString<ConfidenceDto>(raw)
        assertEquals("high", d.level)
        assertEquals(5, d.nDefined)
        assertEquals(2, d.nUnsure)
        assertEquals(0.4, d.photoRatio)
        assertEquals("2026-01-01", d.lastContributionAt)
    }

    @Test
    fun `ContributionRequestDto maps snake_case fields`() {
        val raw = """{"place_id":1,"criterion_id":2,"answer_option_id":3}"""
        val d = json.decodeFromString<ContributionRequestDto>(raw)
        assertEquals(1L, d.placeId)
        assertEquals(2L, d.criterionId)
        assertEquals(3L, d.answerOptionId)
    }

    @Test
    fun `ProfileDto maps snake_case consent_at and has_consent`() {
        val raw = """{"needs":["silla"],"consent_at":"2026-01-01","has_consent":true}"""
        val d = json.decodeFromString<ProfileDto>(raw)
        assertEquals(listOf("silla"), d.needs)
        assertEquals("2026-01-01", d.consentAt)
        assertEquals(true, d.hasConsent)
    }

    @Test
    fun `PhotoInputDto maps snake_case fields`() {
        val raw = """{"data":"base64","contribution_id":1,"suggested_slot":"rampa"}"""
        val d = json.decodeFromString<PhotoInputDto>(raw)
        assertEquals("base64", d.data)
        assertEquals(1L, d.contributionId)
        assertEquals("rampa", d.suggestedSlot)
    }

    @Test
    fun `SubmissionRequestDto serializes comment and photos`() {
        val r = SubmissionRequestDto(
            placeId = 1,
            comment = "hola",
            photos = listOf(PhotoInputDto(data = "abc", contributionId = null, suggestedSlot = null))
        )
        val raw = json.encodeToString(r)
        assertEquals(true, raw.contains("\"place_id\":1"))
        assertEquals(true, raw.contains("\"comment\":\"hola\""))
        assertEquals(true, raw.contains("\"data\":\"abc\""))
    }

    @Test
    fun `unknown state strings coerce to NO_DATA via fromApi`() {
        val raw = """{"criterion_id":1,"key":"k","prompt":"p","state":"unknown","n_defined":1}"""
        val d = json.decodeFromString<CriterionScoreDto>(raw)
        assertEquals(AccessibilityState.NO_DATA, AccessibilityState.fromApi(d.state))
    }

    @Test
    fun `DimensionScoreDto maps snake_case n_contributions`() {
        val raw = """{"dimension_id":1,"key":"k","name":"N","state":"green","n_contributions":7}"""
        val d = json.decodeFromString<DimensionScoreDto>(raw)
        assertEquals(7, d.nContributions)
    }

    @Test
    fun `DimensionScoreDto defaults n_contributions to zero when omitted`() {
        val raw = """{"dimension_id":1,"key":"k","name":"N","state":"green"}"""
        val d = json.decodeFromString<DimensionScoreDto>(raw)
        assertEquals(0, d.nContributions)
    }

    @Test
    fun `PlaceDetailDto maps snake_case has_my_submission`() {
        val raw = """{"id":1,"name":"N","latitude":0,"longitude":0,"has_my_submission":true}"""
        val d = json.decodeFromString<PlaceDetailDto>(raw)
        assertEquals(true, d.hasMySubmission)
    }

    @Test
    fun `PlaceDetailDto defaults has_my_submission to false when omitted`() {
        val raw = """{"id":1,"name":"N","latitude":0,"longitude":0}"""
        val d = json.decodeFromString<PlaceDetailDto>(raw)
        assertEquals(false, d.hasMySubmission)
    }
}