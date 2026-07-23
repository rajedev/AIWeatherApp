package com.rajedev.aiweatherapp.data.local

import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import kotlinx.datetime.LocalDate

fun HourlyForecast.toCached(): CachedHourlyForecast =
    CachedHourlyForecast(timestamp, temp, conditionMain, conditionIcon)

fun CachedHourlyForecast.toDomain(): HourlyForecast =
    HourlyForecast(timestamp, temp, conditionMain, conditionIcon)

fun DailyForecast.toCached(): CachedDailyForecast =
    CachedDailyForecast(date.toString(), tempMin, tempMax, dominantCondition, icon)

fun CachedDailyForecast.toDomain(): DailyForecast =
    DailyForecast(LocalDate.parse(date), tempMin, tempMax, dominantCondition, icon)
