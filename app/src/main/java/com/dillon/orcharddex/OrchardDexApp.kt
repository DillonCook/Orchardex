package com.dillon.orcharddex

import android.app.Application
import com.dillon.orcharddex.notifications.ReminderNotificationManager

class OrchardDexApp : Application() {
    lateinit var container: OrchardDexContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = OrchardDexContainer(this)
        ReminderNotificationManager.createChannel(this)
    }
}
