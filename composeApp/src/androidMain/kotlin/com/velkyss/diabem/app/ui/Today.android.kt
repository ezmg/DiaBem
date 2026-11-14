package com.velkyss.diabem.app.ui

import com.velkyss.diabem.core.profile.LocalDay
import java.util.Calendar

actual fun today(): LocalDay = Calendar.getInstance().let {
    LocalDay(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
}
