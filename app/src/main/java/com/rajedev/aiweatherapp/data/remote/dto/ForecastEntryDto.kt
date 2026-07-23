package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastEntryDto(
    val dt: Long? = null,
    val main: MainWeatherDto? = null,
    val weather: List<WeatherConditionDto>? = null,
    val wind: WindDto? = null,
    @SerialName("dt_txt") val dtTxt: String? = null,
)
