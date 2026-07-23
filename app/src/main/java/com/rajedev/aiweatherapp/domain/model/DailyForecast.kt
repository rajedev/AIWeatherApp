package com.rajedev.aiweatherapp.domain.model

import kotlinx.datetime.LocalDate

data class DailyForecast(
    val date: LocalDate,
    val tempMin: Double,
    val tempMax: Double,
    val dominantCondition: String,
    val icon: String,
)
