package org.s3m4su.accesspath

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.test.FakeSettings
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @BeforeTest
    fun setup() {
        AuthRepository.reset(FakeSettings())
    }

    @AfterTest
    fun teardown() {
        AuthRepository.reset(FakeSettings())
    }

    @Test
    fun `app launches without crash and shows branding`() {
        composeRule.onNodeWithText("AccessPath").assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar sesion").assertIsDisplayed()
    }
}