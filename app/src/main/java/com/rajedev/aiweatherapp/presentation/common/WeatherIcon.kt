package com.rajedev.aiweatherapp.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.rajedev.aiweatherapp.presentation.theme.resolveMood
import com.rajedev.aiweatherapp.presentation.theme.toThemeColors
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toLocalDateTime

// Independent of the app-wide weather mood - a single hourly/daily slice must reflect its own
// actual condition, not the current screen's overall theme mood.
fun weatherIconFor(conditionMain: String): ImageVector = when (conditionMain) {
    "Clear" -> Icons.Filled.WbSunny
    "Clouds" -> Icons.Filled.Cloud
    "Rain", "Drizzle" -> Icons.Filled.Umbrella
    "Thunderstorm" -> Icons.Filled.Bolt
    "Snow" -> Icons.Filled.AcUnit
    else -> Icons.Filled.WaterDrop
}

// Per-item tint for list rows (hourly/daily/saved-city cards) where each item can represent a
// DIFFERENT condition than the screen's single global WeatherAdaptiveTheme mood. Deliberately
// does NOT read LocalWeatherTheme - that's the screen-level mood - this derives through the same
// resolveMood()/toThemeColors() pipeline so colors stay consistent everywhere.
fun weatherTintFor(conditionMain: String, temp: Double, localHour: Int): Color =
    resolveMood(conditionMain, temp, localHour).toThemeColors().primary

@OptIn(ExperimentalTime::class)
fun localHourFrom(timestampMillis: Long, tzOffsetSeconds: Int): Int {
    val zone = UtcOffset(seconds = tzOffsetSeconds).asTimeZone()
    return Instant.fromEpochMilliseconds(timestampMillis).toLocalDateTime(zone).hour
}
