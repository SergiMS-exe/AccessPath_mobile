package org.s3m4su.accesspath.ui.place

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.s3m4su.accesspath.data.accessibility.UserProfile
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.ProfileApi
import org.s3m4su.accesspath.test.Fixtures
import org.s3m4su.accesspath.test.TestResets
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceDetailViewModelTest {

    @BeforeTest fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        TestResets.all()
    }
    @AfterTest fun teardown() {
        Dispatchers.resetMain()
        TestResets.all()
    }

    @Test
    fun `initial state has loading=true and no detail`() = runTest {
        val vm = PlaceDetailViewModel()
        assertEquals(true, vm.state.value.loading)
        assertNull(vm.state.value.detail)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `load success stores data and clears loading`() = runTest {
        val detail = Fixtures.placeDetail()
        PlaceApi.detailOverride = { Result.success(detail) }
        ProfileApi.getOverride = { Result.success(UserProfile()) }

        val vm = PlaceDetailViewModel()
        vm.load(1L)

        assertEquals(false, vm.state.value.loading)
        assertEquals(detail, vm.state.value.detail)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `load failure stores generic error message and keeps loading=false`() = runTest {
        PlaceApi.detailOverride = { Result.failure(RuntimeException("red caida")) }
        ProfileApi.getOverride = { Result.success(UserProfile()) }

        val vm = PlaceDetailViewModel()
        vm.load(1L)

        assertEquals(false, vm.state.value.loading)
        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.detail)
    }

    @Test
    fun `load with profile failure still loads detail`() = runTest {
        val detail = Fixtures.placeDetail()
        PlaceApi.detailOverride = { Result.success(detail) }
        ProfileApi.getOverride = { Result.failure(RuntimeException("sin perfil")) }

        val vm = PlaceDetailViewModel()
        vm.load(1L)

        assertEquals(detail, vm.state.value.detail)
        assertEquals(emptySet(), vm.state.value.userNeeds)
    }

    @Test
    fun `load with profile populates userNeeds`() = runTest {
        PlaceApi.detailOverride = { Result.success(Fixtures.placeDetail()) }
        ProfileApi.getOverride = { Result.success(UserProfile(needs = listOf("silla", "ceguera"), hasConsent = true)) }

        val vm = PlaceDetailViewModel()
        vm.load(1L)

        assertEquals(setOf("silla", "ceguera"), vm.state.value.userNeeds)
    }

    @Test
    fun `load triggers loading state at the start`() = runTest {
        PlaceApi.detailOverride = { Result.success(Fixtures.placeDetail()) }
        ProfileApi.getOverride = { Result.success(UserProfile()) }
        val vm = PlaceDetailViewModel()
        vm.load(1L)
        assertEquals(false, vm.state.value.loading)
    }

    @Test
    fun `second load overwrites previous state`() = runTest {
        val first = Fixtures.placeDetail(id = 1, name = "Cafe A")
        val second = Fixtures.placeDetail(id = 2, name = "Cafe B")
        var callCount = 0
        PlaceApi.detailOverride = {
            callCount++
            Result.success(if (callCount == 1) first else second)
        }
        ProfileApi.getOverride = { Result.success(UserProfile()) }

        val vm = PlaceDetailViewModel()
        vm.load(1L)
        vm.load(2L)
        assertEquals(second, vm.state.value.detail)
        assertEquals(2, callCount)
    }
}