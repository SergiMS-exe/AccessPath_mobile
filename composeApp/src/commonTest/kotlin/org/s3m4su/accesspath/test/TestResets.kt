package org.s3m4su.accesspath.test

import org.s3m4su.accesspath.data.api.ContributionApi
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.ProfileApi
import org.s3m4su.accesspath.data.api.SubmissionApi
import org.s3m4su.accesspath.data.auth.AuthRepository

object TestResets {

    fun all() {
        AuthRepository.reset(FakeSettings())
        PlaceApi.searchOverride = null
        PlaceApi.mapPinsOverride = null
        PlaceApi.detailOverride = null
        PlaceApi.importFromGoogleOverride = null
        ContributionApi.nextQuestionOverride = null
        ContributionApi.saveOverride = null
        ContributionApi.deleteOverride = null
        SubmissionApi.saveOverride = null
        ProfileApi.getOverride = null
        ProfileApi.setOverride = null
        ProfileApi.deleteOverride = null
    }
}