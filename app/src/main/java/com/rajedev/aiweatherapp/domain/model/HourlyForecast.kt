package com.rajedev.aiweatherapp.domain.model

data class HourlyForecast(
    val timestamp: Long,
    val temp: Double,
    val conditionMain: String,
    val conditionIcon: String,
)
