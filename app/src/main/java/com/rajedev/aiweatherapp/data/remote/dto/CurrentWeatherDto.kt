package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherDto(
    val coord: CoordDto? = null,
    val weather: List<WeatherConditionDto>? = null,
    val main: MainWeatherDto? = null,
    val wind: WindDto? = null,
    val dt: Long? = null,
    val sys: SysDto? = null,
    val name: String? = null,
    val timezone: Int? = null,
)
