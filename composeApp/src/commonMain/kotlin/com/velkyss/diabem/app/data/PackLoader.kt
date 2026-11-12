package com.velkyss.diabem.app.data

import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.core.AppInfo
import com.velkyss.diabem.core.pack.CountryPack
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Loads the country pack bundled with the app (generated at build time from data/). */
object PackLoader {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(country: String = AppInfo.DEFAULT_COUNTRY): CountryPack =
        CountryPack.decode(Res.readBytes("files/packs/$country.json").decodeToString())
}
