package com.rajedev.aiweatherapp.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable data object SavedCities : Route
    @Serializable data class CityDetail(val cityId: String) : Route
    @Serializable data class DailyForecast(val cityId: String) : Route
    @Serializable data object CitySearch : Route
    @Serializable data object Settings : Route
}
