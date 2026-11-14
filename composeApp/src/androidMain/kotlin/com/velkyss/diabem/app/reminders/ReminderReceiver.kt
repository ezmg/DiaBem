package com.velkyss.diabem.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Woken by the alarm service when a reminder is due, and by the system after the phone
 * restarts or the app is updated (alarms are lost in both cases and must be set again).
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderScheduler.ACTION_FIRE) {
            intent.getStringExtra(ReminderScheduler.EXTRA_SLOT)?.let { ReminderScheduler.show(context, it) }
        }
        // Always set the next alarms: tomorrow's reminder, or all of them after a restart/update.
        ReminderScheduler.scheduleAll(context)
    }
}
