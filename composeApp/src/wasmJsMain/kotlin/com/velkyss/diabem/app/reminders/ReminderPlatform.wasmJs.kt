package com.velkyss.diabem.app.reminders

import androidx.compose.runtime.Composable

actual fun reminderPlatform(): ReminderPlatform = NoReminders

@Composable
actual fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit = { onResult(false) }
