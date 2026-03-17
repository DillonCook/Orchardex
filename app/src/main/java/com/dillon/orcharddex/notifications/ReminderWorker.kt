package com.dillon.orcharddex.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dillon.orcharddex.OrchardDexApp
import com.dillon.orcharddex.data.repository.displayName

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val reminderId = inputData.getString(KEY_REMINDER_ID) ?: return Result.failure()
        val app = applicationContext as OrchardDexApp
        val reminder = app.container.database.reminderDao().getReminder(reminderId) ?: return Result.success()
        if (!reminder.enabled || reminder.completedAt != null) return Result.success()
        val treeLabel = reminder.treeId?.let { app.container.database.treeDao().getTree(it)?.displayName() }
        ReminderNotificationManager.notify(applicationContext, reminder, treeLabel)
        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
    }
}
