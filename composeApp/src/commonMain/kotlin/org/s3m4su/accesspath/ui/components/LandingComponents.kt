package org.s3m4su.accesspath.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.AccessibilityLevel
import org.s3m4su.accesspath.data.AccessibilityScore
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.ui.theme.AccessPathTheme
import kotlin.math.roundToInt

@Composable
fun TopSearchBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onMenuClick),
                tint = colors.iconTint
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Mapa Accesible",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onSearchClick),
                tint = colors.iconTint
            )
        }
    }
}

@Composable
fun PlaceBottomSheet(
    place: Place,
    onDismiss: () -> Unit,
    onDetailsClick: () -> Unit,
    onNavigateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val dismissThreshold = 180f
    val dismissOffset = 800f

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            if (offsetY.value + delta >= 0f) {
                                offsetY.snapTo(offsetY.value + delta)
                            }
                        }
                    },
                    onDragStopped = {
                        scope.launch {
                            if (offsetY.value > dismissThreshold) {
                                offsetY.animateTo(
                                    targetValue = dismissOffset,
                                    animationSpec = tween(250)
                                )
                                onDismiss()
                            } else {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(250)
                                )
                            }
                        }
                    }
                ),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.divider)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Place name and image
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Categoria del lugar
                        CategoryChip(category = place.category.getDisplayName())
                    }

                    // Category icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = place.category.getIcon(),
                            contentDescription = place.category.getDisplayName(),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Accessibility and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    place.averageAccessibility?.let { avgScore ->
                        AccessibilityChip(level = avgScore.level)
                    } ?: run {
                        Surface(
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
                    }

                    // Rating
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

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDetailsClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.primary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detalles",
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ir ahora",
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = colors.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AccessibilityTypeRow(
    icon: ImageVector,
    label: String,
    score: AccessibilityScore?,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
        }

        if (score != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = score.score.formatScore(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = score.level.getColor()
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(score.level.getColor())
                )
            }
        } else {
            Text(
                text = "Sin datos",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
fun AccessibilityChip(
    level: AccessibilityLevel,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    val (color, bgColor, text) = when (level) {
        AccessibilityLevel.VERY_EASY -> Triple(
            colors.accessVeryEasy,
            colors.accessVeryEasyBg,
            "Muy Accesible"
        )
        AccessibilityLevel.EASY -> Triple(
            colors.accessEasy,
            colors.accessEasyBg,
            "Accesible"
        )
        AccessibilityLevel.MODERATE -> Triple(
            colors.accessModerate,
            colors.accessModerateBg,
            "Moderado"
        )
        AccessibilityLevel.DIFFICULT -> Triple(
            colors.accessDifficult,
            colors.accessDifficultBg,
            "Difícil"
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Accessible,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun MapControlButtons(
    onMyLocationClick: () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingActionButton(
            onClick = onMyLocationClick,
            containerColor = colors.surface,
            contentColor = colors.primary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación"
            )
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Column {
                IconButton(onClick = onZoomInClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom in",
                        tint = colors.iconTint
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(horizontal = 12.dp),
                    color = colors.divider
                )
                IconButton(onClick = onZoomOutClick) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom out",
                        tint = colors.iconTint
                    )
                }
            }
        }
    }
}

// Extension para obtener color según nivel de accesibilidad
@Composable
fun AccessibilityLevel.getColor() = when (this) {
    AccessibilityLevel.VERY_EASY -> AccessPathTheme.colors.accessVeryEasy
    AccessibilityLevel.EASY -> AccessPathTheme.colors.accessEasy
    AccessibilityLevel.MODERATE -> AccessPathTheme.colors.accessModerate
    AccessibilityLevel.DIFFICULT -> AccessPathTheme.colors.accessDifficult
}

// Extension para formatear scores
private fun Double.formatScore(): String {
    val rounded = kotlin.math.round(this * 10) / 10.0
    val str = rounded.toString()
    return if ('.' in str) {
        val parts = str.split('.')
        "${parts[0]}.${parts[1].take(1)}"
    } else {
        "$str.0"
    }
}

// Extension para obtener el icono de cada categoría
fun PlaceCategory.getIcon(): ImageVector = when (this) {
    PlaceCategory.RESTAURANT -> Icons.Default.Restaurant
    PlaceCategory.CAFE -> Icons.Default.LocalCafe
    PlaceCategory.HOTEL -> Icons.Default.Hotel
    PlaceCategory.MUSEUM -> Icons.Default.Museum
    PlaceCategory.THEATER -> Icons.Default.Theaters
    PlaceCategory.LIBRARY -> Icons.Default.LocalLibrary
    PlaceCategory.SHOP -> Icons.Default.Store
    PlaceCategory.MALL -> Icons.Default.ShoppingBag
    PlaceCategory.HOSPITAL -> Icons.Default.LocalHospital
    PlaceCategory.PHARMACY -> Icons.Default.LocalPharmacy
    PlaceCategory.PARK -> Icons.Default.Park
    PlaceCategory.TRANSPORT -> Icons.Default.DirectionsTransit
    PlaceCategory.OTHER -> Icons.Default.Place
}
