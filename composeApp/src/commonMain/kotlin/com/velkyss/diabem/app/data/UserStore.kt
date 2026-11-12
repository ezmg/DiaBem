package com.velkyss.diabem.app.data

import com.russhwolf.settings.Settings
import com.velkyss.diabem.core.profile.HealthProfile
import com.velkyss.diabem.core.profile.NutritionistPlan
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Display preferences chosen by the person. */
@Serializable
data class DisplayPrefs(
    val textScale: Float = 1.0f,
    val highContrast: Boolean = false,
) {
    companion object {
        val TEXT_SCALES = listOf(1.0f, 1.15f, 1.3f, 1.5f)
    }
}

/**
 * Everything the app remembers, stored ONLY on this device
 * (SharedPreferences on Android, localStorage in the browser). Nothing is ever sent anywhere.
 */
class UserStore(private val settings: Settings = Settings()) {
    private val json = Json { ignoreUnknownKeys = true }

    var noticeAccepted: Boolean
        get() = settings.getBoolean(KEY_NOTICE, false)
        set(value) = settings.putBoolean(KEY_NOTICE, value)

    var profile: HealthProfile?
        get() = settings.getStringOrNull(KEY_PROFILE)?.let { runCatching { json.decodeFromString(HealthProfile.serializer(), it) }.getOrNull() }
        set(value) {
            if (value == null) settings.remove(KEY_PROFILE)
            else settings.putString(KEY_PROFILE, json.encodeToString(HealthProfile.serializer(), value))
        }

    var favorites: Set<String>
        get() = settings.getStringOrNull(KEY_FAVORITES)?.split(SEP)?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        set(value) = settings.putString(KEY_FAVORITES, value.sorted().joinToString(SEP))

    /** The nutritionist's plan, typed by the person or a relative. */
    var plan: NutritionistPlan
        get() = settings.getStringOrNull(KEY_PLAN)?.let { runCatching { json.decodeFromString(NutritionistPlan.serializer(), it) }.getOrNull() } ?: NutritionistPlan()
        set(value) = settings.putString(KEY_PLAN, json.encodeToString(NutritionistPlan.serializer(), value))

    var displayPrefs: DisplayPrefs
        get() = settings.getStringOrNull(KEY_DISPLAY)?.let { runCatching { json.decodeFromString(DisplayPrefs.serializer(), it) }.getOrNull() } ?: DisplayPrefs()
        set(value) = settings.putString(KEY_DISPLAY, json.encodeToString(DisplayPrefs.serializer(), value))

    /** Deletes everything stored by the app on this device. */
    fun clearAll() = settings.clear()

    private companion object {
        const val KEY_NOTICE = "notice_accepted_v1"
        const val KEY_PROFILE = "health_profile_v1"
        const val KEY_FAVORITES = "favorites_v1"
        const val KEY_PLAN = "nutritionist_plan_v1"
        const val KEY_DISPLAY = "display_prefs_v1"
        const val SEP = ","
    }
}
