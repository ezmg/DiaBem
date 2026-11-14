package com.velkyss.diabem.core.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A calendar day, without time zone. Kept as "2026-11-14" when stored. */
data class LocalDay(val year: Int, val month: Int, val day: Int) : Comparable<LocalDay> {
    init {
        require(month in 1..12 && day in 1..31) { "Invalid date $year-$month-$day" }
    }

    /** "2026-11-14" */
    val iso: String get() = "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    /** "14/11/2026", the usual way to write dates in Brazil and Spain. */
    val display: String get() = "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/$year"

    /** "14/11" */
    val shortDisplay: String get() = "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}"

    fun plusDays(days: Int): LocalDay = fromEpochDay(toEpochDay() + days)

    /** Days since 1970-01-01 (proleptic Gregorian calendar). */
    fun toEpochDay(): Long {
        val y = year.toLong() - if (month <= 2) 1 else 0
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    override fun compareTo(other: LocalDay): Int = toEpochDay().compareTo(other.toEpochDay())

    companion object {
        fun fromEpochDay(epochDay: Long): LocalDay {
            val z = epochDay + 719468
            val era = (if (z >= 0) z else z - 146096) / 146097
            val doe = z - era * 146097
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val y = yoe + era * 400
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
            val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
            return LocalDay((y + if (m <= 2) 1 else 0).toInt(), m, d)
        }

        fun parse(iso: String): LocalDay? {
            val parts = iso.split('-')
            if (parts.size != 3) return null
            val (y, m, d) = parts.map { it.toIntOrNull() ?: return null }
            return runCatching { LocalDay(y, m, d) }.getOrNull()?.takeIf { fromEpochDay(it.toEpochDay()) == it }
        }
    }
}

/** The moments of the day of the nutritionist's sheet. Serial names are stored: never change them. */
@Serializable
enum class GlucoseMoment {
    @SerialName("fasting") FASTING,
    @SerialName("after_breakfast") AFTER_BREAKFAST,
    @SerialName("after_lunch") AFTER_LUNCH,
    @SerialName("after_dinner") AFTER_DINNER,
}

/**
 * The person's blood glucose diary: the numbers they read on their own meter, by day and moment.
 *
 * The app only stores and shows these numbers so the person can take them to their doctor or
 * nutritionist. It never judges them (no "high" or "low", no targets, no colours) and never
 * suggests insulin, medicine or what to do: that is the health professional's job.
 */
@Serializable
data class GlucoseLog(
    /** Day ("2026-11-14") -> moment -> value in mg/dL. */
    val entries: Map<String, Map<GlucoseMoment, Int>> = emptyMap(),
) {
    fun valuesFor(day: LocalDay): Map<GlucoseMoment, Int> = entries[day.iso].orEmpty()

    /** Days that have at least one value, newest first. */
    val days: List<LocalDay> get() = entries.filterValues { it.isNotEmpty() }.keys.mapNotNull(LocalDay::parse).sortedDescending()

    val isEmpty: Boolean get() = entries.values.all { it.isEmpty() }

    /**
     * Sets, changes or (with null) removes one value. A value outside what a meter can show is
     * a typing mistake and is ignored.
     */
    fun with(day: LocalDay, moment: GlucoseMoment, value: Int?): GlucoseLog {
        if (value != null && value !in MIN_VALUE..MAX_VALUE) return this
        val current = valuesFor(day)
        val updated = if (value == null) current - moment else current + (moment to value)
        return copy(entries = if (updated.isEmpty()) entries - day.iso else entries + (day.iso to updated))
    }

    /** The diary as plain text, to copy and show or send to the health professional. */
    fun toShareText(title: String, unit: String = UNIT, momentLabel: (GlucoseMoment) -> String): String = buildString {
        appendLine(title)
        days.forEach { day ->
            appendLine(day.display)
            val values = valuesFor(day)
            GlucoseMoment.entries.forEach { moment -> values[moment]?.let { appendLine("- ${momentLabel(moment)}: $it $unit") } }
        }
    }.trimEnd()

    companion object {
        const val UNIT = "mg/dL"

        /** Limits of what can be typed (meter range). They catch typos; they are not health limits. */
        const val MIN_VALUE = 20
        const val MAX_VALUE = 600
    }
}
