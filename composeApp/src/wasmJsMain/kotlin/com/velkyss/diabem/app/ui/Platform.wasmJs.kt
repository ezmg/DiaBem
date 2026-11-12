package com.velkyss.diabem.app.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // The browser has its own back button; the app shows an on-screen "back" button instead.
}
