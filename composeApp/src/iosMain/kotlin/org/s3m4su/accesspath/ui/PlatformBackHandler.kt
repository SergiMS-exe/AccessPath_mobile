package org.s3m4su.accesspath.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS usa el gesto de swipe nativo; no se necesita handler explicito
}
