package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.app_name
import com.velkyss.diabem.app.resources.condition_both
import com.velkyss.diabem.app.resources.condition_diabetes
import com.velkyss.diabem.app.resources.condition_hypertension
import com.velkyss.diabem.app.resources.limit_carbs
import com.velkyss.diabem.app.resources.limit_rice
import com.velkyss.diabem.app.resources.limits_help
import com.velkyss.diabem.app.resources.limits_title
import com.velkyss.diabem.app.resources.notice_accept
import com.velkyss.diabem.app.resources.notice_point_doctor
import com.velkyss.diabem.app.resources.notice_point_doses
import com.velkyss.diabem.app.resources.notice_point_privacy
import com.velkyss.diabem.app.resources.notice_point_recipes
import com.velkyss.diabem.app.resources.notice_title
import com.velkyss.diabem.app.resources.profile_question
import com.velkyss.diabem.app.resources.profile_title
import com.velkyss.diabem.app.resources.save
import com.velkyss.diabem.app.ui.components.BigButton
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.SectionTitle
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.CustomLimits
import com.velkyss.diabem.core.profile.HealthProfile
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoticeScreen(onAccept: () -> Unit) {
    val appName = stringResource(Res.string.app_name)
    ScreenScaffold(title = stringResource(Res.string.notice_title), onBack = null) { pad ->
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(pad),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            NoticePoint(Icons.Outlined.RestaurantMenu, stringResource(Res.string.notice_point_recipes, appName))
            NoticePoint(Icons.Outlined.MedicalServices, stringResource(Res.string.notice_point_doctor))
            NoticePoint(Icons.Outlined.HealthAndSafety, stringResource(Res.string.notice_point_doses))
            NoticePoint(Icons.Outlined.Lock, stringResource(Res.string.notice_point_privacy))
            Spacer(Modifier.heightIn(min = 8.dp))
            BigButton(stringResource(Res.string.notice_accept), onAccept)
        }
    }
}

@Composable
private fun NoticePoint(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

private enum class ConditionChoice(val conditions: Set<Condition>) {
    DIABETES(setOf(Condition.DIABETES_TYPE_2)),
    HYPERTENSION(setOf(Condition.HYPERTENSION)),
    BOTH(setOf(Condition.DIABETES_TYPE_2, Condition.HYPERTENSION)),
}

@Composable
fun ProfileScreen(initial: HealthProfile?, onSave: (HealthProfile) -> Unit, onBack: (() -> Unit)?) {
    var choice by remember { mutableStateOf(ConditionChoice.entries.firstOrNull { it.conditions == initial?.conditions }) }
    var carbs by remember { mutableStateOf(initial?.customLimits?.maxCarbsPerMealG?.toInt()?.toString() ?: "") }
    var rice by remember { mutableStateOf(initial?.customLimits?.maxRiceSpoonsPerMeal?.toString() ?: "") }
    val carbsValue = carbs.toIntOrNull()?.takeIf { it in 1..300 }
    val riceValue = rice.toIntOrNull()?.takeIf { it in 1..30 }
    val inputsOk = (carbs.isBlank() || carbsValue != null) && (rice.isBlank() || riceValue != null)

    ScreenScaffold(title = stringResource(Res.string.profile_title), onBack = onBack) { pad ->
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(pad),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(stringResource(Res.string.profile_question))
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ConditionChoice.entries.forEach { c ->
                    val label = when (c) {
                        ConditionChoice.DIABETES -> Res.string.condition_diabetes
                        ConditionChoice.HYPERTENSION -> Res.string.condition_hypertension
                        ConditionChoice.BOTH -> Res.string.condition_both
                    }
                    ChoiceRow(stringResource(label), selected = choice == c) { choice = c }
                }
            }
            SectionTitle(stringResource(Res.string.limits_title))
            Text(stringResource(Res.string.limits_help), style = MaterialTheme.typography.bodyMedium)
            NumberField(stringResource(Res.string.limit_carbs), carbs, isError = carbs.isNotBlank() && carbsValue == null) { carbs = it }
            NumberField(stringResource(Res.string.limit_rice), rice, isError = rice.isNotBlank() && riceValue == null) { rice = it }
            BigButton(
                stringResource(Res.string.save),
                enabled = choice != null && inputsOk,
                onClick = {
                    val c = choice ?: return@BigButton
                    onSave(HealthProfile(c.conditions, CustomLimits(carbsValue?.toDouble(), riceValue)))
                },
            )
        }
    }
}

@Composable
private fun ChoiceRow(text: String, selected: Boolean, onSelect: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(if (selected) 3.dp else 1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, isError: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() }.take(3)) },
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        singleLine = true,
        isError = isError,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch),
    )
}

