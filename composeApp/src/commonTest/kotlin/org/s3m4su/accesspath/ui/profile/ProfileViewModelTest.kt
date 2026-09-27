package org.s3m4su.accesspath.ui.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.s3m4su.accesspath.data.accessibility.UserProfile
import org.s3m4su.accesspath.data.api.ProfileApi
import org.s3m4su.accesspath.test.TestResets
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @BeforeTest fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        TestResets.all()
    }
    @AfterTest fun teardown() {
        Dispatchers.resetMain()
        TestResets.all()
    }

    @Test
    fun `init loads profile from backend`() = runTest {
        ProfileApi.getOverride = {
            Result.success(UserProfile(needs = listOf("silla"), hasConsent = true))
        }
        val vm = ProfileViewModel()
        assertEquals(false, vm.state.value.loading)
        assertEquals(setOf("silla"), vm.state.value.selectedNeeds)
        assertEquals(true, vm.state.value.hasConsent)
    }

    @Test
    fun `init failure stores error and falls back to empty profile`() = runTest {
        ProfileApi.getOverride = { Result.failure(RuntimeException("offline")) }
        val vm = ProfileViewModel()
        assertEquals(false, vm.state.value.loading)
        assertEquals(emptySet(), vm.state.value.selectedNeeds)
        assertEquals(false, vm.state.value.hasConsent)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `toggleNeed adds and removes from selectedNeeds`() = runTest {
        ProfileApi.getOverride = { Result.success(UserProfile()) }
        val vm = ProfileViewModel()
        vm.toggleNeed("silla")
        assertTrue(vm.state.value.selectedNeeds.contains("silla"))
        vm.toggleNeed("silla")
        assertFalse(vm.state.value.selectedNeeds.contains("silla"))
    }

    @Test
    fun `toggleNeed clears saved flag`() = runTest {
        ProfileApi.getOverride = { Result.success(UserProfile()) }
        ProfileApi.setOverride = { Result.success(UserProfile(needs = listOf("silla"), hasConsent = true)) }
        val vm = ProfileViewModel()
        vm.toggleNeed("silla")
        vm.save()
        assertEquals(true, vm.state.value.saved)
        vm.toggleNeed("silla")
        assertEquals(false, vm.state.value.saved)
    }

    @Test
    fun `save sends needs with consent=true`() = runTest {
        ProfileApi.getOverride = { Result.success(UserProfile()) }
        var capturedNeeds: List<String>? = null
        var capturedConsent: Boolean? = null
        ProfileApi.setOverride = { req ->
            capturedNeeds = req.needs
            capturedConsent = req.consent
            Result.success(UserProfile(needs = req.needs, hasConsent = true))
        }

        val vm = ProfileViewModel()
        vm.toggleNeed("silla")
        vm.toggleNeed("ceguera")
        vm.save()

        assertEquals(listOf("silla", "ceguera"), capturedNeeds)
        assertEquals(true, capturedConsent)
        assertEquals(true, vm.state.value.saved)
        assertEquals(true, vm.state.value.hasConsent)
    }

    @Test
    fun `save failure stores error and does not mark as saved`() = runTest {
        ProfileApi.getOverride = { Result.success(UserProfile()) }
        ProfileApi.setOverride = { Result.failure(RuntimeException("red caida")) }
        val vm = ProfileViewModel()
        vm.toggleNeed("silla")
        vm.save()
        assertEquals(false, vm.state.value.saved)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `delete clears profile and consent`() = runTest {
        ProfileApi.getOverride = {
            Result.success(UserProfile(needs = listOf("silla"), hasConsent = true))
        }
        ProfileApi.deleteOverride = { Result.success(Unit) }

        val vm = ProfileViewModel()
        assertEquals(true, vm.state.value.hasConsent)

        vm.delete()
        assertEquals(emptySet(), vm.state.value.selectedNeeds)
        assertEquals(false, vm.state.value.hasConsent)
    }

    @Test
    fun `delete failure stores error`() = runTest {
        ProfileApi.getOverride = { Result.success(UserProfile(hasConsent = true)) }
        ProfileApi.deleteOverride = { Result.failure(RuntimeException("no se pudo")) }
        val vm = ProfileViewModel()
        vm.delete()
        assertNotNull(vm.state.value.error)
    }
}