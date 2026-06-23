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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

    LaunchedEffect(query) {
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
                    text = "Añadir lugar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Busca un lugar en Google Maps") },
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

                when {
                    isSearching -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                    error != null -> {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    suggestions.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                            items(suggestions) { item ->
                                SuggestionRow(
                                    item = item,
                                    enabled = !isImporting,
                                    onClick = {
                                        scope.launch {
                                            isImporting = true
                                            error = null
                                            PlaceApi.importFromGoogle(item.placeId, sessionToken)
                                                .onSuccess { place ->
                                                    onPlaceAdded(place)
                                                    onDismiss()
                                                }
                                                .onFailure {
                                                    error = if (it.message?.contains("429") == true) {
                                                        "Limite mensual de busquedas alcanzado"
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
                            text = "No se encontraron resultados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                if (isImporting) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
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
