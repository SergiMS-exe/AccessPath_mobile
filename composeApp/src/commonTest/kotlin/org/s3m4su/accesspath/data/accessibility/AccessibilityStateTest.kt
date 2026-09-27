package org.s3m4su.accesspath.data.accessibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccessibilityStateTest {

    @Test
    fun `fromApi maps known states`() {
        assertEquals(AccessibilityState.GREEN, AccessibilityState.fromApi("green"))
        assertEquals(AccessibilityState.YELLOW, AccessibilityState.fromApi("yellow"))
        assertEquals(AccessibilityState.RED, AccessibilityState.fromApi("red"))
        assertEquals(AccessibilityState.NO_DATA, AccessibilityState.fromApi("no_data"))
    }

    @Test
    fun `fromApi maps unknown and null to NO_DATA`() {
        assertEquals(AccessibilityState.NO_DATA, AccessibilityState.fromApi("???"))
        assertEquals(AccessibilityState.NO_DATA, AccessibilityState.fromApi(null))
        assertEquals(AccessibilityState.NO_DATA, AccessibilityState.fromApi(""))
    }

    @Test
    fun `label is descriptive and unique per state`() {
        val labels = AccessibilityState.entries.map { it.label }.toSet()
        assertEquals(4, labels.size)
        assertTrue(labels.containsAll(setOf("Accesible", "Con salvedades", "No accesible", "Sin datos")))
    }

    @Test
    fun `FunctionalNeed fromKey roundtrips each entry`() {
        for (n in FunctionalNeed.entries) {
            assertEquals(n, FunctionalNeed.fromKey(n.key))
        }
    }

    @Test
    fun `FunctionalNeed fromKey returns null for unknown key`() {
        assertNull(FunctionalNeed.fromKey("inventado"))
        assertNull(FunctionalNeed.fromKey(""))
    }
}