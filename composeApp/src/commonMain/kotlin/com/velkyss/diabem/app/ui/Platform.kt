package com.velkyss.diabem.app.ui

import androidx.compose.runtime.Composable

/** System back button (Android). No-op where there is none. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
