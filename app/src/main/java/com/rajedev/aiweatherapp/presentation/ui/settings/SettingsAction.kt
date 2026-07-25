package com.rajedev.aiweatherapp.presentation.ui.settings

import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.model.WeatherUnit

sealed interface SettingsAction {
    data class SetUnit(val unit: WeatherUnit) : SettingsAction
    data class SetHighTempThreshold(val value: Double) : SettingsAction
    data class SetLowTempThreshold(val value: Double) : SettingsAction
    data class ToggleAlerts(val enabled: Boolean) : SettingsAction
    data class SetThemeMode(val themeMode: ThemeMode) : SettingsAction
}
