package org.s3m4su.accesspath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.s3m4su.accesspath.data.AccessibilityLevel
import org.s3m4su.accesspath.data.AccessibilityScore
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

// =============================================================================
// PREVIEWS DE COMPONENTES — piezas reutilizables que se repiten por la app.
// =============================================================================

// --- SearchBar ---------------------------------------------------------------

@Preview(showBackground = true, name = "SearchBar — vacia (Light)")
@Composable
fun SearchBarEmptyLightPreview() {
    AccessPathTheme(darkTheme = false) {
        SearchBar(
            query = "",
            onQueryChange = {},
            onMenuClick = {},
            filter = PlaceFilter(),
            onCategoriesChange = {},
            onMinAccessibilityChange = {},
            onClearFilters = {},
            places = previewPlaces,
            onPlaceSelected = {},
            onPlaceAdded = {}
        )
    }
}

@Preview(showBackground = true, name = "SearchBar — vacia (Dark)", backgroundColor = 0xFF121212)
@Composable
fun SearchBarEmptyDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        SearchBar(
            query = "",
            onQueryChange = {},
            onMenuClick = {},
            filter = PlaceFilter(),
            onCategoriesChange = {},
            onMinAccessibilityChange = {},
            onClearFilters = {},
            places = previewPlaces,
            onPlaceSelected = {},
            onPlaceAdded = {}
        )
    }
}

@Preview(showBackground = true, name = "SearchBar — con filtros activos (Light)")
@Composable
fun SearchBarWithFiltersLightPreview() {
    AccessPathTheme(darkTheme = false) {
        SearchBar(
            query = "Museo",
            onQueryChange = {},
            onMenuClick = {},
            filter = PlaceFilter(
                query = "Museo",
                categories = setOf(PlaceCategory.MUSEUM, PlaceCategory.LIBRARY),
                minAccessibility = 3.5f
            ),
            onCategoriesChange = {},
            onMinAccessibilityChange = {},
            onClearFilters = {},
            places = previewPlaces,
            onPlaceSelected = {},
            onPlaceAdded = {}
        )
    }
}

// --- PlaceBottomSheet --------------------------------------------------------

