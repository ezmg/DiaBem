package com.velkyss.diabem.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.russhwolf.settings.MapSettings
import com.velkyss.diabem.app.data.PackLoader
import com.velkyss.diabem.app.data.UserStore
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.theme.DiaBemTheme
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.CustomLimits
import com.velkyss.diabem.core.profile.HealthProfile
import kotlinx.coroutines.runBlocking
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** UI tests of the main screens, with the real Brazilian pack and Brazilian Portuguese texts. */
@OptIn(ExperimentalTestApi::class)
class AppUiTest {
    private val pack = runBlocking { PackLoader.load() }

    @BeforeTest
    fun usePortuguese() = Locale.setDefault(Locale.forLanguageTag("pt-BR"))

    private fun newStore(withProfile: Boolean, limits: CustomLimits = CustomLimits()) = UserStore(MapSettings()).apply {
        if (withProfile) {
            noticeAccepted = true
            profile = HealthProfile(setOf(Condition.DIABETES_TYPE_2, Condition.HYPERTENSION), limits)
        }
    }

    private fun ComposeUiTest.launch(store: UserStore): AppController {
        val controller = AppController(pack, store)
        setContent {
            DiaBemTheme(highContrast = controller.display.highContrast, textScale = controller.display.textScale) {
                AppContent(controller)
            }
        }
        return controller
    }

    @Test
    fun firstUseShowsMedicalNoticeThenProfile() = runComposeUiTest {
        val store = newStore(withProfile = false)
        launch(store)
        onNodeWithText("Antes de começar").assertIsDisplayed()
        onNodeWithText("nunca calcula doses de insulina", substring = true).assertIsDisplayed()
        onNodeWithText("Entendi").performScrollTo().performClick()

        onNodeWithText("O que você tem?").assertIsDisplayed()
        onNodeWithText("Salvar").performScrollTo().assertIsNotEnabled()
        onNodeWithText("Pressão alta").performScrollTo().performClick()
        onNodeWithText("Pressão alta").assertIsSelected()
        onNodeWithText("Salvar").performScrollTo().performClick()

        onNodeWithText("O que você vai preparar?").assertIsDisplayed()
        assertTrue(store.noticeAccepted)
        assertEquals(setOf(Condition.HYPERTENSION), store.profile?.conditions)
    }

    @Test
    fun nutritionistLimitsAreSavedOnTheDevice() = runComposeUiTest {
        val store = newStore(withProfile = false).apply { noticeAccepted = true }
        launch(store)
        onNodeWithText("Diabetes tipo 2").performClick()
        onNode(hasSetTextAction() and hasText("Máximo de carboidratos", substring = true)).performScrollTo().performTextInput("40")
        onNode(hasSetTextAction() and hasText("Máximo de colheres", substring = true)).performScrollTo().performTextInput("2")
        onNodeWithText("Salvar").performScrollTo().performClick()
        assertEquals(CustomLimits(maxCarbsPerMealG = 40.0, maxRiceSpoonsPerMeal = 2), store.profile?.customLimits)
    }

    @Test
    fun pickIngredientsAndSeeRecipesWithTrafficLights() = runComposeUiTest {
        val controller = launch(newStore(withProfile = true))
        onNodeWithText("Café da manhã").performClick()
        onNodeWithText("O que você tem em casa?").assertIsDisplayed()

        onNode(hasSetTextAction()).performTextInput("ovo")
        onNodeWithText("Ovos").performClick()
        assertEquals(setOf("ovo"), controller.selected)
        onNodeWithText("1 ingrediente marcado").assertIsDisplayed()

        onNodeWithText("Ver receitas").performClick()
        onNodeWithText("Receitas para você").assertIsDisplayed()
        // Traffic lights are announced with words, not only colour.
        onAllNodes(hasContentDescription("Carboidratos:", substring = true)).onFirst().assertExists()
        onAllNodes(hasContentDescription("Sal:", substring = true)).onFirst().assertExists()
        onAllNodesWithText("Ainda não revisada por nutricionista").onFirst().assertExists()
    }

