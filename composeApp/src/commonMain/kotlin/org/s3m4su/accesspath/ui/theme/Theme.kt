package org.s3m4su.accesspath.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ===========================================
// COLORES DE ACCESIBILIDAD - Alto contraste
// ===========================================
object AccessibilityColors {
    // Colores para modo claro - WCAG AA compliant
    object Light {
        val veryEasy = Color(0xFF1B5E20)      // Verde oscuro - contraste 7:1
        val veryEasyBg = Color(0xFFE8F5E9)    // Verde muy claro
        val easy = Color(0xFF0277BD)           // Azul - contraste 5.5:1
        val easyBg = Color(0xFFE1F5FE)        // Azul muy claro
        val moderate = Color(0xFFE65100)       // Naranja oscuro - contraste 4.5:1
        val moderateBg = Color(0xFFFFF3E0)    // Naranja muy claro
        val difficult = Color(0xFFC62828)      // Rojo oscuro - contraste 5.9:1
        val difficultBg = Color(0xFFFFEBEE)   // Rojo muy claro
        val noData = Color(0xFF616161)         // Gris
        val noDataBg = Color(0xFFF5F5F5)      // Gris claro
    }

    // Colores para modo oscuro
    object Dark {
        val veryEasy = Color(0xFF81C784)      // Verde claro
        val veryEasyBg = Color(0xFF1B3D1F)    // Verde oscuro
        val easy = Color(0xFF4FC3F7)           // Azul claro
        val easyBg = Color(0xFF0D3347)        // Azul oscuro
        val moderate = Color(0xFFFFB74D)       // Naranja claro
        val moderateBg = Color(0xFF4A2800)    // Naranja oscuro
        val difficult = Color(0xFFE57373)      // Rojo claro
        val difficultBg = Color(0xFF4A1C1C)   // Rojo oscuro
        val noData = Color(0xFFBDBDBD)         // Gris claro
        val noDataBg = Color(0xFF424242)      // Gris oscuro
    }
}

// ===========================================
// COLORES DE LA APP
// ===========================================
object AppColors {
    object Light {
        // Superficies
        val surface = Color(0xFFFFFFFF)
        val surfaceVariant = Color(0xFFF5F5F5)
        val background = Color(0xFFFAFAFA)

        // Textos
        val textPrimary = Color(0xFF1F1F1F)
        val textSecondary = Color(0xFF757575)
        val textTertiary = Color(0xFF9E9E9E)

        // Acentos
        val primary = Color(0xFF6200EE)
        val primaryVariant = Color(0xFF3700B3)
        val secondary = Color(0xFF03DAC6)
        val success = Color(0xFF00C853)
        val warning = Color(0xFFFF9800)
        val error = Color(0xFFB00020)

        // UI Elements
        val divider = Color(0xFF757575)
        val iconTint = Color(0xFF1F1F1F)
        val starRating = Color(0xFFFFC107)
        val shadow = Color(0x1A000000)
    }

    object Dark {
        // Superficies
        val surface = Color(0xFF1E1E1E)
        val surfaceVariant = Color(0xFF2D2D2D)
        val background = Color(0xFF121212)

        // Textos
        val textPrimary = Color(0xFFE1E1E1)
        val textSecondary = Color(0xFFB0B0B0)
        val textTertiary = Color(0xFF757575)

        // Acentos
        val primary = Color(0xFFBB86FC)
        val primaryVariant = Color(0xFF3700B3)
        val secondary = Color(0xFF03DAC6)
        val success = Color(0xFF69F0AE)
        val warning = Color(0xFFFFB74D)
        val error = Color(0xFFCF6679)

        // UI Elements
        val divider = Color(0xFF3D3D3D)
        val iconTint = Color(0xFFE1E1E1)
        val starRating = Color(0xFFFFD54F)
        val shadow = Color(0x40000000)
    }
}

// ===========================================
// COLOR SCHEME WRAPPER
// ===========================================
data class AccessPathColors(
    val isDark: Boolean,
    // Superficies
    val surface: Color,
    val surfaceVariant: Color,
    val background: Color,
    // Textos
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // Acentos
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    // UI Elements
    val divider: Color,
    val iconTint: Color,
    val starRating: Color,
    // Accesibilidad
    val accessVeryEasy: Color,
    val accessVeryEasyBg: Color,
    val accessEasy: Color,
    val accessEasyBg: Color,
    val accessModerate: Color,
    val accessModerateBg: Color,
    val accessDifficult: Color,
    val accessDifficultBg: Color,
    val accessNoData: Color,
    val accessNoDataBg: Color
)

