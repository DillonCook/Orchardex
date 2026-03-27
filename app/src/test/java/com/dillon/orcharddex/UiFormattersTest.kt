package com.dillon.orcharddex

import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.localDateAtStartOfDay
import com.dillon.orcharddex.ui.toDateLabel
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Test

class UiFormattersTest {
    @Test
    fun toDateLabel_andLocalDateAtStartOfDay_useOrchardTimezone() {
        val originalZoneId = OrchardTime.zoneId().id
        try {
            OrchardTime.updateTimezoneId("Pacific/Auckland")
            val aucklandInstant = ZonedDateTime.of(2026, 3, 28, 0, 30, 0, 0, ZoneId.of("Pacific/Auckland"))
                .toInstant()
                .toEpochMilli()

            assertThat(aucklandInstant.toDateLabel()).isEqualTo("Mar 28, 2026")

            OrchardTime.updateTimezoneId("America/Los_Angeles")

            assertThat(aucklandInstant.toDateLabel()).isEqualTo("Mar 27, 2026")

            val startOfDay = localDateAtStartOfDay(LocalDate.of(2026, 3, 28))
            val losAngelesDate = Instant.ofEpochMilli(startOfDay)
                .atZone(ZoneId.of("America/Los_Angeles"))
                .toLocalDate()

            assertThat(losAngelesDate).isEqualTo(LocalDate.of(2026, 3, 28))
        } finally {
            OrchardTime.updateTimezoneId(originalZoneId)
        }
    }
}
