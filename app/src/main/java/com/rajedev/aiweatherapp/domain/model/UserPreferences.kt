package com.rajedev.aiweatherapp.domain.model

data class UserPreferences(
    val unit: WeatherUnit,
    val thresholds: AlertThresholds,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
