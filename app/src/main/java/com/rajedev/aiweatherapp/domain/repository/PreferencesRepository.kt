package com.rajedev.aiweatherapp.domain.repository

import com.rajedev.aiweatherapp.domain.model.AlertThresholds
import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.model.UserPreferences
import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun setUnit(unit: WeatherUnit)
    suspend fun setAlertThresholds(thresholds: AlertThresholds)
    suspend fun setThemeMode(themeMode: ThemeMode)
}
