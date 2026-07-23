package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MainWeatherDto(
    val temp: Double? = null,
    @SerialName("feels_like") val feelsLike: Double? = null,
    @SerialName("temp_min") val tempMin: Double? = null,
    @SerialName("temp_max") val tempMax: Double? = null,
    val humidity: Int? = null,
    val pressure: Int? = null,
)
