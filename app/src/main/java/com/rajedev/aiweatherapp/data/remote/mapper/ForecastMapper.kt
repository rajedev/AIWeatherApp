@file:OptIn(ExperimentalTime::class)

package com.rajedev.aiweatherapp.data.remote.mapper

import com.rajedev.aiweatherapp.data.remote.dto.ForecastEntryDto
import com.rajedev.aiweatherapp.data.remote.dto.ForecastResponseDto
import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toLocalDateTime

private const val MILLIS_PER_SECOND = 1000L

// A full day at 3h intervals is 8 slices; require at least 6 (~18h coverage) for a boundary day
// to be considered "shown" - drops partial first/last days consistently regardless of sync time.
private const val MIN_SLICES_FOR_FULL_DAY = 6

fun ForecastResponseDto.toHourly(): List<HourlyForecast> =
    list.orEmpty().mapNotNull { entry ->
        val dt = entry.dt ?: return@mapNotNull null
        val temp = entry.main?.temp ?: return@mapNotNull null
        val condition = entry.weather?.firstOrNull()
        HourlyForecast(
            timestamp = dt * MILLIS_PER_SECOND,
            temp = temp,
            conditionMain = condition?.main ?: "Clear",
            conditionIcon = condition?.icon.orEmpty(),
        )
    }

fun List<ForecastEntryDto>.toDailyBuckets(tzOffsetSeconds: Int): List<DailyForecast> {
    val zone = UtcOffset(seconds = tzOffsetSeconds).asTimeZone()
    return mapNotNull { entry -> entry.dt?.let { dt -> Instant.fromEpochSeconds(dt) to entry } }
        .groupBy { (instant, _) -> instant.toLocalDateTime(zone).date }
        .filterValues { it.size >= MIN_SLICES_FOR_FULL_DAY }
        .map { (date, dayEntries) -> buildDailyForecast(date, dayEntries.map { it.second }) }
        .sortedBy { it.date }
}

private fun buildDailyForecast(
    date: kotlinx.datetime.LocalDate,
    entries: List<ForecastEntryDto>,
): DailyForecast {
    val temps = entries.mapNotNull { it.main?.temp }
    val tempMins = entries.mapNotNull { it.main?.tempMin }.ifEmpty { temps }
    val tempMaxs = entries.mapNotNull { it.main?.tempMax }.ifEmpty { temps }
    // Dominant condition = most frequent across the day's slices, not just the first (often
    // midnight) slice - avoids showing "sunny" when only one early slice happened to be clear.
    val dominant = entries.flatMap { it.weather.orEmpty() }
        .mapNotNull { it.main }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?: "Clear"
    // Representative icon: pick a mid-day slice, not the first (often midnight) one.
    val midEntry = entries.getOrNull(entries.size / 2)
    return DailyForecast(
        date = date,
        tempMin = tempMins.minOrNull() ?: 0.0,
        tempMax = tempMaxs.maxOrNull() ?: 0.0,
        dominantCondition = dominant,
        icon = midEntry?.weather?.firstOrNull()?.icon.orEmpty(),
    )
}
