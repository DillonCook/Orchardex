package com.dillon.orcharddex.ui

import com.dillon.orcharddex.time.OrchardTime
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val decimalFormatter = DecimalFormat("#.##")

fun Long.toDateLabel(): String = Instant.ofEpochMilli(this)
    .atZone(OrchardTime.zoneId())
    .toLocalDate()
    .format(dateFormatter)

fun Long.toDateTimeLabel(): String {
    val zoned = Instant.ofEpochMilli(this).atZone(OrchardTime.zoneId())
    return "${zoned.toLocalDate().format(dateFormatter)} • ${zoned.toLocalTime().format(timeFormatter)}"
}

fun Long.toTimeLabel(): String = Instant.ofEpochMilli(this)
    .atZone(OrchardTime.zoneId())
    .toLocalTime()
    .format(timeFormatter)

fun localDateAtStartOfDay(date: LocalDate): Long = date
    .atStartOfDay(OrchardTime.zoneId())
    .toInstant()
    .toEpochMilli()

fun localDateWithTime(date: LocalDate, time: LocalTime): Long = date
    .atTime(time)
    .atZone(OrchardTime.zoneId())
    .toInstant()
    .toEpochMilli()

fun epochToLocalDate(epochMillis: Long): LocalDate = Instant.ofEpochMilli(epochMillis)
    .atZone(OrchardTime.zoneId())
    .toLocalDate()

fun epochToLocalTime(epochMillis: Long): LocalTime = Instant.ofEpochMilli(epochMillis)
    .atZone(OrchardTime.zoneId())
    .toLocalTime()

fun Double.displayAmount(): String = decimalFormatter.format(this)
