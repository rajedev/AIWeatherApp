package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherConditionDto(
    val id: Int? = null,
    @SerialName("main") val main: String? = null,
    val description: String? = null,
    val icon: String? = null,
)
