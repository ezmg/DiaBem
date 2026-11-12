package com.velkyss.diabem.app.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop is only used for UI tests; the on-screen "back" button is enough.
}
