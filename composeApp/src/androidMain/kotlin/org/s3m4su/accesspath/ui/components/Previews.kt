package org.s3m4su.accesspath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.s3m4su.accesspath.data.AccessibilityLevel
import org.s3m4su.accesspath.data.AccessibilityScore
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

@Preview(showBackground = true, name = "Light")
@Composable
fun SearchAndFilterBarLightPreview() {
    AccessPathTheme(darkTheme = false) {
        SearchBar(
            query = "",
            onQueryChange = {},
            onMenuClick = {},
            filter = PlaceFilter(),
            onCategoriesChange = {},
            onMinAccessibilityChange = {},
            onClearFilters = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark", backgroundColor = 0xFF121212)
@Composable
fun SearchAndFilterBarDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        SearchBar(
            query = "Museo",
            onQueryChange = {},
            onMenuClick = {},
            filter = PlaceFilter(minAccessibility = 3f),
            onCategoriesChange = {},
            onMinAccessibilityChange = {},
            onClearFilters = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapControlButtonsPreview() {
    AccessPathTheme {
        MapControlButtons(
            onMyLocationClick = {},
            onZoomInClick = {},
            onZoomOutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "All Chips - Light")
@Composable
fun AccessibilityChipsLightPreview() {
    AccessPathTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AccessibilityChip(level = AccessibilityLevel.VERY_EASY)
            AccessibilityChip(level = AccessibilityLevel.EASY)
            AccessibilityChip(level = AccessibilityLevel.MODERATE)
            AccessibilityChip(level = AccessibilityLevel.DIFFICULT)
        }
    }
}

@Preview(showBackground = true, name = "All Chips - Dark", backgroundColor = 0xFF121212)
@Composable
fun AccessibilityChipsDarkPreview() {
    AccessPathTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AccessibilityChip(level = AccessibilityLevel.VERY_EASY)
            AccessibilityChip(level = AccessibilityLevel.EASY)
            AccessibilityChip(level = AccessibilityLevel.MODERATE)
            AccessibilityChip(level = AccessibilityLevel.DIFFICULT)
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun PlaceBottomSheetLightPreview() {
    val samplePlace = Place(
        id = "1",
        name = "Café Central",
        address = "Av. Principal 123",
        latitude = 40.4168,
        longitude = -3.7038,
        rating = 4.8f,
        category = PlaceCategory.CAFE,
        physicalAccessibility = AccessibilityScore.fromScore(4.5),
        sensoryAccessibility = AccessibilityScore.fromScore(3.5),
        cognitiveAccessibility = AccessibilityScore.fromScore(4.0)
    )

    AccessPathTheme(darkTheme = false) {
        PlaceBottomSheet(
            place = samplePlace,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark", backgroundColor = 0xFF121212)
@Composable
fun PlaceBottomSheetDarkPreview() {
    val samplePlace = Place(
        id = "1",
        name = "Museo de Arte",
        address = "Paseo del Arte 78",
        latitude = 40.4158,
        longitude = -3.7028,
        rating = 4.9f,
        category = PlaceCategory.MUSEUM,
        physicalAccessibility = AccessibilityScore.fromScore(5.0),
        sensoryAccessibility = AccessibilityScore.fromScore(4.8),
        cognitiveAccessibility = AccessibilityScore.fromScore(4.2)
    )

    AccessPathTheme(darkTheme = true) {
        PlaceBottomSheet(
            place = samplePlace,
            onDismiss = {},
            onDetailsClick = {},
            onNavigateClick = {}
        )
    }
}
