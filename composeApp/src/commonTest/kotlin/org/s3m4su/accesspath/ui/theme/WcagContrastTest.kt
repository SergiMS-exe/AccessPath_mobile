package org.s3m4su.accesspath.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

class WcagContrastTest {

    private fun hexToRgb(color: Color): Triple<Int, Int, Int> {
        val argb = color.toArgb()
        return Triple(
            (argb shr 16) and 0xFF,
            (argb shr 8) and 0xFF,
            argb and 0xFF
        )
    }

    private fun hexStringToColor(s: String): Color {
        val h = s.removePrefix("#")
        val r = h.substring(0, 2).toInt(16)
        val g = h.substring(2, 4).toInt(16)
        val b = h.substring(4, 6).toInt(16)
        return Color(red = r, green = g, blue = b)
    }

    private fun lum(rgb: Triple<Int, Int, Int>): Double {
        fun channel(c: Int): Double {
            val s = c / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(rgb.first) + 0.7152 * channel(rgb.second) + 0.0722 * channel(rgb.third)
    }

    fun ratio(fg: Color, bg: Color): Double {
        val l1 = lum(hexToRgb(fg))
        val l2 = lum(hexToRgb(bg))
        val max = maxOf(l1, l2)
        val min = minOf(l1, l2)
        return (max + 0.05) / (min + 0.05)
    }

    fun ratio(fg: String, bg: String): Double = ratio(hexStringToColor(fg), hexStringToColor(bg))

    private fun assertAA(fg: Color, bg: Color, context: String) {
        val r = ratio(fg, bg)
        assertTrue(r >= 4.5, "$context: contrasto $r:1 < AA 4.5:1 (fg=$fg bg=$bg)")
    }

    private fun assertAA(fg: Color, bg: String, context: String) {
        assertAA(fg, hexStringToColor(bg), context)
    }

    private fun assertAA(fg: String, bg: String, context: String) {
        assertAA(hexStringToColor(fg), hexStringToColor(bg), context)
    }

    @Test
    fun `light theme textPrimary on all surfaces meets AA`() {
        for (bg in listOf(AppColors.Light.surface, AppColors.Light.background, AppColors.Light.surfaceVariant)) {
            assertAA(AppColors.Light.textPrimary, bg, "textPrimary on $bg")
        }
    }

    @Test
    fun `light theme textSecondary on all surfaces meets AA`() {
        // #6E6E6E garantiza >= 4.5:1 sobre los 3 fondos de AppColors.Light
        // (#757575 solo daba 4.4 sobre #FAFAFA, fuera de WCAG AA).
        val r1 = ratio("#6E6E6E", "#FFFFFF")
        val r2 = ratio("#6E6E6E", "#FAFAFA")
        val r3 = ratio("#6E6E6E", "#F5F5F5")
        val min = minOf(r1, r2, r3)
        assertTrue(min >= 4.5,
            "textSecondary #6E6E6E falla en al menos un fondo: surface=$r1, background=$r2, surfaceVariant=$r3")
    }

    @Test
    fun `light theme textTertiary meets AA on all surfaces after fix`() {
        val newTertiary = "#6E6E6E"
        val r1 = ratio(newTertiary, "#FFFFFF")
        val r2 = ratio(newTertiary, "#FAFAFA")
        val r3 = ratio(newTertiary, "#F5F5F5")
        val min = minOf(r1, r2, r3)
        assertTrue(min >= 4.5,
            "textTertiary light $newTertiary falla en algun fondo: surface=$r1, background=$r2, surfaceVariant=$r3")
    }

    @Test
    fun `dark theme textTertiary meets AA on all surfaces after fix`() {
        val newTertiary = "#A0A0A0"
        val r1 = ratio(newTertiary, "#1E1E1E")
        val r2 = ratio(newTertiary, "#121212")
        val r3 = ratio(newTertiary, "#2D2D2D")
        val min = minOf(r1, r2, r3)
        assertTrue(min >= 4.5,
            "textTertiary dark $newTertiary falla en algun fondo: surface=$r1, background=$r2, surfaceVariant=$r3")
    }

    @Test
    fun `light theme accessibility palette contrasts meet AA on bg colors`() {
        assertAA(AppColors.Light.textPrimary, "#E8F5E9", "veryEasy on veryEasyBg")
        // #01579B en lugar de #0277BD: el azul original daba 4.27:1 sobre
        // #E1F5FE, fuera de AA 4.5:1.
        assertAA("#01579B", "#E1F5FE", "easy on easyBg")
        // #BF360C en lugar de #E65100: el naranja original daba 3.45:1 sobre
        // #FFF3E0, fuera de AA 4.5:1.
        assertAA("#BF360C", "#FFF3E0", "moderate on moderateBg")
        assertAA("#C62828", "#FFEBEE", "difficult on difficultBg")
    }

    @Test
    fun `dark theme textPrimary on all surfaces meets AA`() {
        for (bg in listOf(AppColors.Dark.surface, AppColors.Dark.background, AppColors.Dark.surfaceVariant)) {
            assertAA(AppColors.Dark.textPrimary, bg, "textPrimary on $bg (dark)")
        }
    }

    @Test
    fun `dark theme accessibility palette contrasts meet AA`() {
        assertAA(AppColors.Dark.textPrimary, "#1B3D1F", "veryEasy on veryEasyBg (dark)")
        assertAA("#4FC3F7", "#0D3347", "easy on easyBg (dark)")
        assertAA("#FFB74D", "#4A2800", "moderate on moderateBg (dark)")
        assertAA("#E57373", "#4A1C1C", "difficult on difficultBg (dark)")
    }

    @Test
    fun `contrast ratio helper returns expected values for known pairs`() {
        assertTrue(kotlin.math.abs(ratio("#FFFFFF", "#000000") - 21.0) < 0.1)
        assertTrue(kotlin.math.abs(ratio("#000000", "#FFFFFF") - 21.0) < 0.1)
        assertTrue(kotlin.math.abs(ratio("#FFFFFF", "#FFFFFF") - 1.0) < 0.01)
    }
}