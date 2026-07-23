package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoordDto(
    val lat: Double? = null,
    val lon: Double? = null,
)
