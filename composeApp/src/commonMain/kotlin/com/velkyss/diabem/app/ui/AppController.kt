package com.velkyss.diabem.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.velkyss.diabem.app.data.DisplayPrefs
import com.velkyss.diabem.app.data.UserStore
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.pack.CountryPack
import com.velkyss.diabem.core.profile.HealthProfile
import com.velkyss.diabem.core.search.RecipeMatch
import com.velkyss.diabem.core.search.RecipeSearch
import com.velkyss.diabem.core.search.SearchQuery

sealed interface Screen {
    data object Notice : Screen
    data class Profile(val firstTime: Boolean) : Screen
    data object Home : Screen
    data class Ingredients(val meal: MealType) : Screen
    data class Results(val meal: MealType) : Screen
    data class Detail(val recipeId: String, val meal: MealType?) : Screen
    data object Favorites : Screen
    data object Settings : Screen
}

/** Holds the app state and the simple back stack. No business logic lives here: see core. */
class AppController(val pack: CountryPack, private val store: UserStore) {
    private val search = RecipeSearch(pack)

    var profile by mutableStateOf(store.profile)
        private set
    var favorites by mutableStateOf(store.favorites)
        private set
    var display by mutableStateOf(store.displayPrefs)
        private set

    /** Ingredients ticked by the person in this session. */
    var selected by mutableStateOf(emptySet<String>())
        private set

    private val stack = mutableStateListOf(initialScreen())
    val screen: Screen get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    private fun initialScreen(): Screen = when {
        !store.noticeAccepted -> Screen.Notice
        profile == null -> Screen.Profile(firstTime = true)
        else -> Screen.Home
    }

    fun go(screen: Screen) { stack.add(screen) }

    fun back() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    private fun resetTo(screen: Screen) { stack.clear(); stack.add(screen) }

    fun acceptNotice() {
        store.noticeAccepted = true
        resetTo(if (profile == null) Screen.Profile(firstTime = true) else Screen.Home)
    }

    fun saveProfile(newProfile: HealthProfile) {
        store.profile = newProfile
        profile = newProfile
        if (screen is Screen.Profile && (screen as Screen.Profile).firstTime) resetTo(Screen.Home) else back()
    }

    fun toggleIngredient(id: String) {
        selected = if (id in selected) selected - id else selected + id
    }

    fun clearIngredients() { selected = emptySet() }

    fun results(meal: MealType): List<RecipeMatch> {
        val p = profile ?: return emptyList()
        return search.search(SearchQuery(meal, selected, p))
    }

    fun match(recipeId: String, meal: MealType?): RecipeMatch? {
        val p = profile ?: return null
        val recipe = pack.recipes.firstOrNull { it.id == recipeId } ?: return null
        return search.match(recipe, SearchQuery(meal ?: recipe.recipe.mealTypes.first(), selected, p))
    }

    fun isFavorite(id: String) = id in favorites

    fun toggleFavorite(id: String) {
        favorites = if (id in favorites) favorites - id else favorites + id
        store.favorites = favorites
    }

    fun updateDisplay(prefs: DisplayPrefs) {
        display = prefs
        store.displayPrefs = prefs
    }

    /** Erases everything stored on the device and starts again. */
    fun eraseAll() {
        store.clearAll()
        profile = null
        favorites = emptySet()
        display = DisplayPrefs()
        selected = emptySet()
        resetTo(Screen.Notice)
    }

    fun pantryName(id: String) = pack.pantry.firstOrNull { it.id == id }?.name ?: id
}
