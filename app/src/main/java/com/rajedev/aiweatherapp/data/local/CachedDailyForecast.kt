package com.rajedev.aiweatherapp.data.local

import kotlinx.serialization.Serializable

@Serializable
data class CachedDailyForecast(
    val date: String,
    val tempMin: Double,
    val tempMax: Double,
    val dominantCondition: String,
    val icon: String,
)
