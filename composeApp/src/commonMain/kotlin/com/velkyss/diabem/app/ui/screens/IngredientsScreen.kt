package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.clear
import com.velkyss.diabem.app.resources.ingredients_help
import com.velkyss.diabem.app.resources.ingredients_title
import com.velkyss.diabem.app.resources.search_hint
import com.velkyss.diabem.app.resources.see_recipes
import com.velkyss.diabem.app.resources.selected_count
import com.velkyss.diabem.app.resources.selected_none
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.BigButton
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.search.normalizeText
import com.velkyss.diabem.app.resources.ingredients_title_with_plan
import com.velkyss.diabem.app.resources.plan_box_hint
import com.velkyss.diabem.app.resources.plan_box_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun IngredientsScreen(controller: AppController, meal: MealSlot, onBack: () -> Unit, onSeeRecipes: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val pack = controller.pack
    val visible = pack.pantry.filterNot { it.staple }
        .filter { query.isBlank() || normalizeText(it.name).contains(normalizeText(query.trim())) }
    val selected = controller.selected

    ScreenScaffold(
        title = stringResource(meal.label),
        onBack = onBack,
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (selected.isEmpty()) stringResource(Res.string.selected_none)
                            else pluralStringResource(Res.plurals.selected_count, selected.size, selected.size),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected.isNotEmpty()) {
                            TextButton(onClick = controller::clearIngredients, modifier = Modifier.heightIn(min = MinTouch)) {
                                Text(stringResource(Res.string.clear), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    BigButton(stringResource(Res.string.see_recipes), onSeeRecipes)
                }
            }
        },
    ) { pad ->
        LazyColumn(contentPadding = pad, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val planOptions = controller.plan.optionsFor(meal)
            if (planOptions.isNotEmpty() && query.isBlank()) {
                item(key = "plan") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(Res.string.plan_box_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                            planOptions.forEach { option ->
                                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()) {
                                    Text(option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(12.dp))
                                }
                            }
                            Text(stringResource(Res.string.plan_box_hint), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item {
                Text(stringResource(if (planOptions.isEmpty()) Res.string.ingredients_title else Res.string.ingredients_title_with_plan), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.size(8.dp))
                Text(stringResource(Res.string.ingredients_help), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(Res.string.search_hint)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch),
                )
            }
            pack.categories.forEach { category ->
                val items = visible.filter { it.category == category.id }
                if (items.isNotEmpty()) {
                    item(key = "cat-${category.id}") {
                        Row(Modifier.padding(top = 20.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(categoryIcon(category.icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(category.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                        }
                        HorizontalDivider()
                    }
                    items(items, key = { it.id }) { item ->
                        IngredientRow(item.name, item.id in selected) { controller.toggleIngredient(item.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(name: String, checked: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch)
            .toggleable(value = checked, onValueChange = { onToggle() }, role = Role.Checkbox),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = null)
            Spacer(Modifier.width(12.dp))
            Text(name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
