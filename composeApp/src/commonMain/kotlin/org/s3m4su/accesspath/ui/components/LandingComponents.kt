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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.PlaceCategory
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.api.AutocompleteItemDto
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.PlaceDto
import org.s3m4su.accesspath.ui.landing.PlaceFilter
import org.s3m4su.accesspath.ui.theme.AccessPathTheme
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    filter: PlaceFilter,
    onCategoriesChange: (Set<PlaceCategory>) -> Unit,
    onStatesChange: (Set<AccessibilityState>) -> Unit,
    onClearFilters: () -> Unit,
    places: List<Place>,
    onPlaceSelected: (Place) -> Unit,
    onPlaceAdded: (PlaceDto) -> Unit,
    onActiveChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var showFilters by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var googleMode by remember { mutableStateOf(false) }
    var googleSuggestions by remember { mutableStateOf<List<AutocompleteItemDto>>(emptyList()) }
    var isGoogleSearching by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }
    var sessionToken by remember { mutableStateOf(Uuid.random().toString()) }

    val showDropdown = isActive && query.isNotEmpty()

    val filteredPlaces = remember(places, query) {
        if (query.isBlank()) emptyList()
        else places.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true)
        }.take(5)
    }

    LaunchedEffect(query, googleMode) {
        if (!googleMode || query.length < 3) {
            googleSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        isGoogleSearching = true
        googleError = null
        PlaceApi.search(query, sessionToken)
            .onSuccess { googleSuggestions = it }
            .onFailure { googleError = "Error al buscar. Intenta de nuevo." }
        isGoogleSearching = false
    }

    fun dismiss() {
        focusManager.clearFocus()
        isActive = false
        googleMode = false
        googleSuggestions = emptyList()
        googleError = null
    }

    val shape = if (showDropdown) RoundedCornerShape(16.dp) else RoundedCornerShape(28.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, shape),
        shape = shape,
        color = colors.surface,
        tonalElevation = 2.dp
    ) {
        Column {
            // --- Fila de búsqueda ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (googleMode) {
                    IconButton(onClick = {
                        googleMode = false
                        googleSuggestions = emptyList()
                        googleError = null
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.iconTint
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onMenuClick),
                        tint = colors.iconTint
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    isActive = true
                                    onActiveChange(true)
                                } else {
                                    scope.launch {
                                        delay(100)
                                        isActive = false
                                        onActiveChange(false)
                                        googleMode = false
                                        googleSuggestions = emptyList()
                                    }
                                }
                            }
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
                    IconButton(onClick = {
                        onQueryChange("")
                        dismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = colors.iconTint)
                    }
                }

                if (!googleMode) {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Filtros",
                            tint = if (showFilters || filter.activeCount > 0) colors.primary else colors.iconTint
                        )
                    }
                }
            }

            // --- Dropdown de resultados ---
            AnimatedVisibility(
                visible = showDropdown,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = colors.divider)

                    if (googleMode) {
                        when {
                            isImporting -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = colors.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Añadiendo lugar...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            isGoogleSearching -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = colors.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            googleError != null -> {
                                Text(
                                    text = googleError!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                )
                            }
                            googleSuggestions.isNotEmpty() -> {
                                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                                    items(googleSuggestions) { item ->
                                        GoogleSuggestionRow(
                                            item = item,
                                            enabled = !isImporting,
                                            onClick = {
                                                scope.launch {
                                                    isImporting = true
                                                    googleError = null
                                                    PlaceApi.importFromGoogle(item.placeId, sessionToken)
                                                        .onSuccess { dto ->
                                                            sessionToken = Uuid.random().toString()
                                                            onPlaceAdded(dto)
                                                            dismiss()
                                                        }
                                                        .onFailure {
                                                            googleError = when {
                                                                it.message?.contains("429") == true ->
                                                                    "Limite mensual de busquedas alcanzado"
                                                                // Mensaje del backend (p.ej. sitio cerrado permanentemente).
                                                                !it.message.isNullOrBlank() && it is IllegalStateException ->
                                                                    it.message
                                                                else -> "Error al añadir el lugar"
                                                            }
                                                        }
                                                    isImporting = false
                                                }
                                            }
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            color = colors.divider
                                        )
                                    }
                                }
                            }
                            query.length >= 3 -> {
                                Text(
                                    text = "No se encontraron resultados en Google Maps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textTertiary,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                )
                            }
                        }
                    } else {
                        // Resultados de AccessPath
                        if (filteredPlaces.isNotEmpty()) {
                            LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                                items(filteredPlaces) { place ->
                                    AccessPathPlaceRow(place = place) {
                                        dismiss()
                                        onPlaceSelected(place)
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = colors.divider
                                    )
                                }
                            }
                        }

                        // Fila de Google Maps al fondo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple()
                                ) { googleMode = true }
                                .padding(horizontal = 20.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Buscar \"$query\" en Google Maps",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- Panel de filtros (oculto mientras el dropdown está abierto) ---
            AnimatedVisibility(
                visible = showFilters && !showDropdown,
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
                                text = "Estado de accesibilidad",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (filter.states.isEmpty()) "Cualquiera"
                                       else "${filter.states.size} elegido${if (filter.states.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary
                            )
                        }

                        // Chips del semaforo: etiqueta de texto + color por estado
                        // (nunca solo color). Multi-seleccion; vacio = cualquiera.
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(AccessibilityState.entries) { state ->
                                val selected = state in filter.states
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val next = filter.states.toMutableSet().apply {
                                            if (selected) remove(state) else add(state)
                                        }
                                        onStatesChange(next)
                                    },
                                    label = { Text(state.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = state.getColor(),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
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

                // Estado global del semaforo (sin nota media ni estrellas: la
                // accesibilidad nunca se colapsa a un numero).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccessibilityStateChip(state = place.overallState)
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

/**
 * Chip del semaforo de accesibilidad: etiqueta de texto + color por estado
 * (nunca solo color, requisito de accesibilidad). Gris = sin datos, con la
 * misma dignidad visual que el resto de estados.
 */
@Composable
fun AccessibilityStateChip(
    state: AccessibilityState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = state.getBgColor()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Accessible,
                contentDescription = null,
                tint = state.getColor(),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = state.getColor()
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
private fun AccessPathPlaceRow(place: Place, onClick: () -> Unit) {
    val colors = AccessPathTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = place.category.getIcon(),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            if (place.address.isNotEmpty()) {
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun GoogleSuggestionRow(
    item: AutocompleteItemDto,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = AccessPathTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.mainText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                text = item.secondaryText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

/** Color de primer plano del semaforo (paleta WCAG AA del tema). */
@Composable
fun AccessibilityState.getColor() = when (this) {
    AccessibilityState.GREEN -> AccessPathTheme.colors.accessVeryEasy
    AccessibilityState.YELLOW -> AccessPathTheme.colors.accessModerate
    AccessibilityState.RED -> AccessPathTheme.colors.accessDifficult
    AccessibilityState.NO_DATA -> AccessPathTheme.colors.accessNoData
}

/** Color de fondo suave del semaforo (paleta WCAG AA del tema). */
@Composable
fun AccessibilityState.getBgColor() = when (this) {
    AccessibilityState.GREEN -> AccessPathTheme.colors.accessVeryEasyBg
    AccessibilityState.YELLOW -> AccessPathTheme.colors.accessModerateBg
    AccessibilityState.RED -> AccessPathTheme.colors.accessDifficultBg
    AccessibilityState.NO_DATA -> AccessPathTheme.colors.accessNoDataBg
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