@Preview(showBackground = true, name = "BottomSheet — muy accesible (Light)")
@Composable
fun PlaceBottomSheetVeryEasyLightPreview() {
    AccessPathTheme(darkTheme = false) {
        PlaceBottomSheet(
            place = previewPlaceMuseum,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

@Preview(showBackground = true, name = "BottomSheet — muy accesible (Dark)", backgroundColor = 0xFF121212)
@Composable
fun PlaceBottomSheetVeryEasyDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        PlaceBottomSheet(
            place = previewPlaceLibrary,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

@Preview(showBackground = true, name = "BottomSheet — dificil acceso")
@Composable
fun PlaceBottomSheetDifficultPreview() {
    AccessPathTheme(darkTheme = false) {
        PlaceBottomSheet(
            place = previewPlaceDifficult,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

@Preview(showBackground = true, name = "BottomSheet — sin datos de accesibilidad")
@Composable
fun PlaceBottomSheetNoDataPreview() {
    AccessPathTheme(darkTheme = false) {
        PlaceBottomSheet(
            place = previewPlaceNoData,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

// --- DrawerMenu --------------------------------------------------------------

@Preview(showBackground = true, name = "Drawer — Inicio seleccionado (Light)")
@Composable
fun DrawerMenuHomeLightPreview() {
    AccessPathTheme(darkTheme = false) {
        DrawerMenu(
            userProfile = previewUserContributor,
            selectedItem = DrawerMenuItem.HOME,
            isDarkMode = false,
            onItemSelected = {},
            onDarkModeToggle = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Drawer — Guardados seleccionado (Dark)", backgroundColor = 0xFF121212)
@Composable
fun DrawerMenuSavedDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        DrawerMenu(
            userProfile = previewUserContributor,
            selectedItem = DrawerMenuItem.SAVED_PLACES,
            isDarkMode = true,
            onItemSelected = {},
            onDarkModeToggle = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Drawer — usuario basico")
@Composable
fun DrawerMenuBasicUserPreview() {
    AccessPathTheme(darkTheme = false) {
        DrawerMenu(
            userProfile = previewUserBasic,
            selectedItem = DrawerMenuItem.MY_REVIEWS,
            isDarkMode = false,
            onItemSelected = {},
            onDarkModeToggle = {},
            onLogout = {}
        )
    }
}

// --- AccessibilityChips ------------------------------------------------------

@Preview(showBackground = true, name = "Chips de accesibilidad (Light)")
@Composable
fun AccessibilityChipsLightPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibilityChip(level = AccessibilityLevel.VERY_EASY)
            AccessibilityChip(level = AccessibilityLevel.EASY)
            AccessibilityChip(level = AccessibilityLevel.MODERATE)
            AccessibilityChip(level = AccessibilityLevel.DIFFICULT)
        }
    }
}

@Preview(showBackground = true, name = "Chips de accesibilidad (Dark)", backgroundColor = 0xFF121212)
@Composable
fun AccessibilityChipsDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibilityChip(level = AccessibilityLevel.VERY_EASY)
            AccessibilityChip(level = AccessibilityLevel.EASY)
            AccessibilityChip(level = AccessibilityLevel.MODERATE)
            AccessibilityChip(level = AccessibilityLevel.DIFFICULT)
        }
    }
}

// --- AccessibilityTypeRow ----------------------------------------------------

@Preview(showBackground = true, name = "Filas de accesibilidad por dimension")
@Composable
fun AccessibilityTypeRowsPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibilityTypeRow(
                icon = Icons.AutoMirrored.Filled.Accessible,
                label = "Fisica",
                score = AccessibilityScore.fromScore(4.5)
            )
            AccessibilityTypeRow(
                icon = Icons.Default.Hearing,
                label = "Sensorial",
                score = AccessibilityScore.fromScore(3.2)
            )
            AccessibilityTypeRow(
                icon = Icons.Default.Psychology,
                label = "Cognitiva",
                score = null
            )
        }
    }
}

@Preview(showBackground = true, name = "Filas de accesibilidad (Dark)", backgroundColor = 0xFF121212)
@Composable
fun AccessibilityTypeRowsDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibilityTypeRow(
                icon = Icons.AutoMirrored.Filled.Accessible,
                label = "Fisica",
                score = AccessibilityScore.fromScore(5.0)
            )
            AccessibilityTypeRow(
                icon = Icons.Default.Hearing,
                label = "Sensorial",
                score = AccessibilityScore.fromScore(4.8)
            )
            AccessibilityTypeRow(
                icon = Icons.Default.Psychology,
                label = "Cognitiva",
                score = AccessibilityScore.fromScore(1.2)
            )
        }
    }
}

// --- CategoryChips -----------------------------------------------------------

@Preview(showBackground = true, name = "Category chips — varias categorias")
@Composable
fun CategoryChipsPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(category = "Cafeteria")
                CategoryChip(category = "Museo")
                CategoryChip(category = "Restaurante")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(category = "Biblioteca")
                CategoryChip(category = "Hotel")
                CategoryChip(category = "Parque")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(category = "Hospital")
                CategoryChip(category = "Transporte")
                CategoryChip(category = "Farmacia")
            }
        }
    }
}

// --- MapControlButtons -------------------------------------------------------

@Preview(showBackground = true, name = "Botones de control del mapa (Light)")
@Composable
fun MapControlButtonsLightPreview() {
    AccessPathTheme(darkTheme = false) {
        MapControlButtons(
            onMyLocationClick = {},
            onZoomInClick = {},
            onZoomOutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Botones de control del mapa (Dark)", backgroundColor = 0xFF121212)
@Composable
fun MapControlButtonsDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        MapControlButtons(
            onMyLocationClick = {},
            onZoomInClick = {},
            onZoomOutClick = {}
        )
    }
}
