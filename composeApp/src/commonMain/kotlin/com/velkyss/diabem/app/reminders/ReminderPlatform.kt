package com.velkyss.diabem.app.reminders

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

/** One meal reminder as the platform needs it: time plus the texts to show. */
@Serializable
data class ReminderItem(
    /** [com.velkyss.diabem.core.model.MealSlot.key] */
    val slotKey: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val title: String,
    val body: String,
)

/**
 * Daily meal reminders shown by the device itself (local notifications).
 * No server and no internet: the phone's own alarm clock wakes the app.
 */
interface ReminderPlatform {
    /** False where the platform cannot show reminders with the app closed (the web version). */
    val supported: Boolean

    /** True when the person allows this app to show notifications. */
    fun notificationsAllowed(): Boolean

    /** Saves [items] and (re)schedules them. [channelName] is the name shown in the phone settings. */
    fun apply(items: List<ReminderItem>, channelName: String)
}

expect fun reminderPlatform(): ReminderPlatform

/** Returns a function that asks the person for permission to show notifications. */
@Composable
expect fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit

/** Platform without reminders. */
object NoReminders : ReminderPlatform {
    override val supported = false
    override fun notificationsAllowed() = false
    override fun apply(items: List<ReminderItem>, channelName: String) = Unit
}
