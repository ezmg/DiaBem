package com.velkyss.diabem.core.profile

import com.velkyss.diabem.core.model.MealSlot
import kotlinx.serialization.Serializable

/** A daily reminder for one meal, at [hour]:[minute] (24-hour clock). */
@Serializable
data class MealReminder(val hour: Int, val minute: Int, val enabled: Boolean = false) {
    init {
        require(hour in 0..23 && minute in 0..59) { "Invalid time $hour:$minute" }
    }

    /** "07:05" */
    val label: String get() = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    fun plusHours(delta: Int) = copy(hour = (hour + delta).mod(24))

    fun plusMinutes(delta: Int) = copy(minute = (minute + delta).mod(60))
}

/**
 * The person's meal reminders. They only remind the person to eat at the times they chose
 * (for example, the mid-morning snack their nutritionist asked for). The app does not decide
 * the times: the starting values are only a convenience and can be changed freely.
 */
@Serializable
data class MealReminders(val times: Map<MealSlot, MealReminder> = emptyMap()) {

    fun reminderFor(slot: MealSlot): MealReminder = times[slot] ?: DEFAULT_TIMES.getValue(slot)

    fun with(slot: MealSlot, reminder: MealReminder) = copy(times = times + (slot to reminder))

    val anyEnabled: Boolean get() = times.values.any { it.enabled }

    companion object {
        /** Starting times shown before the person chooses their own. All disabled. */
        val DEFAULT_TIMES = mapOf(
            MealSlot.BREAKFAST to MealReminder(7, 0),
            MealSlot.MORNING_SNACK to MealReminder(10, 0),
            MealSlot.LUNCH to MealReminder(12, 0),
            MealSlot.AFTERNOON_SNACK to MealReminder(15, 30),
            MealSlot.DINNER to MealReminder(19, 0),
        )
    }
}
