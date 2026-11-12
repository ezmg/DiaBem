package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.add_favorite
import com.velkyss.diabem.app.resources.added_sugar
import com.velkyss.diabem.app.resources.carbs
import com.velkyss.diabem.app.resources.criteria_pending
import com.velkyss.diabem.app.resources.custom_limit_used
import com.velkyss.diabem.app.resources.energy
import com.velkyss.diabem.app.resources.fiber
import com.velkyss.diabem.app.resources.fiber_high
import com.velkyss.diabem.app.resources.fiber_source
import com.velkyss.diabem.app.resources.ingredient_line
import com.velkyss.diabem.app.resources.ingredients
import com.velkyss.diabem.app.resources.medical_notice
import com.velkyss.diabem.app.resources.minutes
import com.velkyss.diabem.app.resources.no_data
import com.velkyss.diabem.app.resources.nutrition
import com.velkyss.diabem.app.resources.nutrition_source
import com.velkyss.diabem.app.resources.optional
import com.velkyss.diabem.app.resources.red_warning
import com.velkyss.diabem.app.resources.remove_favorite
import com.velkyss.diabem.app.resources.rice_warning
import com.velkyss.diabem.app.resources.servings
import com.velkyss.diabem.app.resources.servings_less
import com.velkyss.diabem.app.resources.servings_more
import com.velkyss.diabem.app.resources.sodium
import com.velkyss.diabem.app.resources.sodium_value
import com.velkyss.diabem.app.resources.step_number
import com.velkyss.diabem.app.resources.steps
import com.velkyss.diabem.app.resources.substitute
import com.velkyss.diabem.app.resources.substitution_note
import com.velkyss.diabem.app.resources.tips
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.BigOutlinedButton
import com.velkyss.diabem.app.ui.components.NoticeBox
import com.velkyss.diabem.app.ui.components.ReviewBadge
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.SectionTitle
import com.velkyss.diabem.app.ui.components.TrafficLightBadge
import com.velkyss.diabem.app.ui.theme.LocalLightColors
import com.velkyss.diabem.core.health.CriterionStatus
import com.velkyss.diabem.core.health.FiberLabel
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.nutrition.GRAM_UNIT
import com.velkyss.diabem.core.search.QuantityFormatter
import com.velkyss.diabem.core.search.ServingScaler
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(controller: AppController, recipeId: String, meal: MealType?, onBack: () -> Unit) {
    val match = remember(recipeId, meal, controller.profile, controller.selected) { controller.match(recipeId, meal) } ?: return
    val compiled = match.recipe
    val recipe = compiled.recipe
    var servings by remember(recipeId) { mutableIntStateOf(recipe.servings) }
    val scaled = remember(recipeId, servings) { ServingScaler.scale(compiled, servings) }
    val pack = controller.pack
    val favorite = controller.isFavorite(recipe.id)

    ScreenScaffold(title = recipe.name, onBack = onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(pad), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.minutes, recipe.prepMinutes), style = MaterialTheme.typography.bodyLarge)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrafficLightBadge(stringResource(Res.string.carbs), match.health.carbs)
                TrafficLightBadge(stringResource(Res.string.sodium), match.health.sodium)
            }
            if (match.health.relevantRed) {
                Text(stringResource(Res.string.red_warning), color = LocalLightColors.current.red, style = MaterialTheme.typography.bodyLarge)
            }
            if (match.exceedsRiceLimit) {
                val limit = controller.profile?.customLimits?.maxRiceSpoonsPerMeal ?: 0
                Text(
                    stringResource(Res.string.rice_warning, limit, QuantityFormatter.format(match.riceSpoonsPerServing ?: 0.0)),
                    color = LocalLightColors.current.red,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            ReviewBadge(recipe.reviewedByNutritionist)
            BigOutlinedButton(
                stringResource(if (favorite) Res.string.remove_favorite else Res.string.add_favorite),
                onClick = { controller.toggleFavorite(recipe.id) },
                icon = if (favorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
            )

            SectionTitle(stringResource(Res.string.ingredients))
            ServingsStepper(servings, onChange = { servings = it })
            scaled.forEach { s ->
                val food = pack.foods.firstOrNull { it.id == s.ingredient.food }
                val unitName = unitLabel(controller, s.ingredient.unit, s.quantity)
                val line = stringResource(Res.string.ingredient_line, QuantityFormatter.format(s.quantity), unitName, food?.name ?: s.ingredient.food)
                val extra = listOfNotNull(s.ingredient.note, if (s.ingredient.optional) stringResource(Res.string.optional) else null)
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(10.dp))
                    Text(if (extra.isEmpty()) line else "$line, ${extra.joinToString(" ")}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (match.substitutions.isNotEmpty()) {
                match.substitutions.forEach { sub ->
                    Text(stringResource(Res.string.substitute, controller.pantryName(sub.from), controller.pantryName(sub.to)), style = MaterialTheme.typography.bodyMedium)
                }
                Text(stringResource(Res.string.substitution_note), style = MaterialTheme.typography.bodyMedium)
            }

            SectionTitle(stringResource(Res.string.steps))
            recipe.steps.forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.Top) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(stringResource(Res.string.step_number, i + 1), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(step, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (recipe.tips.isNotEmpty()) {
                SectionTitle(stringResource(Res.string.tips))
                recipe.tips.forEach { Text("• $it", style = MaterialTheme.typography.bodyLarge) }
            }

            SectionTitle(stringResource(Res.string.nutrition))
            val noData = stringResource(Res.string.no_data)
            fun v(n: Nutrient, unit: String) = compiled.perServing[n]?.let { "${formatNumber(it)} $unit" } ?: noData
            NutrientRow(stringResource(Res.string.energy), v(Nutrient.ENERGY_KCAL, "kcal"))
            NutrientRow(stringResource(Res.string.carbs), v(Nutrient.CARBOHYDRATE_G, "g"))
            NutrientRow(stringResource(Res.string.fiber), v(Nutrient.FIBER_G, "g"))
            NutrientRow(stringResource(Res.string.sodium_value), v(Nutrient.SODIUM_MG, "mg"))
            NutrientRow(stringResource(Res.string.added_sugar), "${formatNumber(compiled.addedSugarPerServingG)} g")
            when (match.health.fiber) {
                FiberLabel.HIGH -> Text(stringResource(Res.string.fiber_high), style = MaterialTheme.typography.bodyMedium)
                FiberLabel.SOURCE -> Text(stringResource(Res.string.fiber_source), style = MaterialTheme.typography.bodyMedium)
                FiberLabel.NONE -> {}
            }
            if (match.health.usesCustomCarbLimit) {
                Text(stringResource(Res.string.custom_limit_used), style = MaterialTheme.typography.bodyMedium)
            } else if (pack.healthCriteria.carbohydrate.status == CriterionStatus.PENDING_REVIEW) {
                Text(stringResource(Res.string.criteria_pending), style = MaterialTheme.typography.bodyMedium)
            }
            Text(stringResource(Res.string.nutrition_source), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            NoticeBox(stringResource(Res.string.medical_notice))
        }
    }
}

@Composable
private fun ServingsStepper(servings: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(Res.string.servings), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        FilledIconButton(
            onClick = { onChange(servings - 1) },
            enabled = servings > ServingScaler.MIN_SERVINGS,
            modifier = Modifier.size(56.dp),
        ) { Icon(Icons.Filled.Remove, contentDescription = stringResource(Res.string.servings_less)) }
        Text(
            "$servings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp).semantics { liveRegion = LiveRegionMode.Polite },
        )
        FilledIconButton(
            onClick = { onChange(servings + 1) },
            enabled = servings < ServingScaler.MAX_SERVINGS,
            modifier = Modifier.size(56.dp),
        ) { Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.servings_more)) }
    }
}

@Composable
private fun NutrientRow(label: String, value: String) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()
    }
}

private fun unitLabel(controller: AppController, unitId: String, quantity: Double): String {
    if (unitId == GRAM_UNIT) return "g"
    val unit = controller.pack.units.firstOrNull { it.id == unitId } ?: return unitId
    return if (quantity <= 1.0) unit.singular else unit.plural
}

/** Whole numbers from 10 up, one decimal below 10; comma as decimal separator (pt-BR, es). */
internal fun formatNumber(value: Double): String {
    if (value >= 10) return kotlin.math.round(value).toLong().toString()
    val tenths = kotlin.math.round(value * 10).toLong()
    return if (tenths % 10 == 0L) (tenths / 10).toString() else "${tenths / 10},${tenths % 10}"
}
