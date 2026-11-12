package com.velkyss.diabem.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.velkyss.diabem.app.data.PackLoader
import com.velkyss.diabem.app.data.UserStore
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.PlatformBackHandler
import com.velkyss.diabem.app.ui.Screen
import com.velkyss.diabem.app.ui.WithAppLanguage
import com.velkyss.diabem.app.ui.screens.DetailScreen
import com.velkyss.diabem.app.ui.screens.FavoritesScreen
import com.velkyss.diabem.app.ui.screens.HomeScreen
import com.velkyss.diabem.app.ui.screens.IngredientsScreen
import com.velkyss.diabem.app.ui.screens.NoticeScreen
import com.velkyss.diabem.app.ui.screens.PlanScreen
import com.velkyss.diabem.app.ui.screens.ProfileScreen
import com.velkyss.diabem.app.ui.screens.ResultsScreen
import com.velkyss.diabem.app.ui.screens.SettingsScreen
import com.velkyss.diabem.app.ui.theme.DiaBemTheme

@Composable
fun App(store: UserStore = remember { UserStore() }) {
    var controller by remember { mutableStateOf<AppController?>(null) }
    LaunchedEffect(Unit) { controller = AppController(PackLoader.load(), store) }

    val c = controller
    val prefs = c?.display ?: store.displayPrefs
    DiaBemTheme(highContrast = prefs.highContrast, textScale = prefs.textScale) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (c == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                WithAppLanguage(c.pack.locale) { AppContent(c) }
            }
        }
    }
}

@Composable
fun AppContent(c: AppController) {
    PlatformBackHandler(enabled = c.canGoBack) { c.back() }
    when (val s = c.screen) {
        Screen.Notice -> NoticeScreen(onAccept = c::acceptNotice)
        is Screen.Profile -> ProfileScreen(
            initial = c.profile,
            onSave = c::saveProfile,
            onBack = if (s.firstTime) null else c::back,
        )
        Screen.Home -> HomeScreen(
            onMeal = { c.go(Screen.Ingredients(it)) },
            onPlan = { c.go(Screen.Plan) },
            onFavorites = { c.go(Screen.Favorites) },
            onSettings = { c.go(Screen.Settings) },
        )
        is Screen.Ingredients -> IngredientsScreen(c, s.slot, onBack = c::back, onSeeRecipes = { c.go(Screen.Results(s.slot)) })
        is Screen.Results -> ResultsScreen(c, s.slot, onBack = c::back, onOpen = { c.go(Screen.Detail(it, s.slot.mealType)) })
        is Screen.Detail -> DetailScreen(c, s.recipeId, s.meal, onBack = c::back)
        Screen.Favorites -> FavoritesScreen(c, onBack = c::back, onOpen = { c.go(Screen.Detail(it, null)) })
        Screen.Plan -> PlanScreen(c, onBack = c::back)
        Screen.Settings -> SettingsScreen(c, onBack = c::back, onEditProfile = { c.go(Screen.Profile(firstTime = false)) })
    }
}
