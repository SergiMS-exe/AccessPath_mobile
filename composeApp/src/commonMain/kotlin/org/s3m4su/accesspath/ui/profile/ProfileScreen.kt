package org.s3m4su.accesspath.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.s3m4su.accesspath.data.accessibility.FunctionalNeed
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

/**
 * Perfil funcional del usuario: necesidades opt-in con proposito explicado y
 * consentimiento explicito. NO es un diagnostico, nunca es obligatorio, y es
 * editable y borrable en cualquier momento. Sin perfil, la app pregunta sobre
 * todos los criterios por igual.
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors
    val viewModel: ProfileViewModel = viewModel(key = "profile") { ProfileViewModel() }
    val state by viewModel.state.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
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
                    text = "Mi perfil de accesibilidad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Proposito, claro y por delante (opt-in informado).
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "¿Para qué sirve esto?",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Si nos cuentas qué necesidades tienes, te haremos primero las " +
                                "preguntas que más te afectan y podrás filtrar sitios por lo que " +
                                "te importa. Es totalmente opcional: sin perfil, la app funciona " +
                                "igual. No es un diagnóstico y puedes borrarlo cuando quieras.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Mis necesidades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Objetivos grandes (>= 56dp), apilados, seleccion multiple.
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FunctionalNeed.entries.forEach { need ->
                        val selected = need.key in state.selectedNeeds
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) colors.primary.copy(alpha = 0.12f) else colors.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleNeed(need.key) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = need.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) colors.primary else colors.textPrimary
                                )
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = colors.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                state.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (state.saved) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Perfil guardado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                // Consentimiento explicito: guardar = consentir, con el texto delante.
                Text(
                    text = "Al guardar aceptas que AccessPath use estas necesidades solo para " +
                        "personalizar preguntas y filtros. Nunca se muestran a otros usuarios " +
                        "ni se usan para otra cosa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.save() },
                    enabled = !state.saving && state.selectedNeeds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(text = "Guardar y dar consentimiento", fontWeight = FontWeight.SemiBold)
                }

                if (state.hasConsent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.delete() },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Borrar mi perfil y retirar el consentimiento",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
