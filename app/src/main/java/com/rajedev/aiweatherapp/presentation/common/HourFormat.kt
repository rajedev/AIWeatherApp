package com.rajedev.aiweatherapp.presentation.common

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toLocalDateTime

private const val NOON_HOUR = 12

// Formats a forecast timestamp as a 12-hour local time label (e.g. "3 PM") in the PLACE's own
// timezone, not the device's - matters when viewing a city in a different timezone than the user.
@OptIn(ExperimentalTime::class)
fun formatHour(timestampMillis: Long, tzOffsetSeconds: Int): String {
    val zone = UtcOffset(seconds = tzOffsetSeconds).asTimeZone()
    val hour24 = Instant.fromEpochMilliseconds(timestampMillis).toLocalDateTime(zone).hour
    val amPm = if (hour24 < NOON_HOUR) "AM" else "PM"
    val hour12 = when {
        hour24 == 0 -> NOON_HOUR
        hour24 > NOON_HOUR -> hour24 - NOON_HOUR
        else -> hour24
    }
    return "$hour12 $amPm"
}
