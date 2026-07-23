package com.rajedev.aiweatherapp.domain.model

data class CurrentConditions(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val conditionMain: String,
    val conditionDescription: String,
    val conditionIcon: String,
    val tzOffsetSeconds: Int,
    val observedAt: Long,
)
