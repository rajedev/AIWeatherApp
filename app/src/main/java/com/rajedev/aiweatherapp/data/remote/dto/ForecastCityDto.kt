package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForecastCityDto(
    val name: String? = null,
    val country: String? = null,
    val timezone: Int? = null,
)
