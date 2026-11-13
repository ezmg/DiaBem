package com.velkyss.diabem.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.russhwolf.settings.MapSettings
import com.velkyss.diabem.app.data.PackLoader
import com.velkyss.diabem.app.data.UserStore
import com.velkyss.diabem.app.ui.AppController
import com.velkyss.diabem.app.ui.Screen
import com.velkyss.diabem.app.ui.WithAppLanguage
import com.velkyss.diabem.app.ui.theme.DiaBemTheme
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.HealthProfile
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.Test

/**
 * Generates the phone screenshots for the store listing (1080 x 1920).
 * Only runs when asked: ./gradlew :composeApp:desktopTest -Pdiabem.screenshots=docs/store/screenshots
 */
@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
class StoreScreenshots {
    private val outDir: File? = System.getProperty("diabem.screenshots")?.takeIf { it.isNotBlank() }?.let(::File)

    private fun shot(name: String, prepare: (AppController) -> Unit) = runSkikoComposeUiTest(
        size = Size(1080f, 1920f),
        density = Density(3f),
    ) {
        val store = UserStore(MapSettings()).apply {
            noticeAccepted = true
            profile = HealthProfile(setOf(Condition.DIABETES_TYPE_2, Condition.HYPERTENSION))
        }
        val controller = AppController(runBlocking { PackLoader.load() }, store)
        prepare(controller)
        setContent {
            DiaBemTheme(highContrast = false, textScale = 1f) {
                WithAppLanguage(controller.pack.locale) { AppContent(controller) }
            }
        }
        waitForIdle()
        val bitmap = onRoot().captureToImage().asSkiaBitmap()
        val png = Image.makeFromBitmap(bitmap).encodeToData(EncodedImageFormat.PNG)!!.bytes
        File(outDir!!.apply { mkdirs() }, "$name.png").writeBytes(png)
    }

    @Test
    fun generate() {
        if (outDir == null) return
        val kitchen = listOf("arroz", "feijao", "ovo", "tomate", "cebola", "alho", "frango", "alface")
        shot("1-inicio") {}
        shot("2-ingredientes") { c -> kitchen.take(3).forEach(c::toggleIngredient); c.go(Screen.Ingredients(MealSlot.LUNCH)) }
        shot("3-receitas") { c -> kitchen.forEach(c::toggleIngredient); c.go(Screen.Results(MealSlot.LUNCH)) }
        shot("4-receita") { c -> c.go(Screen.Detail("arroz-feijao-e-ovo", null)) }
        shot("5-meu-plano") { c ->
            c.addPlanOption(MealSlot.BREAKFAST, "1 pão francês com queijo branco + café com leite sem açúcar")
            c.addPlanOption(MealSlot.MORNING_SNACK, "1 fruta")
            c.go(Screen.Ingredients(MealSlot.BREAKFAST))
        }
        shot("6-consultar-alimento") { c -> c.go(Screen.FoodDetail("182")) }
    }
}
