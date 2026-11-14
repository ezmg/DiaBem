package com.velkyss.diabem.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.velkyss.diabem.app.reminders.AppContextHolder
import com.velkyss.diabem.app.reminders.ReminderScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppContextHolder.context = applicationContext
        // Alarms are lost when the app is force-stopped; opening the app sets them again.
        ReminderScheduler.scheduleAll(applicationContext)
        setContent { App() }
    }
}
