package com.velkyss.diabem.core.profile

import com.velkyss.diabem.core.model.MealSlot
import kotlinx.serialization.Serializable

/**
 * What the person's own nutritionist wrote for each meal, copied word for word by the person
 * or a relative. The app only stores and shows this text: it never interprets, scores or
 * changes it, so it can never contradict the nutritionist.
 */
@Serializable
data class NutritionistPlan(val options: Map<MealSlot, List<String>> = emptyMap()) {

    fun optionsFor(slot: MealSlot): List<String> = options[slot].orEmpty()

    val isEmpty: Boolean get() = options.values.all { it.isEmpty() }

    /** Adds [text] to [slot]. Blank text, repeated text or a full slot leave the plan unchanged. */
    fun add(slot: MealSlot, text: String): NutritionistPlan {
        val clean = text.trim().replace(Regex("\\s+"), " ").take(MAX_OPTION_LENGTH)
        val current = optionsFor(slot)
        if (clean.isEmpty() || clean in current || current.size >= MAX_OPTIONS_PER_SLOT) return this
        return copy(options = options + (slot to current + clean))
    }

    fun remove(slot: MealSlot, index: Int): NutritionistPlan {
        val current = optionsFor(slot)
        if (index !in current.indices) return this
        return copy(options = options + (slot to current.filterIndexed { i, _ -> i != index }))
    }

    companion object {
        const val MAX_OPTIONS_PER_SLOT = 10
        const val MAX_OPTION_LENGTH = 200
    }
}
