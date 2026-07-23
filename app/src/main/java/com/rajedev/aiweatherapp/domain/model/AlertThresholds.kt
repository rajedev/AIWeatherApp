package com.rajedev.aiweatherapp.domain.model

data class AlertThresholds(
    val highTempC: Double,
    val lowTempC: Double,
    val alertsEnabled: Boolean,
)
