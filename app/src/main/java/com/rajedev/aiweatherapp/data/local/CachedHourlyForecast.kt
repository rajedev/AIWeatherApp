package com.rajedev.aiweatherapp.data.local

import kotlinx.serialization.Serializable

@Serializable
data class CachedHourlyForecast(
    val timestamp: Long,
    val temp: Double,
    val conditionMain: String,
    val conditionIcon: String,
)
