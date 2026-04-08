package com.dillon.orcharddex

import android.app.Application
import android.os.StrictMode
import com.dillon.orcharddex.data.phenology.PhenologyCatalogAssets
import com.dillon.orcharddex.notifications.ReminderNotificationManager

class OrchardDexApp : Application() {
    lateinit var container: OrchardDexContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
        PhenologyCatalogAssets.initialize(this)
        container = OrchardDexContainer(this)
        container.diagnosticsStore.installCrashHandler()
        ReminderNotificationManager.createChannel(this)
    }
}
