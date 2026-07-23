package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WindDto(
    val speed: Double? = null,
    val deg: Int? = null,
)
