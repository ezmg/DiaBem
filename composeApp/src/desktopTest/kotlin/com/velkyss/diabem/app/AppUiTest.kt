package com.velkyss.diabem.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
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
import com.velkyss.diabem.app.ui.WithAppLanguage
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

    /** The device is in Spanish on purpose: the app must still be all in the pack's language. */
    @BeforeTest
    fun deviceInSpanish() = Locale.setDefault(Locale.forLanguageTag("es-ES"))

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
                WithAppLanguage(controller.pack.locale) { AppContent(controller) }
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
            com.velkyss.diabem.app.ui.Screen.Ingredients(com.velkyss.diabem.core.model.MealSlot.LUNCH),
            com.velkyss.diabem.app.ui.Screen.Results(com.velkyss.diabem.core.model.MealSlot.LUNCH),
            com.velkyss.diabem.app.ui.Screen.Detail("caldo-verde-leve", null),
            com.velkyss.diabem.app.ui.Screen.Settings,
            com.velkyss.diabem.app.ui.Screen.Plan,
            com.velkyss.diabem.app.ui.Screen.FoodSearch,
            com.velkyss.diabem.app.ui.Screen.FoodDetail("53"),
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

    @Test
    fun moreRecipesButtonAndMoreCarbsOption() = runComposeUiTest {
        val controller = launch(newStore(withProfile = true))
        controller.toggleIngredient("ovo")
        controller.go(com.velkyss.diabem.app.ui.Screen.Results(com.velkyss.diabem.core.model.MealSlot.BREAKFAST))
        val meal = com.velkyss.diabem.core.model.MealType.BREAKFAST
        val all = controller.results(meal)
        val short = controller.shortListSize(meal)
        assertTrue(all.size > short, "the test needs more breakfast recipes than the short list")
        val firstHidden = all[short].recipe.recipe.name
        onAllNodesWithText(firstHidden).assertCountEquals(0)
        onNode(androidx.compose.ui.test.hasScrollAction()).performScrollToNode(hasText("Ver mais receitas"))
        onNodeWithText("Ver mais receitas").performClick()
        onNode(androidx.compose.ui.test.hasScrollAction()).performScrollToNode(hasText(firstHidden))
        onNodeWithText(firstHidden).assertExists()

        // "more carbohydrates first": the first card becomes the highest-carb recipe that is not red
        onNode(androidx.compose.ui.test.hasScrollAction()).performScrollToNode(hasText("Mostrar primeiro", substring = true))
        onNodeWithText("Mostrar primeiro as receitas com mais carboidratos").performClick()
        onNodeWithText("só se o seu médico ou nutricionista orientou", substring = true).assertExists()
        val top = controller.results(meal, moreCarbs = true).first()
        assertTrue(!top.health.relevantRed)
        onNodeWithText(top.recipe.recipe.name).assertExists()
    }

    @Test
    fun nutritionistPlanIsTypedSavedAndShownAtTheMeal() = runComposeUiTest {
        val store = newStore(withProfile = true)
        val controller = launch(store)
        val slot = com.velkyss.diabem.core.model.MealSlot.MORNING_SNACK
        onNodeWithText("Meu plano da nutricionista").performScrollTo().performClick()
        onNodeWithText("Copie do papel", substring = true).assertIsDisplayed()

        onNode(hasSetTextAction() and hasText("Nova opção para Lanche da manhã")).performScrollTo().performTextInput("1 fruta + 1 iogurte natural")
        // the "add" button right after the mid-morning field (2nd slot)
        onAllNodesWithText("Adicionar opção")[1].performScrollTo().performClick()
        assertEquals(listOf("1 fruta + 1 iogurte natural"), store.plan.optionsFor(slot))
        onNodeWithText("1 fruta + 1 iogurte natural").assertExists()

        // the person sees it first when choosing that meal
        onNodeWithText("Voltar").performClick()
        onNodeWithText("Lanche da manhã").performScrollTo().performClick()
        onNodeWithText("O que a sua nutricionista indicou").assertIsDisplayed()
        onNodeWithText("1 fruta + 1 iogurte natural").assertIsDisplayed()
        onNodeWithText("Quer outras ideias?").assertExists()

        // other meals without plan stay as before
        onNodeWithText("Voltar").performClick()
        onNodeWithText("Jantar").performScrollTo().performClick()
        onAllNodesWithText("O que a sua nutricionista indicou").assertCountEquals(0)
        onNodeWithText("O que você tem em casa?").assertExists()

        // and it can be deleted
        controller.back()
        controller.go(com.velkyss.diabem.app.ui.Screen.Plan)
        onNodeWithContentDescription("Apagar: 1 fruta + 1 iogurte natural").performScrollTo().performClick()
        assertTrue(store.plan.isEmpty)
    }

    @Test
    fun lookUpAFoodAndSeeTheLightChangeWithTheAmount() = runComposeUiTest {
        launch(newStore(withProfile = true))
        onNodeWithText("Consultar um alimento").performScrollTo().performClick()
        onNode(hasSetTextAction()).performTextInput("pao frances")
        onNodeWithText("Pão, trigo, francês").performClick()

        // default amount: 1 unit = 50 g -> 29 g of carbohydrate -> amber on the snack scale
        onNodeWithText("1 unidade (50 g)").assertIsSelected()
        onNodeWithText("29 g").assertExists()
        onNode(hasContentDescription("Carboidratos: médio")).assertExists()
        // bread has 648 mg of sodium per 100 g: red, and the profile has hypertension
        onNode(hasContentDescription("Sal: muito")).assertExists()
        onNodeWithText("passa do limite indicado para você", substring = true).assertExists()

        // half a unit: 15 g -> green; two units: 59 g -> red
        onNodeWithText("1/2 unidade (25 g)").performClick()
        onNode(hasContentDescription("Carboidratos: pouco")).assertExists()
        onNodeWithText("2 unidades (100 g)").performClick()
        onNode(hasContentDescription("Carboidratos: muito")).assertExists()
        onNodeWithText("59 g").assertExists()
        onNodeWithText("quem decide é o seu médico ou nutricionista", substring = true).assertExists()
    }

    @Test
    fun everydayFoodNameIsFound() = runComposeUiTest {
        launch(newStore(withProfile = true))
        onNodeWithText("Consultar um alimento").performScrollTo().performClick()
        onNode(hasSetTextAction()).performTextInput("goiabada")
        onAllNodesWithText("Goiabada").onFirst().performClick()
        onNodeWithText("Na Tabela TACO: Goiaba, doce", substring = true).assertExists()
        onNode(hasContentDescription("Carboidratos:", substring = true)).assertExists()
    }

    @Test
    fun planBackupCanBeCopiedAndRestored() = runComposeUiTest {
        val store = newStore(withProfile = true)
        val controller = launch(store)
        controller.addPlanOption(com.velkyss.diabem.core.model.MealSlot.BREAKFAST, "1 pão francês + café com leite")
        controller.go(com.velkyss.diabem.app.ui.Screen.Plan)

        onNodeWithText("Guardar uma cópia do plano").performScrollTo().performClick()
        onNodeWithText("[breakfast] Café da manhã", substring = true).assertExists()
        onAllNodesWithText("Voltar").onLast().performClick()

        // simulate a fresh install, then paste the saved text back
        val saved = store.plan.toBackupText { it.key }
        controller.removePlanOption(com.velkyss.diabem.core.model.MealSlot.BREAKFAST, 0)
        assertTrue(store.plan.isEmpty)
        onNodeWithText("Recuperar um plano guardado").performScrollTo().performClick()
        onNode(hasSetTextAction() and hasText("Texto do plano")).performTextInput(saved)
        onNodeWithText("Recuperar").performClick()
        assertEquals(listOf("1 pão francês + café com leite"), store.plan.optionsFor(com.velkyss.diabem.core.model.MealSlot.BREAKFAST))
        onNodeWithText("1 opção recuperada").assertExists()
    }
}
