package com.rajedev.aiweatherapp.presentation.ui.currentconditions

import androidx.compose.runtime.Immutable
import com.rajedev.aiweatherapp.domain.model.CurrentConditions
import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import com.rajedev.aiweatherapp.domain.model.WeatherUnit

@Immutable
data class CityDetailUiState(
    val cityId: String = "",
    val displayName: String = "",
    val current: CurrentConditions? = null,
    val hourly: List<HourlyForecast> = emptyList(),
    val unit: WeatherUnit = WeatherUnit.METRIC,
    val isStale: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
