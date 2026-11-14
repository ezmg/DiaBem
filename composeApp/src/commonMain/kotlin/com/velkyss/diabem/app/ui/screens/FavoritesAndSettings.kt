package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import com.velkyss.diabem.app.resources.reminders_title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.about_sources
import com.velkyss.diabem.app.resources.back
import com.velkyss.diabem.app.resources.edit_profile
import com.velkyss.diabem.app.resources.erase_data
import com.velkyss.diabem.app.resources.favorites
import com.velkyss.diabem.app.resources.favorites_empty
import com.velkyss.diabem.app.resources.high_contrast
import com.velkyss.diabem.app.resources.settings
import com.velkyss.diabem.app.resources.text_size
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.components.BigOutlinedButton
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.SectionTitle
import com.velkyss.diabem.app.ui.theme.MinTouch
import com.velkyss.diabem.app.data.DisplayPrefs
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoritesScreen(controller: AppController, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val favorites = controller.favorites
    val recipes = controller.pack.recipes.filter { it.id in favorites }
    ScreenScaffold(title = stringResource(Res.string.favorites), onBack = onBack) { pad ->
        LazyColumn(contentPadding = pad, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (recipes.isEmpty()) item { Text(stringResource(Res.string.favorites_empty), style = MaterialTheme.typography.bodyLarge) }
            items(recipes, key = { it.id }) { r ->
                val match = controller.match(r.id, null) ?: return@items
                RecipeCard(controller, match, null) { onOpen(r.id) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(controller: AppController, onBack: () -> Unit, onEditProfile: () -> Unit, onReminders: () -> Unit) {
    var confirmErase by remember { mutableStateOf(false) }
    val prefs = controller.display
    ScreenScaffold(title = stringResource(Res.string.settings), onBack = onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(pad), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionTitle(stringResource(Res.string.text_size))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DisplayPrefs.TEXT_SCALES.forEach { scale ->
                    FilterChip(
                        selected = prefs.textScale == scale,
                        onClick = { controller.updateDisplay(prefs.copy(textScale = scale)) },
                        label = { Text("${(scale * 100).toInt()}%", style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.heightIn(min = MinTouch),
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = MinTouch)
                    .toggleable(value = prefs.highContrast, role = Role.Switch, onValueChange = { controller.updateDisplay(prefs.copy(highContrast = it)) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(Res.string.high_contrast), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Switch(checked = prefs.highContrast, onCheckedChange = null)
            }
            BigOutlinedButton(stringResource(Res.string.edit_profile), onEditProfile, icon = Icons.Outlined.Person)
            if (controller.reminderPlatform.supported) {
                BigOutlinedButton(stringResource(Res.string.reminders_title), onReminders, icon = Icons.Outlined.Notifications)
            }

            SectionTitle(stringResource(Res.string.about_sources))
            controller.pack.sources.filter { it.url != null }.forEach {
                Text("• ${it.citation}", style = MaterialTheme.typography.bodyMedium)
            }
            BigOutlinedButton(stringResource(Res.string.erase_data), { confirmErase = true }, icon = Icons.Outlined.DeleteForever)
        }
    }
    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text(stringResource(Res.string.erase_data)) },
            confirmButton = {
                TextButton(onClick = { confirmErase = false; controller.eraseAll() }, modifier = Modifier.heightIn(min = MinTouch)) {
                    Text(stringResource(Res.string.erase_data), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmErase = false }, modifier = Modifier.heightIn(min = MinTouch)) {
                    Text(stringResource(Res.string.back), style = MaterialTheme.typography.labelLarge)
                }
            },
        )
    }
}
