package com.rajedev.aiweatherapp.domain.model

sealed interface WeatherAlert {
    val cityId: String

    data class ExtremeHeat(override val cityId: String, val temp: Double) : WeatherAlert
    data class ExtremeCold(override val cityId: String, val temp: Double) : WeatherAlert
    data class SevereCondition(override val cityId: String, val conditionMain: String) : WeatherAlert
}
