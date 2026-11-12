package com.velkyss.diabem.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.FreeBreakfast
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.ui.graphics.vector.ImageVector
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.meal_breakfast
import com.velkyss.diabem.app.resources.meal_dinner
import com.velkyss.diabem.app.resources.meal_lunch
import com.velkyss.diabem.app.resources.meal_snack
import com.velkyss.diabem.app.resources.meal_morning_snack
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.model.MealType
import org.jetbrains.compose.resources.StringResource

val MealType.label: StringResource
    get() = when (this) {
        MealType.BREAKFAST -> Res.string.meal_breakfast
        MealType.LUNCH -> Res.string.meal_lunch
        MealType.SNACK -> Res.string.meal_snack
        MealType.DINNER -> Res.string.meal_dinner
    }

val MealSlot.label: StringResource
    get() = when (this) {
        MealSlot.BREAKFAST -> Res.string.meal_breakfast
        MealSlot.MORNING_SNACK -> Res.string.meal_morning_snack
        MealSlot.LUNCH -> Res.string.meal_lunch
        MealSlot.AFTERNOON_SNACK -> Res.string.meal_snack
        MealSlot.DINNER -> Res.string.meal_dinner
    }

val MealSlot.icon: ImageVector
    get() = if (this == MealSlot.MORNING_SNACK) Icons.Outlined.Spa else mealType.icon

val MealType.icon: ImageVector
    get() = when (this) {
        MealType.BREAKFAST -> Icons.Outlined.FreeBreakfast
        MealType.LUNCH -> Icons.Outlined.LunchDining
        MealType.SNACK -> Icons.Outlined.Cookie
        MealType.DINNER -> Icons.Outlined.DinnerDining
    }

/** Icon keys used in the country pack (pantry.json). */
fun categoryIcon(key: String): ImageVector = when (key) {
    "grain" -> Icons.Outlined.BakeryDining
    "beans" -> Icons.Outlined.Grain
    "vegetable" -> Icons.Outlined.Eco
    "fruit" -> Icons.Outlined.Spa
    "protein" -> Icons.Outlined.Egg
    "dairy" -> Icons.Outlined.LocalDrink
    "basics" -> Icons.Outlined.Kitchen
    else -> Icons.Outlined.Restaurant
}
