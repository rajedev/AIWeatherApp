package com.rajedev.aiweatherapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rajedev.aiweatherapp.domain.model.AlertThresholds
import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.model.UserPreferences
import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

internal class UserPreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> =
        context.dataStore.data.map { prefs ->
            UserPreferences(
                unit = WeatherUnit.valueOf(prefs[UNIT_KEY] ?: WeatherUnit.METRIC.name),
                thresholds = AlertThresholds(
                    highTempC = prefs[HIGH_TEMP_KEY] ?: DEFAULT_HIGH_TEMP_C,
                    lowTempC = prefs[LOW_TEMP_KEY] ?: DEFAULT_LOW_TEMP_C,
                    alertsEnabled = prefs[ALERTS_ENABLED_KEY] ?: false,
                ),
                themeMode = ThemeMode.valueOf(prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name),
            )
        }

    override suspend fun setUnit(unit: WeatherUnit) {
        context.dataStore.edit { it[UNIT_KEY] = unit.name }
    }

    override suspend fun setAlertThresholds(thresholds: AlertThresholds) {
        context.dataStore.edit {
            it[HIGH_TEMP_KEY] = thresholds.highTempC
            it[LOW_TEMP_KEY] = thresholds.lowTempC
            it[ALERTS_ENABLED_KEY] = thresholds.alertsEnabled
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE_KEY] = themeMode.name }
    }

    private companion object {
        val UNIT_KEY = stringPreferencesKey("unit")
        val HIGH_TEMP_KEY = doublePreferencesKey("high_temp_c")
        val LOW_TEMP_KEY = doublePreferencesKey("low_temp_c")
        val ALERTS_ENABLED_KEY = booleanPreferencesKey("alerts_enabled")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

        // Intentionally mirrors the HOT/WINTER mood-boundary constants in the weather-adaptive
        // theming system, so "looks hot in the theme" and "triggers an alert" agree by default.
        const val DEFAULT_HIGH_TEMP_C = 30.0
        const val DEFAULT_LOW_TEMP_C = 5.0
    }
}
