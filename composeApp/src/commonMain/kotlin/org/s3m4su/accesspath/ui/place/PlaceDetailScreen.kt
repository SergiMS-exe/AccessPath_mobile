package org.s3m4su.accesspath.ui.place

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.data.accessibility.AccessibilityState
import org.s3m4su.accesspath.data.accessibility.CriterionScore
import org.s3m4su.accesspath.data.accessibility.DimensionScore
import org.s3m4su.accesspath.data.accessibility.PlaceSubmission
import org.s3m4su.accesspath.ui.components.PhotoGallery
import org.s3m4su.accesspath.ui.components.PhotoLightbox
import org.s3m4su.accesspath.ui.components.PhotoPlaceholder
import org.s3m4su.accesspath.ui.components.getColor
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

/**
 * Detalle de un lugar (consumo). En la ficha: mini-semaforo por dimension (un
 * vistazo, sin colapsar a un unico estado global) y acceso al desglose completo
 * en un popup que prioriza las dimensiones relevantes para el perfil del
 * usuario. Debajo, "Que cuenta la gente" ya visible sin scroll infinito.
 * Nada de medias ni numeros globales.
 */
@Composable
fun PlaceDetailScreen(
    place: Place,
    onBack: () -> Unit,
    onContribute: (Place) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors
    val viewModel: PlaceDetailViewModel = viewModel(key = "place-detail-${place.id}") { PlaceDetailViewModel() }
    val state by viewModel.state.collectAsState()

    // Recargar al entrar (tambien al volver de Contribute, para ver el semaforo actualizado).
    LaunchedEffect(place.id) {
        place.id.toLongOrNull()?.let { viewModel.load(it) }
    }

    var lightboxIndex by remember { mutableStateOf<Int?>(null) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    val detail = state.detail
    val dimensions = detail?.accessibility?.dimensions ?: place.dimensions
    val galleryPhotos = remember(detail) {
        detail?.submissions.orEmpty().flatMap { sub -> sub.photos.map { it.url } }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
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
                PhotoGallery(
                    photos = galleryPhotos,
                    onPhotoClick = { index -> lightboxIndex = index }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Direccion + mini-semaforo por dimension (vistazo rapido, sin
                // colapsar la accesibilidad a un unico estado).
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = detail?.address ?: place.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        MiniSemaforo(dimensions = dimensions, loading = state.loading)
                        (detail?.description ?: place.description)?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                state.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Acciones: desglose completo en popup + puerta de intencion.
                OutlinedButton(
                    onClick = { showAccessibilityDialog = true },
                    enabled = !state.loading && dimensions.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                ) {
                    Text(text = "Ver accesibilidad en detalle", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onContribute(place) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "Quiero valorar", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Que cuenta la gente — ahora visible sin atravesar el desglose.
                val submissions = detail?.submissions.orEmpty()
                SectionTitle(
                    text = if (submissions.isEmpty()) "Qué cuenta la gente"
                    else "Qué cuenta la gente (${submissions.size})"
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (state.loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (submissions.isEmpty()) {
                    EmptySubmissions()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        submissions.forEach { submission ->
                            SubmissionCard(
                                submission = submission,
                                onPhotoClick = { url ->
                                    val target = galleryPhotos.indexOf(url)
                                    if (target >= 0) lightboxIndex = target
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Visor de fotos a pantalla completa
        lightboxIndex?.let { index ->
            PhotoLightbox(
                photos = galleryPhotos,
                startIndex = index,
                onClose = { lightboxIndex = null }
            )
        }
    }

    if (showAccessibilityDialog) {
        AccessibilityDetailDialog(
            dimensions = dimensions,
            userNeeds = state.userNeeds,
            onDismiss = { showAccessibilityDialog = false }
        )
    }
}

/**
 * Fila compacta con el semaforo de cada dimension CON datos: punto de color +
 * nombre + etiqueta de texto (nunca solo color). Si ninguna tiene datos, un
 * unico "Sin datos" gris con dignidad.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MiniSemaforo(dimensions: List<DimensionScore>, loading: Boolean) {
    val colors = AccessPathTheme.colors
    val withData = dimensions.filter { it.state != AccessibilityState.NO_DATA }

    when {
        loading && dimensions.isEmpty() -> {
            Text(
                text = "Cargando accesibilidad…",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
        }
        withData.isEmpty() -> {
            MiniSemaforoItem(name = "Sin datos aún", state = AccessibilityState.NO_DATA)
        }
        else -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                withData.forEach { dimension ->
                    MiniSemaforoItem(name = dimension.name, state = dimension.state)
                }
            }
        }
    }
}

@Composable
private fun MiniSemaforoItem(name: String, state: AccessibilityState) {
    val colors = AccessPathTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(state.getColor())
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Text(
            text = state.label,
            style = MaterialTheme.typography.labelMedium,
            color = state.getColor()
        )
    }
}

/**
 * Popup con el desglose completo. Si el usuario tiene perfil, primero las
 * dimensiones relevantes para sus necesidades ("Para ti") y el resto plegado
 * tras "Ver el resto". Sin perfil, todas por igual.
 */
@Composable
private fun AccessibilityDetailDialog(
    dimensions: List<DimensionScore>,
    userNeeds: Set<String>,
    onDismiss: () -> Unit
) {
    val colors = AccessPathTheme.colors

    // Una dimension es relevante si algun criterio suyo casa con las necesidades.
    val (relevant, others) = remember(dimensions, userNeeds) {
        if (userNeeds.isEmpty()) dimensions to emptyList()
        else dimensions.partition { dim ->
            dim.criteria.any { cr -> cr.profileTags.any { it in userNeeds } }
        }
    }
    var showOthers by remember { mutableStateOf(relevant.isEmpty()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accesibilidad en detalle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.iconTint
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (userNeeds.isNotEmpty() && relevant.isNotEmpty()) {
                        Text(
                            text = "Para ti",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                    relevant.forEach { dimension ->
                        DimensionSemaforoCard(dimension = dimension)
                    }

                    if (others.isNotEmpty()) {
                        if (!showOthers) {
                            TextButton(
                                onClick = { showOthers = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Ver el resto (${others.size})",
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary
                                )
                            }
                        } else {
                            Text(
                                text = "Resto de dimensiones",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary
                            )
                            others.forEach { dimension ->
                                DimensionSemaforoCard(dimension = dimension)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Tarjeta de una dimension: semaforo (texto + color) y desglose expandible
 * criterio a criterio con conteos, conflicto y ultima valoracion.
 */
@Composable
private fun DimensionSemaforoCard(dimension: DimensionScore) {
    val colors = AccessPathTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
                    .heightIn(min = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = dimension.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dimension.state.getColor())
                        )
                        Text(
                            text = dimension.state.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = dimension.state.getColor()
                        )
                        if (dimension.conflict) {
                            Text(
                                text = "Opiniones divididas",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Ocultar detalle" else "Ver detalle",
                    tint = colors.iconTint
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    dimension.criteria.forEach { criterion ->
                        CriterionRow(criterion = criterion)
                    }
                }
            }
        }
    }
}

/** Fila de un criterio: prompt, punto de color + etiqueta, y confianza. */
@Composable
private fun CriterionRow(criterion: CriterionScore) {
    val colors = AccessPathTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = criterion.prompt,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(criterion.state.getColor())
                )
                Text(
                    text = criterion.state.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = criterion.state.getColor()
                )
            }
        }

        // Confianza: eje separado, informativo. No altera el color.
        val confidence = criterion.confidence
        val parts = buildList {
            if (confidence.nDefined > 0) add("${confidence.nDefined} respuesta${if (confidence.nDefined == 1) "" else "s"}")
            if (confidence.nUnsure > 0) add("${confidence.nUnsure} no lo sabía${if (confidence.nUnsure == 1) "" else "n"}")
            if (criterion.conflict) add("opiniones divididas")
            confidence.lastContributionAt?.let { add("última valoración: ${it.take(10)}") }
        }
        if (parts.isNotEmpty()) {
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
        }
    }
}

/** Tarjeta de "que cuenta la gente": autor, comentario y fotos. */
@Composable
private fun SubmissionCard(
    submission: PlaceSubmission,
    onPhotoClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = submission.authorName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = submission.authorName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    submission.createdAt?.let { date ->
                        Text(
                            text = date.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            submission.comment?.let { comment ->
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            if (submission.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(submission.photos) { _, photo ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPhotoClick(photo.url) }
                        ) {
                            PhotoPlaceholder(seed = photo.url, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
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
private fun EmptySubmissions() {
    val colors = AccessPathTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Aún nadie ha contado nada",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            Text(
                text = "Sé la primera persona en valorar la accesibilidad de este sitio.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary
            )
        }
    }
}
