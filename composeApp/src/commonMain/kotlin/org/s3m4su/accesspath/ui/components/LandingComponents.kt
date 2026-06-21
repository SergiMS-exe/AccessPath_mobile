package org.s3m4su.accesspath.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.AccessibilityLevel
import org.s3m4su.accesspath.data.AccessibilityScore
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme
import kotlin.math.roundToInt

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    filter: PlaceFilter,
    onCategoriesChange: (Set<PlaceCategory>) -> Unit,
    onMinAccessibilityChange: (Float) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors
    var showFilters by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
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

                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (query.isEmpty()) {
                        Text(
                            text = "Buscar lugares accesibles",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textTertiary
                        )
                    }
                }

                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar busqueda",
                            tint = colors.iconTint
                        )
                    }
                }

                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filtros",
                        tint = if (showFilters || filter.activeCount > 0) colors.primary else colors.iconTint
                    )
                }
            }

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colors.divider
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
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
                                TextButton(onClick = onClearFilters) {
                                    Text(text = "Limpiar", color = colors.primary)
                                }
                            }
                        }

                        val categoryListState = rememberLazyListState()
                        val canScrollLeft by remember {
                            derivedStateOf {
                                categoryListState.firstVisibleItemIndex > 0 ||
                                    categoryListState.firstVisibleItemScrollOffset > 0
                            }
                        }
                        val canScrollRight by remember {
                            derivedStateOf {
                                val info = categoryListState.layoutInfo
                                val visible = info.visibleItemsInfo
                                visible.isNotEmpty() && (
                                    visible.last().index < info.totalItemsCount - 1 ||
                                        visible.last().offset + visible.last().size > info.viewportEndOffset
                                )
                            }
                        }
                        val leftAlpha by animateFloatAsState(
                            targetValue = if (canScrollLeft) 1f else 0f,
                            animationSpec = tween(200),
                            label = "leftFade"
                        )
                        val rightAlpha by animateFloatAsState(
                            targetValue = if (canScrollRight) 1f else 0f,
                            animationSpec = tween(200),
                            label = "rightFade"
                        )
                        val surfaceColor = colors.surface

                        LazyRow(
                            state = categoryListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawWithContent {
                                    drawContent()
                                    if (leftAlpha > 0f) {
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    surfaceColor.copy(alpha = leftAlpha),
                                                    surfaceColor.copy(alpha = 0f)
                                                ),
                                                startX = 0f,
                                                endX = 56.dp.toPx()
                                            )
                                        )
                                    }
                                    if (rightAlpha > 0f) {
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    surfaceColor.copy(alpha = 0f),
                                                    surfaceColor.copy(alpha = rightAlpha)
                                                ),
                                                startX = size.width - 56.dp.toPx(),
                                                endX = size.width
                                            )
                                        )
                                    }
                                },
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
                                        selectedLabelColor = Color.White
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
                                    formatFilterRating(filter.minAccessibility)
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
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
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
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.divider)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        CategoryChip(category = place.category.getDisplayName())
                    }

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

                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
                imageVector = Icons.AutoMirrored.Filled.Accessible,
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

@Composable
fun AccessibilityLevel.getColor() = when (this) {
    AccessibilityLevel.VERY_EASY -> AccessPathTheme.colors.accessVeryEasy
    AccessibilityLevel.EASY -> AccessPathTheme.colors.accessEasy
    AccessibilityLevel.MODERATE -> AccessPathTheme.colors.accessModerate
    AccessibilityLevel.DIFFICULT -> AccessPathTheme.colors.accessDifficult
}

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

private fun formatFilterRating(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10.0
    val whole = rounded.toInt()
    val decimal = ((rounded - whole) * 10).roundToInt()
    return "$whole.$decimal"
}

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
