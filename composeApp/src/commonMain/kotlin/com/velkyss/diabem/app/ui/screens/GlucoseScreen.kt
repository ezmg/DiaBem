package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.back
import com.velkyss.diabem.app.resources.glucose_after_breakfast
import com.velkyss.diabem.app.resources.glucose_after_breakfast_short
import com.velkyss.diabem.app.resources.glucose_after_dinner
import com.velkyss.diabem.app.resources.glucose_after_dinner_short
import com.velkyss.diabem.app.resources.glucose_after_lunch
import com.velkyss.diabem.app.resources.glucose_after_lunch_short
import com.velkyss.diabem.app.resources.glucose_copy
import com.velkyss.diabem.app.resources.glucose_copy_help
import com.velkyss.diabem.app.resources.glucose_date
import com.velkyss.diabem.app.resources.glucose_day_next
import com.velkyss.diabem.app.resources.glucose_day_previous
import com.velkyss.diabem.app.resources.glucose_fasting
import com.velkyss.diabem.app.resources.glucose_fasting_short
import com.velkyss.diabem.app.resources.glucose_help
import com.velkyss.diabem.app.resources.glucose_history
import com.velkyss.diabem.app.resources.glucose_history_empty
import com.velkyss.diabem.app.resources.glucose_invalid
import com.velkyss.diabem.app.resources.glucose_notice
import com.velkyss.diabem.app.resources.glucose_title
import com.velkyss.diabem.app.resources.glucose_today
import com.velkyss.diabem.app.resources.plan_backup_copy_button
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.BigOutlinedButton
import com.velkyss.diabem.app.ui.components.NoticeBox
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.SectionTitle
import com.velkyss.diabem.app.ui.theme.LocalLightColors
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.core.profile.GlucoseLog
import com.velkyss.diabem.core.profile.GlucoseMoment
import com.velkyss.diabem.core.profile.LocalDay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val GlucoseMoment.label: StringResource
    get() = when (this) {
        GlucoseMoment.FASTING -> Res.string.glucose_fasting
        GlucoseMoment.AFTER_BREAKFAST -> Res.string.glucose_after_breakfast
        GlucoseMoment.AFTER_LUNCH -> Res.string.glucose_after_lunch
        GlucoseMoment.AFTER_DINNER -> Res.string.glucose_after_dinner
    }

private val GlucoseMoment.shortLabel: StringResource
    get() = when (this) {
        GlucoseMoment.FASTING -> Res.string.glucose_fasting_short
        GlucoseMoment.AFTER_BREAKFAST -> Res.string.glucose_after_breakfast_short
        GlucoseMoment.AFTER_LUNCH -> Res.string.glucose_after_lunch_short
        GlucoseMoment.AFTER_DINNER -> Res.string.glucose_after_dinner_short
    }

/** How many past days the history table shows. Older values stay stored and are in the copied text. */
private const val HISTORY_DAYS = 30

/**
 * The glucose diary of the nutritionist's sheet. It records and shows numbers only:
 * no colours, no "high/low", no advice about insulin or medicine.
 */
@Composable
fun GlucoseScreen(controller: AppController, onBack: () -> Unit) {
    val today = controller.today()
    var dayEpoch by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val day = LocalDay.fromEpochDay(dayEpoch)
    var showCopy by rememberSaveable { mutableStateOf(false) }
    val log = controller.glucoseLog

    ScreenScaffold(title = stringResource(Res.string.glucose_title), onBack = onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(pad), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.glucose_help), style = MaterialTheme.typography.bodyLarge)

            // Day selector: never later than today.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(onClick = { dayEpoch -= 1 }, modifier = Modifier.size(MinTouch)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(Res.string.glucose_day_previous))
                }
                Column(Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day.display, style = MaterialTheme.typography.headlineSmall)
                    if (day == today) Text(stringResource(Res.string.glucose_today), style = MaterialTheme.typography.bodyMedium)
                }
                FilledIconButton(onClick = { dayEpoch += 1 }, enabled = day < today, modifier = Modifier.size(MinTouch)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(Res.string.glucose_day_next))
                }
            }

            GlucoseMoment.entries.forEach { moment ->
                ValueField(
                    label = stringResource(moment.label),
                    stored = log.valuesFor(day)[moment],
                    key = "${day.iso}-${moment.name}",
                    onSave = { controller.setGlucose(day, moment, it) },
                )
            }

            NoticeBox(stringResource(Res.string.glucose_notice))

            SectionTitle(stringResource(Res.string.glucose_history))
            if (log.isEmpty) {
                Text(stringResource(Res.string.glucose_history_empty), style = MaterialTheme.typography.bodyMedium)
            } else {
                HistoryTable(log)
                BigOutlinedButton(stringResource(Res.string.glucose_copy), { showCopy = true }, icon = Icons.Outlined.ContentCopy)
            }
        }
    }

    if (showCopy) {
        val clipboard = LocalClipboardManager.current
        val labels = GlucoseMoment.entries.associateWith { stringResource(it.label) }
        val text = log.toShareText(stringResource(Res.string.glucose_title)) { labels.getValue(it) }
        AlertDialog(
            onDismissRequest = { showCopy = false },
            title = { Text(stringResource(Res.string.glucose_copy)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.glucose_copy_help), style = MaterialTheme.typography.bodyMedium)
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
}

/** One number field. It saves as the person types; an empty field removes the value. */
@Composable
private fun ValueField(label: String, stored: Int?, key: String, onSave: (Int?) -> Boolean) {
    var text by rememberSaveable(key) { mutableStateOf(stored?.toString() ?: "") }
    val number = text.toIntOrNull()
    val invalid = text.isNotEmpty() && (number == null || number !in GlucoseLog.MIN_VALUE..GlucoseLog.MAX_VALUE)
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { typed ->
                text = typed.filter { it.isDigit() }.take(3)
                val value = text.toIntOrNull()
                if (text.isEmpty()) onSave(null) else if (value != null && value in GlucoseLog.MIN_VALUE..GlucoseLog.MAX_VALUE) onSave(value)
            },
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            suffix = { Text(GlucoseLog.UNIT) },
            singleLine = true,
            isError = invalid,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().heightIn(min = MinTouch),
        )
        if (invalid) {
            Text(
                stringResource(Res.string.glucose_invalid, GlucoseLog.MIN_VALUE, GlucoseLog.MAX_VALUE),
                color = LocalLightColors.current.red,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** The same table as the paper sheet: one row per day, one column per moment. */
@Composable
private fun HistoryTable(log: GlucoseLog) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Bottom) {
                Text(stringResource(Res.string.glucose_date), style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp).semantics { heading() })
                GlucoseMoment.entries.forEach {
                    Text(stringResource(it.shortLabel), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            log.days.take(HISTORY_DAYS).forEach { day ->
                HorizontalDivider()
                val values = log.valuesFor(day)
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(day.shortDisplay, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.width(64.dp))
                    GlucoseMoment.entries.forEach { moment ->
                        Text(values[moment]?.toString() ?: "–", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
