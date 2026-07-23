package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val list: List<ForecastEntryDto>? = null,
    val city: ForecastCityDto? = null,
)
