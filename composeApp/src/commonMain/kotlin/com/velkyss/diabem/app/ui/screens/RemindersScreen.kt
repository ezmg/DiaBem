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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.reminders.ReminderItem
import com.velkyss.diabem.app.reminders.rememberNotificationPermissionRequest
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.reminder_body_default
import com.velkyss.diabem.app.resources.reminder_body_plan
import com.velkyss.diabem.app.resources.reminder_hour
import com.velkyss.diabem.app.resources.reminder_hour_less
import com.velkyss.diabem.app.resources.reminder_minutes
import com.velkyss.diabem.app.resources.reminder_hour_more
import com.velkyss.diabem.app.resources.reminder_minute_less
import com.velkyss.diabem.app.resources.reminder_minute_more
import com.velkyss.diabem.app.resources.reminder_notification_title
import com.velkyss.diabem.app.resources.reminder_switch
import com.velkyss.diabem.app.resources.reminders_delay_note
import com.velkyss.diabem.app.resources.reminders_help
import com.velkyss.diabem.app.resources.reminders_permission_needed
import com.velkyss.diabem.app.resources.reminders_title
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.theme.LocalLightColors
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.profile.MealReminder
import org.jetbrains.compose.resources.stringResource

/**
 * Keeps the phone's alarms in step with what the person chose. The texts are resolved here
 * (in the app language) because the notification is shown later, with the app closed.
 */
@Composable
fun ReminderSync(controller: AppController) {
    if (!controller.reminderPlatform.supported) return
    val channelName = stringResource(Res.string.reminders_title)
    val defaultBody = stringResource(Res.string.reminder_body_default)
    val items = MealSlot.entries.map { slot ->
        val reminder = controller.reminders.reminderFor(slot)
        val options = controller.plan.optionsFor(slot)
        ReminderItem(
            slotKey = slot.key,
            hour = reminder.hour,
            minute = reminder.minute,
            enabled = reminder.enabled,
            title = stringResource(Res.string.reminder_notification_title, stringResource(slot.label)),
            // When the nutritionist's plan has options for this meal, the reminder shows them.
            body = if (options.isEmpty()) defaultBody else stringResource(Res.string.reminder_body_plan, options.joinToString(" / ")),
        )
    }
    LaunchedEffect(items, channelName) { controller.reminderPlatform.apply(items, channelName) }
}

@Composable
fun RemindersScreen(controller: AppController, onBack: () -> Unit) {
    var allowed by remember { mutableStateOf(controller.reminderPlatform.notificationsAllowed()) }
    val askPermission = rememberNotificationPermissionRequest { allowed = it }
    ScreenScaffold(title = stringResource(Res.string.reminders_title), onBack = onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(pad), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(Res.string.reminders_help), style = MaterialTheme.typography.bodyLarge)
            if (controller.reminders.anyEnabled && !allowed) {
                Text(stringResource(Res.string.reminders_permission_needed), color = LocalLightColors.current.red, style = MaterialTheme.typography.bodyLarge)
            }
            MealSlot.entries.forEach { slot ->
                ReminderCard(slot, controller.reminders.reminderFor(slot)) { changed ->
                    if (changed.enabled && !allowed) askPermission()
                    controller.setReminder(slot, changed)
                }
            }
            Text(stringResource(Res.string.reminders_delay_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReminderCard(slot: MealSlot, reminder: MealReminder, onChange: (MealReminder) -> Unit) {
    val name = stringResource(slot.label)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (reminder.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (reminder.enabled) 3.dp else 1.dp, if (reminder.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = MinTouch)
                    .toggleable(value = reminder.enabled, role = Role.Switch, onValueChange = { onChange(reminder.copy(enabled = it)) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(slot.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    Text(stringResource(Res.string.reminder_switch), style = MaterialTheme.typography.bodyMedium)
                }
                Switch(checked = reminder.enabled, onCheckedChange = null)
            }
            Text(
                reminder.label,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            StepRow(
                label = stringResource(Res.string.reminder_hour),
                lessDescription = stringResource(Res.string.reminder_hour_less, name),
                moreDescription = stringResource(Res.string.reminder_hour_more, name),
                onLess = { onChange(reminder.plusHours(-1)) },
                onMore = { onChange(reminder.plusHours(1)) },
            )
            StepRow(
                label = stringResource(Res.string.reminder_minutes),
                lessDescription = stringResource(Res.string.reminder_minute_less, name),
                moreDescription = stringResource(Res.string.reminder_minute_more, name),
                onLess = { onChange(reminder.plusMinutes(-MINUTE_STEP)) },
                onMore = { onChange(reminder.plusMinutes(MINUTE_STEP)) },
            )
        }
    }
}

/** "Hora  [-] [+]": one labelled row, so it is clear what each pair of buttons changes. */
@Composable
private fun StepRow(label: String, lessDescription: String, moreDescription: String, onLess: () -> Unit, onMore: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        StepButton(Icons.Filled.Remove, lessDescription, onLess)
        Spacer(Modifier.width(12.dp))
        StepButton(Icons.Filled.Add, moreDescription, onMore)
    }
}

private const val MINUTE_STEP = 5

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    FilledIconButton(onClick = onClick, modifier = Modifier.size(MinTouch)) {
        Icon(icon, contentDescription = description)
    }
}
