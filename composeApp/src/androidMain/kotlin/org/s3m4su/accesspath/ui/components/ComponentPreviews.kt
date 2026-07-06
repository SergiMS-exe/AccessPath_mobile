package org.s3m4su.accesspath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
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
            onStatesChange = {},
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
            onStatesChange = {},
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
                states = setOf(AccessibilityState.GREEN)
            ),
            onCategoriesChange = {},
            onStatesChange = {},
            onClearFilters = {},
            places = previewPlaces,
            onPlaceSelected = {},
            onPlaceAdded = {}
        )
    }
}

// --- PlaceBottomSheet --------------------------------------------------------

@Preview(showBackground = true, name = "BottomSheet — accesible (Light)")
@Composable
fun PlaceBottomSheetGreenLightPreview() {
    AccessPathTheme(darkTheme = false) {
        PlaceBottomSheet(
            place = previewPlaceMuseum,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

@Preview(showBackground = true, name = "BottomSheet — accesible (Dark)", backgroundColor = 0xFF121212)
@Composable
fun PlaceBottomSheetGreenDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        PlaceBottomSheet(
            place = previewPlaceLibrary,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

@Preview(showBackground = true, name = "BottomSheet — no accesible")
@Composable
fun PlaceBottomSheetRedPreview() {
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

// --- Chips del semaforo ------------------------------------------------------

@Preview(showBackground = true, name = "Semaforo — 4 estados (Light)")
@Composable
fun AccessibilityStateChipsLightPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibilityStateChip(state = AccessibilityState.GREEN)
            AccessibilityStateChip(state = AccessibilityState.YELLOW)
            AccessibilityStateChip(state = AccessibilityState.RED)
            AccessibilityStateChip(state = AccessibilityState.NO_DATA)
        }
    }
}

@Preview(showBackground = true, name = "Semaforo — 4 estados (Dark)", backgroundColor = 0xFF121212)
@Composable
fun AccessibilityStateChipsDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibilityStateChip(state = AccessibilityState.GREEN)
            AccessibilityStateChip(state = AccessibilityState.YELLOW)
            AccessibilityStateChip(state = AccessibilityState.RED)
            AccessibilityStateChip(state = AccessibilityState.NO_DATA)
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
        }
    }
}

// --- PhotoGallery ------------------------------------------------------------

@Preview(showBackground = true, name = "PhotoGallery — con fotos (Light)")
@Composable
fun PhotoGalleryLightPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            PhotoGallery(photos = previewPhotos, onPhotoClick = {})
        }
    }
}

@Preview(showBackground = true, name = "PhotoGallery — con fotos (Dark)", backgroundColor = 0xFF121212)
@Composable
fun PhotoGalleryDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            PhotoGallery(photos = previewPhotos, onPhotoClick = {})
        }
    }
}

@Preview(showBackground = true, name = "PhotoGallery — sin fotos")
@Composable
fun PhotoGalleryEmptyPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            PhotoGallery(photos = emptyList(), onPhotoClick = {})
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
