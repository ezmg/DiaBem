package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.plan_add
import com.velkyss.diabem.app.resources.plan_empty_slot
import com.velkyss.diabem.app.resources.plan_field_hint
import com.velkyss.diabem.app.resources.plan_field_label
import com.velkyss.diabem.app.resources.plan_help
import com.velkyss.diabem.app.resources.plan_remove
import com.velkyss.diabem.app.resources.plan_title
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.BigOutlinedButton
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.theme.LocalLightColors
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.profile.NutritionistPlan
import org.jetbrains.compose.resources.stringResource

/**
 * Where the person or a relative copies the nutritionist's paper, meal by meal.
 * The text is shown later exactly as written.
 */
@Composable
fun PlanScreen(controller: AppController, onBack: () -> Unit) {
    ScreenScaffold(title = stringResource(Res.string.plan_title), onBack = onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(pad), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(Res.string.plan_help), style = MaterialTheme.typography.bodyLarge)
            MealSlot.entries.forEach { slot -> SlotCard(controller, slot) }
        }
    }
}

@Composable
private fun SlotCard(controller: AppController, slot: MealSlot) {
    var draft by rememberSaveable(slot) { mutableStateOf("") }
    val options = controller.plan.optionsFor(slot)
    val slotName = stringResource(slot.label)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(slot.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(10.dp))
                Text(slotName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            }
            if (options.isEmpty()) {
                Text(stringResource(Res.string.plan_empty_slot), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            options.forEachIndexed { index, option ->
                val removeLabel = stringResource(Res.string.plan_remove, option)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { controller.removePlanOption(slot, index) },
                        modifier = Modifier.size(MinTouch).semantics { contentDescription = removeLabel },
                    ) { Icon(Icons.Filled.Close, contentDescription = null, tint = LocalLightColors.current.red, modifier = Modifier.size(28.dp)) }
                }
            }
            if (options.size < NutritionistPlan.MAX_OPTIONS_PER_SLOT) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(NutritionistPlan.MAX_OPTION_LENGTH) },
                    label = { Text(stringResource(Res.string.plan_field_label, slotName), style = MaterialTheme.typography.bodyMedium) },
                    placeholder = { Text(stringResource(Res.string.plan_field_hint)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch),
                )
                BigOutlinedButton(
                    stringResource(Res.string.plan_add),
                    onClick = { controller.addPlanOption(slot, draft); draft = "" },
                    icon = Icons.Filled.Add,
                )
            }
        }
    }
}
