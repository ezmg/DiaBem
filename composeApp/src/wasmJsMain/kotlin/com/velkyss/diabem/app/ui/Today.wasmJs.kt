package com.velkyss.diabem.app.ui

import com.velkyss.diabem.core.profile.LocalDay

private fun jsYear(): Int = js("new Date().getFullYear()")

private fun jsMonth(): Int = js("new Date().getMonth() + 1")

private fun jsDay(): Int = js("new Date().getDate()")

actual fun today(): LocalDay = LocalDay(jsYear(), jsMonth(), jsDay())
