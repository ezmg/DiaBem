package com.velkyss.diabem.app.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.velkyss.diabem.R
import com.velkyss.diabem.app.MainActivity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.Calendar

/**
 * Schedules the daily meal reminders with the phone's alarm service and shows them.
 * Everything it needs (times and texts) is kept in its own preferences file, because the
 * alarm can fire when the app is closed.
 */
object ReminderScheduler {
    const val ACTION_FIRE = "com.velkyss.diabem.MEAL_REMINDER"
    const val EXTRA_SLOT = "slot"
    private const val PREFS = "diabem_reminders"
    private const val KEY_ITEMS = "items_v1"
    private const val KEY_CHANNEL_NAME = "channel_name_v1"
    private const val CHANNEL_ID = "meal_reminders"
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(ReminderItem.serializer())

    fun save(context: Context, items: List<ReminderItem>, channelName: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ITEMS, json.encodeToString(serializer, items))
            .putString(KEY_CHANNEL_NAME, channelName)
            .apply()
    }

    fun load(context: Context): List<ReminderItem> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, null)
            ?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }
            .orEmpty()

    /** Sets (or cancels) the next alarm of every reminder. Safe to call at any time. */
    fun scheduleAll(context: Context) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        load(context).forEachIndexed { index, item ->
            val pending = pendingIntent(context, index, item.slotKey)
            if (item.enabled) {
                // Inexact on purpose: no special "exact alarm" permission is needed, and a few
                // minutes of delay do not matter for a meal reminder.
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger(item.hour, item.minute), pending)
            } else {
                alarms.cancel(pending)
            }
        }
    }

    /** Next moment (epoch millis) when the clock shows [hour]:[minute], always in the future. */
    fun nextTrigger(hour: Int, minute: Int, now: Calendar = Calendar.getInstance()): Long {
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= now.timeInMillis + 1_000) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis
    }

    fun show(context: Context, slotKey: String) {
        val items = load(context)
        val index = items.indexOfFirst { it.slotKey == slotKey }
        val item = items.getOrNull(index)?.takeIf { it.enabled } ?: return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context, index,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        try {
            manager.notify(index, notification)
        } catch (_: SecurityException) {
            // The person removed the permission between the check and the call: nothing to show.
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CHANNEL_NAME, null) ?: CHANNEL_ID
        // High importance: the reminder makes a sound and appears on screen.
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun pendingIntent(context: Context, index: Int, slotKey: String): PendingIntent =
        PendingIntent.getBroadcast(
            context, index,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_FIRE).putExtra(EXTRA_SLOT, slotKey),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
