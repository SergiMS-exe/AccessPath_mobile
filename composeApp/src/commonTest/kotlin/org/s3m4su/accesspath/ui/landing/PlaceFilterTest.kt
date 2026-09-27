package org.s3m4su.accesspath.ui.landing

import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.test.Fixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaceFilterTest {

    private fun p(
        id: String = "1",
        name: String = "Cafe",
        address: String = "Calle Mayor 1",
        category: PlaceCategory = PlaceCategory.CAFE,
        overall: AccessibilityState = AccessibilityState.GREEN
    ) = Fixtures.place(id = id, name = name, address = address, category = category, overallState = overall)

    @Test
    fun `empty filter accepts everything`() {
        val f = PlaceFilter()
        assertTrue(f.matches(p()))
        assertTrue(f.matches(p(overall = AccessibilityState.NO_DATA)))
    }

    @Test
    fun `query matches by name case-insensitive`() {
        val f = PlaceFilter(query = "cafe")
        assertTrue(f.matches(p(name = "Cafe Central")))
        assertTrue(f.matches(p(name = "CAFETERIA")))
        assertFalse(f.matches(p(name = "Restaurante")))
    }

    @Test
    fun `query matches by address`() {
        val f = PlaceFilter(query = "mayor")
        assertTrue(f.matches(p(address = "Calle Mayor 1")))
        assertFalse(f.matches(p(address = "Avenida Diagonal")))
    }

    @Test
    fun `query is trimmed`() {
        val f = PlaceFilter(query = "  cafe  ")
        assertTrue(f.matches(p(name = "Cafe Central")))
    }

    @Test
    fun `single category filters out others`() {
        val f = PlaceFilter(categories = setOf(PlaceCategory.MUSEUM))
        assertTrue(f.matches(p(category = PlaceCategory.MUSEUM)))
        assertFalse(f.matches(p(category = PlaceCategory.CAFE)))
    }

    @Test
    fun `multiple categories accept any`() {
        val f = PlaceFilter(categories = setOf(PlaceCategory.MUSEUM, PlaceCategory.LIBRARY))
        assertTrue(f.matches(p(category = PlaceCategory.MUSEUM)))
        assertTrue(f.matches(p(category = PlaceCategory.LIBRARY)))
        assertFalse(f.matches(p(category = PlaceCategory.CAFE)))
    }

    @Test
    fun `single accessibility state filters out others`() {
        val f = PlaceFilter(states = setOf(AccessibilityState.GREEN))
        assertTrue(f.matches(p(overall = AccessibilityState.GREEN)))
        assertFalse(f.matches(p(overall = AccessibilityState.YELLOW)))
        assertFalse(f.matches(p(overall = AccessibilityState.NO_DATA)))
    }

    @Test
    fun `multiple states accept any`() {
        val f = PlaceFilter(states = setOf(AccessibilityState.GREEN, AccessibilityState.YELLOW))
        assertTrue(f.matches(p(overall = AccessibilityState.GREEN)))
        assertTrue(f.matches(p(overall = AccessibilityState.YELLOW)))
        assertFalse(f.matches(p(overall = AccessibilityState.RED)))
    }

    @Test
    fun `combined query + category + state must all match`() {
        val f = PlaceFilter(
            query = "cafe",
            categories = setOf(PlaceCategory.CAFE),
            states = setOf(AccessibilityState.GREEN)
        )
        assertTrue(f.matches(p(name = "Cafe", category = PlaceCategory.CAFE, overall = AccessibilityState.GREEN)))
        assertFalse(f.matches(p(name = "Cafe", category = PlaceCategory.RESTAURANT, overall = AccessibilityState.GREEN)))
        assertFalse(f.matches(p(name = "Cafe", category = PlaceCategory.CAFE, overall = AccessibilityState.RED)))
        assertFalse(f.matches(p(name = "Bar", category = PlaceCategory.CAFE, overall = AccessibilityState.GREEN)))
    }

    @Test
    fun `applyFilter preserves order`() {
        val list = listOf(
            p(id = "1", name = "Cafe"),
            p(id = "2", name = "Museo"),
            p(id = "3", name = "Bar")
        )
        val f = PlaceFilter(query = "")
        assertEquals(list, list.applyFilter(f))
    }

    @Test
    fun `applyFilter returns empty when nothing matches`() {
        val list = listOf(p(name = "Cafe"), p(name = "Bar"))
        assertEquals(emptyList(), list.applyFilter(PlaceFilter(query = "Zzz")))
    }

    @Test
    fun `activeCount counts filters but not the query`() {
        assertEquals(0, PlaceFilter().activeCount)
        assertEquals(0, PlaceFilter(query = "x").activeCount)
        assertEquals(1, PlaceFilter(categories = setOf(PlaceCategory.MUSEUM)).activeCount)
        assertEquals(1, PlaceFilter(states = setOf(AccessibilityState.GREEN)).activeCount)
        assertEquals(2, PlaceFilter(
            categories = setOf(PlaceCategory.MUSEUM),
            states = setOf(AccessibilityState.GREEN)
        ).activeCount)
    }

    @Test
    fun `query substring match ignores surrounding whitespace in name`() {
        val f = PlaceFilter(query = "cafe")
        assertTrue(f.matches(p(name = "  cafe  ")))
        assertTrue(f.matches(p(name = "Mi Cafe Favorito")))
    }
}