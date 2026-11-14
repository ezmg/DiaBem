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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.velkyss.diabem.app.resources.back
import com.velkyss.diabem.app.resources.plan_backup_copy
import com.velkyss.diabem.app.resources.plan_backup_copy_button
import com.velkyss.diabem.app.resources.plan_backup_copy_help
import com.velkyss.diabem.app.resources.plan_backup_help
import com.velkyss.diabem.app.resources.plan_backup_paste_label
import com.velkyss.diabem.app.resources.plan_backup_restore
import com.velkyss.diabem.app.resources.plan_backup_restore_button
import com.velkyss.diabem.app.resources.plan_backup_restore_help
import com.velkyss.diabem.app.resources.plan_backup_restored
import com.velkyss.diabem.app.resources.plan_backup_title
import com.velkyss.diabem.app.ui.components.SectionTitle
import org.jetbrains.compose.resources.pluralStringResource
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
            BackupSection(controller)
        }
    }
}

/** Lets the person keep a copy of the plan outside the app, and bring it back. */
@Composable
private fun BackupSection(controller: AppController) {
    var showCopy by rememberSaveable { mutableStateOf(false) }
    var showRestore by rememberSaveable { mutableStateOf(false) }
    var restored by rememberSaveable { mutableStateOf<Int?>(null) }
    val clipboard = LocalClipboardManager.current
    val labels = MealSlot.entries.associateWith { stringResource(it.label) }

    SectionTitle(stringResource(Res.string.plan_backup_title))
    Text(stringResource(Res.string.plan_backup_help), style = MaterialTheme.typography.bodyMedium)
    if (!controller.plan.isEmpty) {
        BigOutlinedButton(stringResource(Res.string.plan_backup_copy), { showCopy = true }, icon = Icons.Outlined.ContentCopy)
    }
    BigOutlinedButton(stringResource(Res.string.plan_backup_restore), { showRestore = true; restored = null }, icon = Icons.Outlined.ContentPaste)
    restored?.let { Text(pluralStringResource(Res.plurals.plan_backup_restored, it, it), style = MaterialTheme.typography.bodyLarge) }

    if (showCopy) {
        val text = controller.plan.toBackupText { labels.getValue(it) }
        AlertDialog(
            onDismissRequest = { showCopy = false },
            title = { Text(stringResource(Res.string.plan_backup_copy)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.plan_backup_copy_help), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(value = text, onValueChange = {}, readOnly = true, textStyle = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = { clipboard.setText(AnnotatedString(text)); showCopy = false }, modifier = Modifier.heightIn(min = MinTouch)) {
                    Text(stringResource(Res.string.plan_backup_copy_button), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCopy = false }, modifier = Modifier.heightIn(min = MinTouch)) {
                    Text(stringResource(Res.string.back), style = MaterialTheme.typography.labelLarge)
                }
            },
        )
    }
    if (showRestore) {
        var pasted by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRestore = false },
            title = { Text(stringResource(Res.string.plan_backup_restore)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.plan_backup_restore_help), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = pasted,
                        onValueChange = { pasted = it.take(20_000) },
                        label = { Text(stringResource(Res.string.plan_backup_paste_label)) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 260.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { restored = controller.restorePlan(pasted); showRestore = false }, modifier = Modifier.heightIn(min = MinTouch)) {
                    Text(stringResource(Res.string.plan_backup_restore_button), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestore = false }, modifier = Modifier.heightIn(min = MinTouch)) {
                    Text(stringResource(Res.string.back), style = MaterialTheme.typography.labelLarge)
                }
            },
        )
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
