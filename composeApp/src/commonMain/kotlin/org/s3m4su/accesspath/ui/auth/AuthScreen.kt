package org.s3m4su.accesspath.ui.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

private enum class AuthTab { LOGIN, REGISTER }

@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val colors = AccessPathTheme.colors
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(AuthTab.LOGIN) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Login fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Register fields
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordConfirm by remember { mutableStateOf("") }

    fun submit() {
        errorMessage = null
        scope.launch {
            isLoading = true
            val result = if (tab == AuthTab.LOGIN) {
                AuthRepository.login(loginEmail.trim(), loginPassword)
            } else {
                if (regPassword != regPasswordConfirm) {
                    errorMessage = "Las contrasenas no coinciden"
                    isLoading = false
                    return@launch
                }
                AuthRepository.register(regUsername.trim(), regEmail.trim(), regPassword)
            }
            isLoading = false
            result.fold(
                onSuccess = { onAuthenticated() },
                onFailure = { errorMessage = it.message ?: "Error desconocido" }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Icon(
                imageVector = Icons.Filled.Accessibility,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "AccessPath",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Text(
                text = "Accesibilidad para todos",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(32.dp))

            // Tab selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    AuthTab.entries.forEach { t ->
                        val selected = t == tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.primary else colors.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            TextButton(
                                onClick = { tab = t; errorMessage = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (t == AuthTab.LOGIN) "Iniciar sesion" else "Registrarse",
                                    color = if (selected) colors.surface else colors.textSecondary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // Form card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { currentTab ->
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentTab == AuthTab.LOGIN) {
                            LoginForm(
                                email = loginEmail,
                                onEmailChange = { loginEmail = it },
                                password = loginPassword,
                                onPasswordChange = { loginPassword = it },
                                onSubmit = ::submit
                            )
                        } else {
                            RegisterForm(
                                username = regUsername,
                                onUsernameChange = { regUsername = it },
                                email = regEmail,
                                onEmailChange = { regEmail = it },
                                password = regPassword,
                                onPasswordChange = { regPassword = it },
                                passwordConfirm = regPasswordConfirm,
                                onPasswordConfirmChange = { regPasswordConfirm = it },
                                onSubmit = ::submit
                            )
                        }
                    }
                }
            }

            // Error
            errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action button
            Button(
                onClick = ::submit,
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colors.surface,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (tab == AuthTab.LOGIN) "Iniciar sesion" else "Crear cuenta",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    AuthField(
        value = email,
        onValueChange = onEmailChange,
        label = "Correo electronico",
        icon = Icons.Filled.Email,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
    )
    AuthField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Contrasena",
        icon = Icons.Filled.Lock,
        isPassword = true,
        imeAction = ImeAction.Done,
        onImeAction = { onSubmit() }
    )
}

@Composable
private fun RegisterForm(
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordConfirm: String,
    onPasswordConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    AuthField(
        value = username,
        onValueChange = onUsernameChange,
        label = "Nombre de usuario",
        icon = Icons.Filled.Person,
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
    )
    AuthField(
        value = email,
        onValueChange = onEmailChange,
        label = "Correo electronico",
        icon = Icons.Filled.Email,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
    )
    AuthField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Contrasena",
        icon = Icons.Filled.Lock,
        isPassword = true,
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
    )
    AuthField(
        value = passwordConfirm,
        onValueChange = onPasswordConfirmChange,
        label = "Confirmar contrasena",
        icon = Icons.Filled.Lock,
        isPassword = true,
        imeAction = ImeAction.Done,
        onImeAction = { onSubmit() }
    )
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val colors = AccessPathTheme.colors
    var passwordVisible by remember { mutableStateOf(false) }
    val visualTransformation = if (isPassword && !passwordVisible)
        PasswordVisualTransformation() else VisualTransformation.None

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.primary),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction() },
                    onDone = { onImeAction() }
                ),
                singleLine = true,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textTertiary
                        )
                    }
                    inner()
                }
            )
            if (isPassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
