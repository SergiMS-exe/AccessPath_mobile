package org.s3m4su.accesspath.ui.place

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.s3m4su.accesspath.data.Place
import org.s3m4su.accesspath.ui.components.AccessibilityStateChip
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

/**
 * Pantalla de contribucion (tras la puerta "Quiero valorar"): UNA pregunta a la
 * vez, opciones grandes full-width apiladas (>= 56dp), "No lo se" siempre
 * presente, Deshacer visible tras responder, semaforo en vivo, y salida sin
 * culpa en cualquier momento. Paso final opcional: comentario.
 * Sin gamificacion: ni rachas, ni puntos, ni badges.
 */
@Composable
fun ContributeScreen(
    place: Place,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors
    val placeId = place.id.toLongOrNull() ?: run { onClose(); return }
    val viewModel: ContributeViewModel =
        viewModel(key = "contribute-${place.id}") { ContributeViewModel(placeId) }
    val state by viewModel.state.collectAsState()

    var comment by remember { mutableStateOf("") }
    // Camino directo a comentario: valorar no obliga a contestar preguntas.
    var commentMode by remember { mutableStateOf(false) }

    val question = state.question
    val noMoreQuestions = !state.loading && question?.criterion == null

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Cabecera: titulo + salida sin culpa siempre visible.
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    if (state.answeredCount > 0) {
                        Text(
                            text = "${state.answeredCount} respuesta${if (state.answeredCount == 1) "" else "s"} guardada${if (state.answeredCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Salir",
                        tint = colors.iconTint
                    )
                }
            }

            // Semaforo en vivo + Deshacer de la ultima respuesta.
            state.lastResult?.let { result ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = result.dimension.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textSecondary
                                )
                                AccessibilityStateChip(state = result.dimension.state)
                            }
                        }
                        TextButton(
                            onClick = { viewModel.undo() },
                            enabled = !state.saving
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text = "Deshacer", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Modo comentario directo: sin obligacion de contestar preguntas.
            if (commentMode && !noMoreQuestions) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CommentStep(
                        comment = comment,
                        onCommentChange = { comment = it },
                        saved = state.commentSaved,
                        saving = state.saving,
                        onSave = { viewModel.saveComment(comment) }
                    )
                    TextButton(
                        onClick = { commentMode = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Volver a las preguntas", color = colors.primary)
                    }
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(text = "Terminar", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                return@Column
            }

            // Zona de pregunta: siempre en la misma posicion.
            AnimatedContent(
                targetState = Triple(state.loading, question?.criterion?.id, noMoreQuestions),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "question"
            ) { (loading, _, finished) ->
                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    finished -> {
                        // Nada util que preguntar: cierre amable + paso opcional de comentario.
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (state.answeredCount > 0) "¡Gracias por tu ayuda!"
                                        else "Este sitio ya está muy valorado",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "No hay más preguntas pendientes para este sitio.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            CommentStep(
                                comment = comment,
                                onCommentChange = { comment = it },
                                saved = state.commentSaved,
                                saving = state.saving,
                                onSave = { viewModel.saveComment(comment) }
                            )
                            Button(
                                onClick = onClose,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text(text = "Terminar", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    else -> {
                        val criterion = question?.criterion
                        if (criterion != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = criterion.prompt,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // Opciones grandes, full-width, apiladas (>= 56dp).
                                // Sin listas densas. "No lo se" viene del catalogo.
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    question.options.forEach { option ->
                                        Button(
                                            onClick = { viewModel.answer(option) },
                                            enabled = !state.saving,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = if (option.isUnsure) {
                                                ButtonDefaults.buttonColors(
                                                    containerColor = colors.surfaceVariant,
                                                    contentColor = colors.textSecondary
                                                )
                                            } else {
                                                ButtonDefaults.buttonColors(
                                                    containerColor = colors.surface,
                                                    contentColor = colors.textPrimary
                                                )
                                            }
                                        ) {
                                            Text(
                                                text = option.label,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // No hace falta contestar preguntas para aportar:
                                // comentario (y pronto foto) tambien valen solos.
                                TextButton(
                                    onClick = { commentMode = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Prefiero solo dejar un comentario",
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary
                                    )
                                }

                                // Salida sin culpa tambien aqui.
                                TextButton(
                                    onClick = onClose,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Salir — puedes seguir en otro momento",
                                        color = colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Paso final opcional: comentario libre sobre el sitio. */
@Composable
private fun CommentStep(
    comment: String,
    onCommentChange: (String) -> Unit,
    saved: Boolean,
    saving: Boolean,
    onSave: () -> Unit
) {
    val colors = AccessPathTheme.colors

    if (saved) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Comentario guardado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "¿Quieres contar algo más? (opcional)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
            placeholder = { Text("Ej.: la rampa lateral es más cómoda que la entrada principal") },
            shape = RoundedCornerShape(16.dp)
        )
        if (comment.isNotBlank()) {
            Button(
                onClick = onSave,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(text = "Guardar comentario", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
