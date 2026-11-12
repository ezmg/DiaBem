package com.velkyss.diabem.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.app_name
import com.velkyss.diabem.app.resources.favorites
import com.velkyss.diabem.app.resources.home_title
import com.velkyss.diabem.app.resources.medical_notice
import com.velkyss.diabem.app.resources.settings
import com.velkyss.diabem.app.ui.components.BigButton
import com.velkyss.diabem.app.ui.components.BigOutlinedButton
import com.velkyss.diabem.app.ui.components.NoticeBox
import com.velkyss.diabem.app.ui.components.ScreenScaffold
import com.velkyss.diabem.app.ui.components.SectionTitle
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.app.resources.plan_button
import androidx.compose.material.icons.outlined.Assignment
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(onMeal: (MealSlot) -> Unit, onPlan: () -> Unit, onFavorites: () -> Unit, onSettings: () -> Unit) {
    ScreenScaffold(title = stringResource(Res.string.app_name), onBack = null) { pad ->
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(pad),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(stringResource(Res.string.home_title))
            MealSlot.entries.forEach { meal ->
                BigButton(stringResource(meal.label), onClick = { onMeal(meal) }, icon = meal.icon)
            }
            BigOutlinedButton(stringResource(Res.string.plan_button), onPlan, icon = Icons.Outlined.Assignment)
            BigOutlinedButton(stringResource(Res.string.favorites), onFavorites, icon = Icons.Outlined.StarOutline)
            BigOutlinedButton(stringResource(Res.string.settings), onSettings, icon = Icons.Outlined.Settings)
            NoticeBox(stringResource(Res.string.medical_notice))
        }
    }
}
