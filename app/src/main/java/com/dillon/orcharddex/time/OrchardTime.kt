package com.dillon.orcharddex.time

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId

object OrchardTime {
    @Volatile
    private var activeZoneId: ZoneId = ZoneId.systemDefault()

    fun zoneId(): ZoneId = activeZoneId

    fun updateTimezoneId(timezoneId: String?) {
        activeZoneId = runCatching {
            ZoneId.of(timezoneId.orEmpty().trim())
        }.getOrElse {
            ZoneId.systemDefault()
        }
    }

    fun today(): LocalDate = LocalDate.now(zoneId())

    fun currentYearMonth(): YearMonth = YearMonth.now(zoneId())

    fun currentYear(): Int = Year.now(zoneId()).value
}
