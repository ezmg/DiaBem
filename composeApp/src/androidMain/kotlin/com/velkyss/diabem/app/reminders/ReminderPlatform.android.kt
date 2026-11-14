package com.velkyss.diabem.app.reminders

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationManagerCompat

/** Set once by MainActivity; the reminders need a context outside the UI. */
object AppContextHolder {
    @Volatile
    var context: Context? = null
}

private class AndroidReminders(private val context: Context) : ReminderPlatform {
    override val supported = true

    override fun notificationsAllowed() = NotificationManagerCompat.from(context).areNotificationsEnabled()

    override fun apply(items: List<ReminderItem>, channelName: String) {
        ReminderScheduler.save(context, items, channelName)
        if (items.any { it.enabled }) ReminderScheduler.ensureChannel(context)
        ReminderScheduler.scheduleAll(context)
    }
}

actual fun reminderPlatform(): ReminderPlatform = AppContextHolder.context?.let(::AndroidReminders) ?: NoReminders

@Composable
actual fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else onResult(true)
    }
}
