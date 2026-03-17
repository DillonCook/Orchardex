package com.dillon.orcharddex

import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.notifications.notificationAtMillis
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderSchedulerHelperTest {
    @Test
    fun notificationAtMillis_respectsOneDayLead() {
        val reminder = ReminderEntity(
            id = "r1",
            treeId = null,
            title = "Prune guava",
            notes = "",
            dueAt = 1_800_000_000_000,
            hasTime = false,
            recurrenceType = RecurrenceType.NONE,
            recurrenceIntervalDays = null,
            enabled = true,
            completedAt = null,
            leadTimeMode = LeadTimeMode.ONE_DAY_BEFORE,
            customLeadTimeHours = null,
            createdAt = 0L,
            updatedAt = 0L
        )

        assertThat(reminder.notificationAtMillis()).isEqualTo(1_799_913_600_000)
    }

    @Test
    fun notificationAtMillis_respectsCustomHours() {
        val reminder = ReminderEntity(
            id = "r2",
            treeId = null,
            title = "Fertilize",
            notes = "",
            dueAt = 1_800_000_000_000,
            hasTime = true,
            recurrenceType = RecurrenceType.NONE,
            recurrenceIntervalDays = null,
            enabled = true,
            completedAt = null,
            leadTimeMode = LeadTimeMode.CUSTOM_HOURS,
            customLeadTimeHours = 6,
            createdAt = 0L,
            updatedAt = 0L
        )

        assertThat(reminder.notificationAtMillis()).isEqualTo(1_799_978_400_000)
    }
}
