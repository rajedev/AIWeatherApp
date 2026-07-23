package com.rajedev.aiweatherapp.domain.model

data class ResolvedCity(
    val cityId: String,
    val name: String,
    val state: String?,
    val country: String,
    val lat: Double,
    val lon: Double,
)
