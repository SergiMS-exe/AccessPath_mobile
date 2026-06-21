package org.s3m4su.accesspath.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.s3m4su.accesspath.data.AccessibilityDimension
import org.s3m4su.accesspath.data.AccessibilityFeature
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.ui.components.AccessibilityChip
import org.s3m4su.accesspath.ui.components.AccessibilityTypeRow
import org.s3m4su.accesspath.ui.components.CategoryChip
import org.s3m4su.accesspath.ui.components.getIcon
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

/**
 * Pantalla de detalle de un lugar. Muestra la cabecera, la accesibilidad media,
 * el desglose por dimension (fisica/sensorial/cognitiva) y la lista de
 * caracteristicas. Trabaja sobre el modelo de dominio [Place] (hoy mock).
 */
@Composable
fun PlaceDetailScreen(
    place: Place,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // Cabecera con boton de volver
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.iconTint
                )
            }
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Tarjeta de cabecera: icono de categoria + nombre + direccion
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = place.category.getIcon(),
                            contentDescription = place.category.getDisplayName(),
                            tint = colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryChip(category = place.category.getDisplayName())
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accesibilidad media + valoracion general
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                place.averageAccessibility?.let { avg ->
                    AccessibilityChip(level = avg.level)
                } ?: Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.accessNoDataBg
                ) {
                    Text(
                        text = "Sin datos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accessNoData,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = colors.starRating,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = place.rating.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            }

            place.description?.let { description ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Desglose por dimension
            SectionTitle(text = "Accesibilidad por tipo")
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccessibilityTypeRow(
                    icon = Icons.AutoMirrored.Filled.Accessible,
                    label = "Fisica",
                    score = place.physicalAccessibility
                )
                AccessibilityTypeRow(
                    icon = Icons.Default.Hearing,
                    label = "Sensorial",
                    score = place.sensoryAccessibility
                )
                AccessibilityTypeRow(
                    icon = Icons.Default.Psychology,
                    label = "Cognitiva",
                    score = place.cognitiveAccessibility
                )
            }

            if (place.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(text = "Caracteristicas")
                Spacer(modifier = Modifier.height(12.dp))
                FeaturesByDimension(features = place.features)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AccessPathTheme.colors.textPrimary
    )
}

@Composable
private fun FeaturesByDimension(features: List<AccessibilityFeature>) {
    val colors = AccessPathTheme.colors
    val grouped = features.groupBy { it.dimension }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AccessibilityDimension.entries.forEach { dimension ->
            val items = grouped[dimension] ?: return@forEach
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = dimension.getDisplayName(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                items.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = feature.getDisplayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