    @Test
    fun recipeDetailShowsMeasuresStepsNutrientsAndScalesServings() = runComposeUiTest {
        val controller = launch(newStore(withProfile = true))
        controller.go(com.velkyss.diabem.app.ui.Screen.Detail("feijao-caseiro", null))
        onNodeWithText("Feijão caseiro com pouco sal").assertIsDisplayed()
        onNodeWithText("6 conchas de feijão carioca cozido", substring = true).assertExists()
        onNodeWithText("Passo 1").assertExists()
        onNodeWithText("Em cada porção").assertExists()
        onNodeWithText("Carboidratos").assertExists()
        onNodeWithText("Valores calculados com a Tabela TACO", substring = true).assertExists()
        onNodeWithText("não substituem seu médico", substring = true).assertExists()

        // 4 servings -> 2 servings: 6 ladles become 3
        onNodeWithContentDescription("Menos uma porção").performScrollTo().performClick()
        onNodeWithContentDescription("Menos uma porção").performClick()
        onNodeWithText("3 conchas de feijão carioca cozido", substring = true).assertExists()
    }

    @Test
    fun riceLimitFromNutritionistShowsAWarning() = runComposeUiTest {
        val controller = launch(newStore(withProfile = true, limits = CustomLimits(maxRiceSpoonsPerMeal = 2)))
        controller.go(com.velkyss.diabem.app.ui.Screen.Detail("prato-feito-com-frango-grelhado", null))
        onNodeWithText("Seu limite é de 2 colheres de arroz", substring = true).assertExists()
    }

    @Test
    fun favoritesAreSavedAndListed() = runComposeUiTest {
        val store = newStore(withProfile = true)
        val controller = launch(store)
        controller.go(com.velkyss.diabem.app.ui.Screen.Detail("salada-de-frutas", null))
        onNodeWithText("Salvar nas favoritas").performScrollTo().performClick()
        assertEquals(setOf("salada-de-frutas"), store.favorites)
        onNodeWithText("Tirar das favoritas").assertExists()

        onNodeWithText("Voltar").performClick()
        onNodeWithText("Minhas favoritas").performScrollTo().performClick()
        onNodeWithText("Salada de frutas").assertIsDisplayed()
    }

    @Test
    fun settingsChangeTextSizeAndContrastAndEraseData() = runComposeUiTest {
        val store = newStore(withProfile = true)
        val controller = launch(store)
        onNodeWithText("Ajustes").performScrollTo().performClick()
        onNodeWithText("130%").performClick()
        assertEquals(1.3f, store.displayPrefs.textScale)
        onNodeWithText("Alto contraste").performClick()
        onNodeWithText("Alto contraste").assertIsOn()
        assertTrue(store.displayPrefs.highContrast)

        onNodeWithText("Apagar meus dados deste aparelho").performScrollTo().performClick()
        // confirm in the dialog (second node with the same text)
        onAllNodesWithText("Apagar meus dados deste aparelho")[2].performClick()
        assertEquals(null, store.profile)
        assertEquals(null, controller.profile)
        onNodeWithText("Antes de começar").assertIsDisplayed()
    }

    @Test
    fun everyClickableElementHasANameForScreenReaders() = runComposeUiTest {
        val controller = launch(newStore(withProfile = true))
        val screens = listOf(
            com.velkyss.diabem.app.ui.Screen.Home,
            com.velkyss.diabem.app.ui.Screen.Ingredients(com.velkyss.diabem.core.model.MealType.LUNCH),
            com.velkyss.diabem.app.ui.Screen.Results(com.velkyss.diabem.core.model.MealType.LUNCH),
            com.velkyss.diabem.app.ui.Screen.Detail("caldo-verde-leve", null),
            com.velkyss.diabem.app.ui.Screen.Settings,
        )
        for (screen in screens) {
            controller.go(screen)
            waitForIdle()
            val clickable = onAllNodes(SemanticsMatcher("clickable") { it.config.contains(androidx.compose.ui.semantics.SemanticsActions.OnClick) }, useUnmergedTree = false)
            val nodes = clickable.fetchSemanticsNodes()
            assertTrue(nodes.isNotEmpty(), "no clickable nodes on $screen")
            nodes.forEach { node ->
                val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
                val description = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                val editable = node.config.getOrNull(SemanticsProperties.EditableText)
                assertNotNull((text?.takeIf { it.isNotBlank() } ?: description?.takeIf { it.isNotBlank() } ?: editable), "unnamed clickable element on $screen")
            }
        }
    }
}
