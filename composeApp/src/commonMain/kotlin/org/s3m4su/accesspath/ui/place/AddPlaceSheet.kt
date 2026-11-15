package org.s3m4su.accesspath.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.api.AutocompleteItemDto
import org.s3m4su.accesspath.data.api.PlaceApi
import org.s3m4su.accesspath.data.api.PlaceDto
import org.s3m4su.accesspath.data.api.PlacePrefillDto
import org.s3m4su.accesspath.data.api.ResolvedLinkDto
import org.s3m4su.accesspath.ui.theme.AccessPathTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddPlaceSheet(
    onDismiss: () -> Unit,
    onPlaceAdded: (PlaceDto) -> Unit
) {
    val colors = AccessPathTheme.colors
    val scope = rememberCoroutineScope()

    val sessionToken = remember { Uuid.random().toString() }
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<AutocompleteItemDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fallback: el sitio no esta en el catalogo.
    var showLinkInput by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }
    var isResolving by remember { mutableStateOf(false) }
    var resolved by remember { mutableStateOf<ResolvedLinkDto?>(null) }
    var community by remember { mutableStateOf<PlacePrefillDto?>(null) }
    var communityName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var pendingName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        // Un cambio de texto invalida el enlace/formulario anteriores.
        resolved = null
        community = null
        if (query.length < 3) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        isSearching = true
        error = null
        PlaceApi.search(query, sessionToken)
            .onSuccess { suggestions = it }
            .onFailure { error = "Error al buscar. Intenta de nuevo." }
        isSearching = false
    }

    fun resolveLink() {
        val url = linkUrl.trim()
        if (url.isEmpty()) {
            error = "Pega un enlace de Google Maps"
            return
        }
        scope.launch {
            isResolving = true
            error = null
            resolved = null
            community = null
            PlaceApi.resolveLink(url)
                .onSuccess { link ->
                    resolved = link
                    if (!link.matched) {
                        val prefill = link.prefill ?: PlacePrefillDto(sourceUrl = url)
                        community = prefill
                        communityName = prefill.name
                        address = prefill.address.orEmpty()
                        if (link.message != null) error = link.message
                    }
                }
                .onFailure { error = it.message ?: "No se pudo leer el enlace" }
            isResolving = false
        }
    }

    fun submitCommunity() {
        val prefill = community ?: return
        val lat = prefill.latitude
        val lng = prefill.longitude
        if (communityName.isBlank()) {
            error = "Escribe el nombre del sitio"
            return
        }
        if (address.isBlank()) {
            error = "Escribe la direccion del sitio"
            return
        }
        if (lat == null || lng == null) {
            error = "Abre el sitio en Google Maps y comparte su enlace, no el del mapa"
            return
        }

        scope.launch {
            isCreating = true
            error = null
            PlaceApi.createCommunity(
                name = communityName.trim(),
                address = address.trim(),
                latitude = lat,
                longitude = lng,
                category = prefill.category,
                sourceUrl = prefill.sourceUrl,
                googleFtid = prefill.googleFtid
            )
                .onSuccess { pendingName = it.name }
                .onFailure { error = it.message ?: "No se pudo crear el sitio" }
            isCreating = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        // Sheet
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.divider)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (pendingName == null) "Añadir lugar" else "Enviado a revision",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (pendingName != null) {
                    Text(
                        text = "\"$pendingName\" se ha enviado para revisar. " +
                            "Aparecera en el mapa cuando lo verifiquemos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Busca un lugar") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = colors.iconTint)
                        },
                        trailingIcon = if (query.isNotEmpty()) {
                            {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = colors.iconTint)
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (error != null) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    when {
                        isSearching -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(28.dp))
                            }
                        }
                        suggestions.isNotEmpty() -> {
                            // Column simple (no LazyColumn): el sheet ya es
                            // scrollable y anidar scroll vertical en la misma
                            // direccion rompe la medida en Compose.
                            Column {
                                suggestions.forEach { item ->
                                    SuggestionRow(
                                        item = item,
                                        enabled = !isImporting,
                                        onClick = {
                                            scope.launch {
                                                isImporting = true
                                                error = null
                                                // El backend resuelve este id contra el
                                                // catalogo activo (o contra el lugar local).
                                                PlaceApi.importFromCatalog(item.source, item.externalId, item.localPlaceId)
                                                    .onSuccess { place ->
                                                        onPlaceAdded(place)
                                                        onDismiss()
                                                    }
                                                    .onFailure {
                                                        error = if (it.message?.contains("429") == true) {
                                                            "Demasiadas peticiones. Espera un momento."
                                                        } else {
                                                            "Error al añadir el lugar"
                                                        }
                                                    }
                                                isImporting = false
                                            }
                                        }
                                    )
                                    HorizontalDivider(color = colors.divider)
                                }
                            }
                        }
                        query.length >= 3 -> {
                            Text(
                                text = "No encontramos ese sitio",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textTertiary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Puedes pegar su enlace de Google Maps y lo anadimos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )

                            if (!showLinkInput) {
                                TextButton(onClick = { showLinkInput = true }) {
                                    Text("Pegar enlace de Google Maps")
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = linkUrl,
                                    onValueChange = { linkUrl = it },
                                    label = { Text("Enlace de Google Maps") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { resolveLink() },
                                    enabled = !isResolving,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Buscar el sitio")
                                }
                            }
                        }
                    }

                    // El enlace coincide con una ficha del catalogo.
                    resolved?.catalog?.let { item ->
                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = item.address.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isImporting = true
                                    error = null
                                    PlaceApi.importFromCatalog(item.source, item.externalId)
                                        .onSuccess { place ->
                                            onPlaceAdded(place)
                                            onDismiss()
                                        }
                                        .onFailure { error = it.message ?: "No se pudo anyadir el lugar" }
                                    isImporting = false
                                }
                            },
                            enabled = !isImporting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Usar este sitio")
                        }
                    }

                    // No esta en el catalogo: formulario prerellenado.
                    community?.let { prefill ->
                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "Añadir este sitio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "No esta en el catalogo. Se publicara cuando lo revisemos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = communityName,
                            onValueChange = { communityName = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Direccion") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        val lat = prefill.latitude
                        val lng = prefill.longitude
                        if (lat != null && lng != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coordenadas: $lat, $lng",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { submitCommunity() },
                            enabled = !isCreating,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar a revision")
                        }
                    }

                    if (isImporting || isResolving || isCreating) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Datos de lugares: (c) Overture Maps Foundation, CDLA-Permissive 2.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    item: AutocompleteItemDto,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = AccessPathTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
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
