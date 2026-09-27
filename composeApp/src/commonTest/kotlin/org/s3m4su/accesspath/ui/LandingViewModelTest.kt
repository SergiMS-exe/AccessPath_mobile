package org.s3m4su.accesspath.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.auth.AuthState
import org.s3m4su.accesspath.location.Location
import org.s3m4su.accesspath.location.LocationService
import org.s3m4su.accesspath.map.MapBounds
import org.s3m4su.accesspath.test.Fixtures
import org.s3m4su.accesspath.test.TestResets
import org.s3m4su.accesspath.ui.components.DrawerMenuItem
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LandingViewModelTest {

    @BeforeTest fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        TestResets.all()
    }
    @AfterTest fun teardown() {
        Dispatchers.resetMain()
        TestResets.all()
    }

    private fun fakeLocationService(
        enabled: Boolean = true,
        permission: Boolean = true,
        location: Location? = Location(40.0, -3.0)
    ) = object : LocationService() {
        override fun isLocationEnabled(): Boolean = enabled
        override fun hasLocationPermission(): Boolean = permission
        override suspend fun requestLocationPermission(): Boolean = permission
        override suspend fun getCurrentLocation(): Location? = location
    }

    @Test
    fun `initial state is empty`() = runTest {
        val vm = LandingViewModel()
        val s = vm.state.value
        assertTrue(s.places.isEmpty())
        assertNull(s.loadedBounds)
        assertNull(s.mapBounds)
        assertNull(s.mapCenter)
        assertEquals(false, s.loading)
        assertNull(s.error)
        assertEquals(PlaceFilter(), s.filter)
    }

    @Test
    fun `onCameraIdle updates mapBounds`() = runTest {
        val vm = LandingViewModel()
        val b = MapBounds(0.0, 1.0, 0.0, 1.0)
        vm.onCameraIdle(b)
        assertEquals(b, vm.state.value.mapBounds)
    }

    @Test
    fun `loadArea fetches places and updates state on success`() = runTest {
        val places = listOf(Fixtures.place(), Fixtures.place(id = "2", name = "Museo"))
        PlaceApi.mapPinsOverride = { _, _, _, _ -> Result.success(places) }
        val vm = LandingViewModel()
        val b = MapBounds(0.0, 1.0, 0.0, 1.0)

        vm.loadArea(b)

        assertEquals(false, vm.state.value.loading)
        assertEquals(places, vm.state.value.places)
        assertEquals(b, vm.state.value.loadedBounds)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `loadArea does not repeat call when same bounds`() = runTest {
        var calls = 0
        PlaceApi.mapPinsOverride = { _, _, _, _ ->
            calls++
            Result.success(emptyList())
        }
        val vm = LandingViewModel()
        val b = MapBounds(0.0, 1.0, 0.0, 1.0)
        vm.loadArea(b)
        vm.loadArea(b)
        assertEquals(1, calls)
    }

    @Test
    fun `loadArea stores error message on failure`() = runTest {
        PlaceApi.mapPinsOverride = { _, _, _, _ -> Result.failure(RuntimeException("red caida")) }
        val vm = LandingViewModel()
        vm.loadArea(MapBounds(0.0, 1.0, 0.0, 1.0))
        assertEquals(false, vm.state.value.loading)
        assertEquals("red caida", vm.state.value.error)
        assertTrue(vm.state.value.places.isEmpty())
    }

    @Test
    fun `bootstrap centers on selected place and skips location`() = runTest {
        var locationRequested = false
        val service = object : LocationService() {
            override fun isLocationEnabled() = true
            override fun hasLocationPermission() = true
            override suspend fun requestLocationPermission() = true
            override suspend fun getCurrentLocation(): Location? {
                locationRequested = true
                return Location(0.0, 0.0)
            }
        }
        val vm = LandingViewModel()
        val p = Fixtures.place(latitude = 41.0, longitude = 2.0)
        vm.bootstrap(p, service, null)
        assertEquals(Location(41.0, 2.0), vm.state.value.mapCenter)
        assertEquals(false, locationRequested)
    }

    @Test
    fun `bootstrap asks for location when no selected place`() = runTest {
        var locationRequested = false
        val service = object : LocationService() {
            override fun isLocationEnabled() = true
            override fun hasLocationPermission() = true
            override suspend fun requestLocationPermission() = true
            override suspend fun getCurrentLocation(): Location? {
                locationRequested = true
                return Location(40.5, -3.5)
            }
        }
        val vm = LandingViewModel()
        vm.bootstrap(null, service, null)
        assertEquals(true, locationRequested)
        assertEquals(Location(40.5, -3.5), vm.state.value.mapCenter)
    }

    @Test
    fun `refreshLocation uses fallback location when no permission granted`() = runTest {
        val service = object : LocationService() {
            override fun isLocationEnabled() = true
            override fun hasLocationPermission() = false
            override suspend fun requestLocationPermission() = false
            override suspend fun getCurrentLocation() = null
        }
        val vm = LandingViewModel()
        vm.refreshLocation(service, null)
        assertEquals(Location(40.4168, -3.7038), vm.state.value.mapCenter)
    }

    @Test
    fun `onMyLocationClick updates map center and zoom`() = runTest {
        val vm = LandingViewModel()
        val loc = Location(40.5, -3.5)
        vm.refreshLocation(fakeLocationService(location = loc), null)
        vm.onPlaceClicked(Fixtures.place(latitude = 41.0, longitude = 2.0))
        vm.onMyLocationClick()
        assertEquals(loc, vm.state.value.mapCenter)
        assertEquals(16f, vm.state.value.zoom)
    }

    @Test
    fun `onMyLocationClick no-op when currentLocation is null`() = runTest {
        val vm = LandingViewModel()
        val centerBefore = vm.state.value.mapCenter
        vm.onMyLocationClick()
        assertEquals(centerBefore, vm.state.value.mapCenter)
    }

    @Test
    fun `zoom clamps between min and max`() = runTest {
        val vm = LandingViewModel()
        repeat(20) { vm.onZoomIn() }
        assertEquals(20f, vm.state.value.zoom)
        repeat(20) { vm.onZoomOut() }
        assertEquals(5f, vm.state.value.zoom)
    }

    @Test
    fun `filter setters update the filter`() = runTest {
        val vm = LandingViewModel()
        vm.onQueryChange("Museo")
        assertEquals("Museo", vm.state.value.filter.query)
        vm.onCategoriesChange(setOf(PlaceCategory.MUSEUM))
        assertEquals(setOf(PlaceCategory.MUSEUM), vm.state.value.filter.categories)
        vm.onStatesChange(setOf(AccessibilityState.GREEN))
        assertEquals(setOf(AccessibilityState.GREEN), vm.state.value.filter.states)
        vm.onClearFilters()
        assertEquals("", vm.state.value.filter.query)
        assertTrue(vm.state.value.filter.categories.isEmpty())
        assertTrue(vm.state.value.filter.states.isEmpty())
    }

    @Test
    fun `onSearchActiveChange toggles searchActive`() = runTest {
        val vm = LandingViewModel()
        vm.onSearchActiveChange(true)
        assertEquals(true, vm.state.value.searchActive)
        vm.onSearchActiveChange(false)
        assertEquals(false, vm.state.value.searchActive)
    }

    @Test
    fun `onMenuItemSelected updates selectedMenuItem`() = runTest {
        val vm = LandingViewModel()
        vm.onMenuItemSelected(DrawerMenuItem.MY_REVIEWS)
        assertEquals(DrawerMenuItem.MY_REVIEWS, vm.state.value.selectedMenuItem)
    }

    @Test
    fun `onAuthStateChanged updates username when authenticated`() = runTest {
        val vm = LandingViewModel()
        vm.onAuthStateChanged(AuthState.Authenticated("t", Fixtures.testUser))
        assertEquals("ana", vm.state.value.username)
    }

    @Test
    fun `onAuthStateChanged clears username when unauthenticated`() = runTest {
        val vm = LandingViewModel()
        vm.onAuthStateChanged(AuthState.Authenticated("t", Fixtures.testUser))
        vm.onAuthStateChanged(AuthState.Unauthenticated)
        assertEquals("", vm.state.value.username)
    }

    @Test
    fun `onPlaceClicked centers map on place`() = runTest {
        val vm = LandingViewModel()
        val p = Fixtures.place(latitude = 41.5, longitude = 2.5)
        vm.onPlaceClicked(p)
        assertEquals(Location(41.5, 2.5), vm.state.value.mapCenter)
    }

    @Test
    fun `onPlaceSelectedFromSearch clears query`() = runTest {
        val vm = LandingViewModel()
        vm.onQueryChange("hola")
        val p = Fixtures.place()
        vm.onPlaceSelectedFromSearch(p)
        assertEquals("", vm.state.value.filter.query)
    }

    @Test
    fun `onPlaceAdded appends place and clears query`() = runTest {
        val vm = LandingViewModel()
        vm.onQueryChange("x")
        val dto = org.s3m4su.accesspath.test.Fixtures.let {
            org.s3m4su.accesspath.data.api.PlaceDto(
                id = 99, code = "X", name = "Nuevo", address = "Calle 1",
                latitude = 41.0, longitude = 2.0, description = null,
                googlePlaceId = null, createdBy = 1,
                createdAt = "2026-01-01", updatedAt = "2026-01-01"
            )
        }
        vm.onPlaceAdded(dto)
        assertEquals(1, vm.state.value.places.size)
        assertEquals("", vm.state.value.filter.query)
    }

    @Test
    fun `refreshLocation updates locationDisabled when service reports disabled`() = runTest {
        val service = object : LocationService() {
            override fun isLocationEnabled() = false
            override fun hasLocationPermission() = true
            override suspend fun requestLocationPermission() = true
            override suspend fun getCurrentLocation() = null
        }
        val vm = LandingViewModel()
        vm.refreshLocation(service, null)
        assertEquals(true, vm.state.value.locationDisabled)
    }

    @Test
    fun `state emits distinct values via flow`() = runTest {
        val vm = LandingViewModel()
        vm.state.test {
            assertEquals(LandingUiState(), awaitItem())
            vm.onCameraIdle(MapBounds(0.0, 1.0, 0.0, 1.0))
            assertEquals(MapBounds(0.0, 1.0, 0.0, 1.0), awaitItem().mapBounds)
            cancelAndIgnoreRemainingEvents()
        }
    }
}