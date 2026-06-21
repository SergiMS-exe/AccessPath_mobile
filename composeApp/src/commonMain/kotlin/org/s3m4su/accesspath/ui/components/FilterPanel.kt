package org.s3m4su.accesspath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme
import kotlin.math.roundToInt

/**
 * Panel desplegable bajo la barra de busqueda con los filtros del mapa:
 * chips de categoria (scroll horizontal) y un minimo de accesibilidad media.
 */
@Composable
fun FilterPanel(
    filter: PlaceFilter,
    onCategoriesChange: (Set<PlaceCategory>) -> Unit,
    onMinAccessibilityChange: (Float) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (filter.activeCount > 0) {
                    TextButton(onClick = onClear) {
                        Text(text = "Limpiar", color = colors.primary)
                    }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PlaceCategory.entries) { category ->
                    val selected = category in filter.categories
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val next = filter.categories.toMutableSet().apply {
                                if (selected) remove(category) else add(category)
                            }
                            onCategoriesChange(next)
                        },
                        label = { Text(category.getDisplayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accesibilidad minima",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = if (filter.minAccessibility <= 0f) {
                        "Cualquiera"
                    } else {
                        formatRating(filter.minAccessibility)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
            }

            Slider(
                value = filter.minAccessibility,
                onValueChange = onMinAccessibilityChange,
                valueRange = 0f..5f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

private fun formatRating(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10.0
    val whole = rounded.toInt()
    val decimal = ((rounded - whole) * 10).roundToInt()
    return "$whole.$decimal"
}
