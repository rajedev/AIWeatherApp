package com.rajedev.aiweatherapp.presentation.ui.settings

import androidx.compose.runtime.Immutable
import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.model.WeatherUnit

@Immutable
data class SettingsUiState(
    val unit: WeatherUnit = WeatherUnit.METRIC,
    val highTempC: Double = 35.0,
    val lowTempC: Double = 5.0,
    val alertsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