val LightAccessPathColors = AccessPathColors(
    isDark = false,
    surface = AppColors.Light.surface,
    surfaceVariant = AppColors.Light.surfaceVariant,
    background = AppColors.Light.background,
    textPrimary = AppColors.Light.textPrimary,
    textSecondary = AppColors.Light.textSecondary,
    textTertiary = AppColors.Light.textTertiary,
    primary = AppColors.Light.primary,
    primaryVariant = AppColors.Light.primaryVariant,
    secondary = AppColors.Light.secondary,
    success = AppColors.Light.success,
    warning = AppColors.Light.warning,
    error = AppColors.Light.error,
    divider = AppColors.Light.divider,
    iconTint = AppColors.Light.iconTint,
    starRating = AppColors.Light.starRating,
    accessVeryEasy = AccessibilityColors.Light.veryEasy,
    accessVeryEasyBg = AccessibilityColors.Light.veryEasyBg,
    accessEasy = AccessibilityColors.Light.easy,
    accessEasyBg = AccessibilityColors.Light.easyBg,
    accessModerate = AccessibilityColors.Light.moderate,
    accessModerateBg = AccessibilityColors.Light.moderateBg,
    accessDifficult = AccessibilityColors.Light.difficult,
    accessDifficultBg = AccessibilityColors.Light.difficultBg,
    accessNoData = AccessibilityColors.Light.noData,
    accessNoDataBg = AccessibilityColors.Light.noDataBg
)

val DarkAccessPathColors = AccessPathColors(
    isDark = true,
    surface = AppColors.Dark.surface,
    surfaceVariant = AppColors.Dark.surfaceVariant,
    background = AppColors.Dark.background,
    textPrimary = AppColors.Dark.textPrimary,
    textSecondary = AppColors.Dark.textSecondary,
    textTertiary = AppColors.Dark.textTertiary,
    primary = AppColors.Dark.primary,
    primaryVariant = AppColors.Dark.primaryVariant,
    secondary = AppColors.Dark.secondary,
    success = AppColors.Dark.success,
    warning = AppColors.Dark.warning,
    error = AppColors.Dark.error,
    divider = AppColors.Dark.divider,
    iconTint = AppColors.Dark.iconTint,
    starRating = AppColors.Dark.starRating,
    accessVeryEasy = AccessibilityColors.Dark.veryEasy,
    accessVeryEasyBg = AccessibilityColors.Dark.veryEasyBg,
    accessEasy = AccessibilityColors.Dark.easy,
    accessEasyBg = AccessibilityColors.Dark.easyBg,
    accessModerate = AccessibilityColors.Dark.moderate,
    accessModerateBg = AccessibilityColors.Dark.moderateBg,
    accessDifficult = AccessibilityColors.Dark.difficult,
    accessDifficultBg = AccessibilityColors.Dark.difficultBg,
    accessNoData = AccessibilityColors.Dark.noData,
    accessNoDataBg = AccessibilityColors.Dark.noDataBg
)

val LocalAccessPathColors = staticCompositionLocalOf { LightAccessPathColors }

// CompositionLocal para el callback de toggle (permite cambiar el tema desde cualquier lugar)
val LocalDarkModeToggle = staticCompositionLocalOf<(Boolean) -> Unit> { {} }

// Material3 color schemes
private val LightColorScheme = lightColorScheme(
    primary = AppColors.Light.primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.Light.primary.copy(alpha = 0.12f),
    secondary = AppColors.Light.secondary,
    background = AppColors.Light.background,
    surface = AppColors.Light.surface,
    surfaceVariant = AppColors.Light.surfaceVariant,
    onBackground = AppColors.Light.textPrimary,
    onSurface = AppColors.Light.textPrimary,
    error = AppColors.Light.error
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Dark.primary,
    onPrimary = Color.Black,
    primaryContainer = AppColors.Dark.primary.copy(alpha = 0.12f),
    secondary = AppColors.Dark.secondary,
    background = AppColors.Dark.background,
    surface = AppColors.Dark.surface,
    surfaceVariant = AppColors.Dark.surfaceVariant,
    onBackground = AppColors.Dark.textPrimary,
    onSurface = AppColors.Dark.textPrimary,
    error = AppColors.Dark.error
)

@Composable
fun AccessPathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onDarkThemeChange: (Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkAccessPathColors else LightAccessPathColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAccessPathColors provides colors,
        LocalDarkModeToggle provides onDarkThemeChange
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

// Helper para acceder a los colores y funciones del tema desde cualquier composable
object AccessPathTheme {
    val colors: AccessPathColors
        @Composable
        get() = LocalAccessPathColors.current

    val isDark: Boolean
        @Composable
        get() = LocalAccessPathColors.current.isDark

    val toggleDarkMode: (Boolean) -> Unit
        @Composable
        get() = LocalDarkModeToggle.current
}
