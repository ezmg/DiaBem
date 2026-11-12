package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.add_favorite
import com.velkyss.diabem.app.resources.carbs
import com.velkyss.diabem.app.resources.medical_notice
import com.velkyss.diabem.app.resources.minutes
import com.velkyss.diabem.app.resources.missing
import com.velkyss.diabem.app.resources.no_results
import com.velkyss.diabem.app.resources.red_warning
import com.velkyss.diabem.app.resources.remove_favorite
import com.velkyss.diabem.app.resources.results_title
import com.velkyss.diabem.app.resources.sodium
import com.velkyss.diabem.app.resources.substitute
import com.velkyss.diabem.app.resources.you_have
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.NoticeBox
import com.velkyss.diabem.app.ui.components.ReviewBadge
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.TrafficLightBadge
import com.velkyss.diabem.app.ui.theme.LocalLightColors
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.search.RecipeMatch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultsScreen(controller: AppController, meal: MealType, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val results = remember(meal, controller.selected, controller.profile) { controller.results(meal) }
    ScreenScaffold(title = stringResource(Res.string.results_title), onBack = onBack) { pad ->
        LazyColumn(contentPadding = pad, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(stringResource(meal.label), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            }
            if (results.isEmpty()) {
                item { Text(stringResource(Res.string.no_results), style = MaterialTheme.typography.bodyLarge) }
            }
            items(results, key = { it.recipe.id }) { match ->
                RecipeCard(controller, match, meal) { onOpen(match.recipe.id) }
            }
            item { NoticeBox(stringResource(Res.string.medical_notice)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(controller: AppController, match: RecipeMatch, meal: MealType?, onOpen: () -> Unit) {
    val recipe = match.recipe.recipe
    val needed = match.have.size + match.missing.size + match.substitutions.size
    val favorite = controller.isFavorite(recipe.id)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(recipe.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).semantics { heading() })
                IconButton(onClick = { controller.toggleFavorite(recipe.id) }, modifier = Modifier.size(56.dp)) {
                    Icon(
                        if (favorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = stringResource(if (favorite) Res.string.remove_favorite else Res.string.add_favorite),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.minutes, recipe.prepMinutes), style = MaterialTheme.typography.bodyMedium)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrafficLightBadge(stringResource(Res.string.carbs), match.health.carbs)
                TrafficLightBadge(stringResource(Res.string.sodium), match.health.sodium)
            }
            if (meal != null && needed > 0 && controller.selected.isNotEmpty()) {
                Text(pluralStringResource(Res.plurals.you_have, needed, match.have.size, needed), style = MaterialTheme.typography.bodyMedium)
            }
            if (match.missing.isNotEmpty() && controller.selected.isNotEmpty()) {
                Text(stringResource(Res.string.missing, match.missing.joinToString { controller.pantryName(it) }), style = MaterialTheme.typography.bodyMedium)
            }
            match.substitutions.forEach { s ->
                Text(stringResource(Res.string.substitute, controller.pantryName(s.from), controller.pantryName(s.to)), style = MaterialTheme.typography.bodyMedium)
            }
            if (match.health.relevantRed) {
                Text(stringResource(Res.string.red_warning), color = LocalLightColors.current.red, style = MaterialTheme.typography.bodyMedium)
            }
            ReviewBadge(recipe.reviewedByNutritionist)
        }
    }
}
