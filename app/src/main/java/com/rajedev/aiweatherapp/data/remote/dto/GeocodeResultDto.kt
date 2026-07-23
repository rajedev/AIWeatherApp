package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodeResultDto(
    val name: String? = null,
    @SerialName("local_names") val localNames: Map<String, String>? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val country: String? = null,
    val state: String? = null,
)
