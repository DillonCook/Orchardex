package com.dillon.orcharddex.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.model.LeadTimeMode
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    fun schedule(reminder: ReminderEntity) {
        if (!reminder.enabled || reminder.completedAt != null) {
            cancel(reminder.id)
            return
        }
        val notifyAt = reminder.notificationAtMillis()
        val delayMs = (notifyAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_REMINDER_ID to reminder.id))
            .build()
        workManager.enqueueUniqueWork(uniqueWorkName(reminder.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(reminderId: String) {
        workManager.cancelUniqueWork(uniqueWorkName(reminderId))
    }

    fun uniqueWorkName(reminderId: String): String = "reminder_$reminderId"
}

fun ReminderEntity.notificationAtMillis(): Long {
    val offsetMillis = when (leadTimeMode) {
        LeadTimeMode.SAME_DAY -> 0L
        LeadTimeMode.ONE_DAY_BEFORE -> TimeUnit.DAYS.toMillis(1)
        LeadTimeMode.CUSTOM_HOURS -> TimeUnit.HOURS.toMillis((customLeadTimeHours ?: 6).toLong())
    }
    return dueAt - offsetMillis
}
