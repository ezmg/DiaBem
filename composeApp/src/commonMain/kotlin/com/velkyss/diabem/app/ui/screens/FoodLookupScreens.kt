package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.carbs
import com.velkyss.diabem.app.resources.energy
import com.velkyss.diabem.app.resources.fiber
import com.velkyss.diabem.app.resources.food_amount_question
import com.velkyss.diabem.app.resources.food_in_this_amount
import com.velkyss.diabem.app.resources.food_lookup_help
import com.velkyss.diabem.app.resources.food_lookup_none
import com.velkyss.diabem.app.resources.food_lookup_scope
import com.velkyss.diabem.app.resources.food_lookup_title
import com.velkyss.diabem.app.resources.food_no_light
import com.velkyss.diabem.app.resources.food_notice
import com.velkyss.diabem.app.resources.food_portion
import com.velkyss.diabem.app.resources.food_red_warning
import com.velkyss.diabem.app.resources.food_search_hint
import com.velkyss.diabem.app.resources.food_table_name
import com.velkyss.diabem.app.resources.no_data
import com.velkyss.diabem.app.resources.nutrition_source
import com.velkyss.diabem.app.resources.sodium
import com.velkyss.diabem.app.resources.sodium_value
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.NoticeBox
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.SectionTitle
import com.velkyss.diabem.app.ui.components.TrafficLightBadge
import com.velkyss.diabem.app.ui.theme.LocalLightColors
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.core.model.CatalogFood
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.search.QuantityFormatter
import org.jetbrains.compose.resources.stringResource

@Composable
fun FoodSearchScreen(controller: AppController, onBack: () -> Unit, onOpen: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { controller.foodLookup.search(query) }
    ScreenScaffold(title = stringResource(Res.string.food_lookup_title), onBack = onBack) { pad ->
        LazyColumn(contentPadding = pad, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.food_lookup_help), style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it.take(40) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        placeholder = { Text(stringResource(Res.string.food_search_hint)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch),
                    )
                }
            }
            if (query.trim().length >= 2 && results.isEmpty()) {
                item { Text(stringResource(Res.string.food_lookup_none), style = MaterialTheme.typography.bodyLarge) }
            }
            items(results, key = { it.id }) { food ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch).clickable(role = Role.Button) { onOpen(food.id) },
                ) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(food.displayName, style = MaterialTheme.typography.bodyLarge)
                            if (food.aliases.isNotEmpty()) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(28.dp))
                    }
                }
            }
            item {
                Text(stringResource(Res.string.food_lookup_scope, controller.pack.catalog.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun household(food: CatalogFood) = food.portions.isNotEmpty()

/** One selectable amount: a household measure or plain grams. */
private data class Amount(val label: String, val grams: Double)

@Composable
private fun amountsFor(controller: AppController, food: CatalogFood): List<Amount> {
    val household = food.portions.flatMap { portion ->
        val unit = controller.pack.units.firstOrNull { it.id == portion.unit } ?: return@flatMap emptyList()
        listOf(0.5 to unit.singular, 1.0 to unit.singular, 2.0 to unit.plural).map { (count, name) ->
            val grams = portion.grams * count
            Amount(stringResource(Res.string.food_portion, QuantityFormatter.format(count), name, formatNumber(grams)), grams)
        }
    }
    val grams = listOf(50.0, 100.0, 200.0).filter { g -> household.none { it.grams == g } }.map { Amount("${it.toInt()} g", it) }
    return household + grams
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodDetailScreen(controller: AppController, foodId: String, onBack: () -> Unit) {
    val food = controller.foodLookup.food(foodId) ?: return
    val amounts = amountsFor(controller, food)
    // Default: one household unit when known, otherwise 100 g.
    val defaultIndex = if (household(food)) 1 else amounts.indexOfFirst { it.grams == 100.0 }.coerceAtLeast(0)
    var selected by rememberSaveable(foodId) { mutableIntStateOf(defaultIndex) }
    val amount = amounts[selected.coerceIn(amounts.indices)]
    val assessment = remember(foodId, amount.grams, controller.profile) { controller.foodLookup.assess(food, amount.grams, controller.profile) }

    ScreenScaffold(title = food.displayName, onBack = onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(pad), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (food.aliases.isNotEmpty()) {
                Text(stringResource(Res.string.food_table_name, food.name), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SectionTitle(stringResource(Res.string.food_amount_question))
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                amounts.forEachIndexed { index, a ->
                    val isSelected = index == selected
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(if (isSelected) 3.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.heightIn(min = MinTouch).selectable(selected = isSelected, role = Role.RadioButton) { selected = index },
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.label, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assessment.carbs?.let { TrafficLightBadge(stringResource(Res.string.carbs), it) }
                assessment.sodium?.let { TrafficLightBadge(stringResource(Res.string.sodium), it) }
            }
            if (assessment.carbs == null || assessment.sodium == null) {
                Text(stringResource(Res.string.food_no_light), style = MaterialTheme.typography.bodyMedium)
            }
            if (assessment.relevantRed) {
                Text(stringResource(Res.string.food_red_warning), color = LocalLightColors.current.red, style = MaterialTheme.typography.bodyLarge)
            }
            SectionTitle(stringResource(Res.string.food_in_this_amount))
            val noData = stringResource(Res.string.no_data)
            fun v(n: Nutrient, unit: String) = assessment.nutrients[n]?.let { "${formatNumber(it)} $unit" } ?: noData
            FoodNutrientRow(stringResource(Res.string.energy), v(Nutrient.ENERGY_KCAL, "kcal"))
            FoodNutrientRow(stringResource(Res.string.carbs), v(Nutrient.CARBOHYDRATE_G, "g"))
            FoodNutrientRow(stringResource(Res.string.fiber), v(Nutrient.FIBER_G, "g"))
            FoodNutrientRow(stringResource(Res.string.sodium_value), v(Nutrient.SODIUM_MG, "mg"))
            Text(stringResource(Res.string.nutrition_source), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            NoticeBox(stringResource(Res.string.food_notice))
        }
    }
}

@Composable
private fun FoodNutrientRow(label: String, value: String) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()
    }
}
