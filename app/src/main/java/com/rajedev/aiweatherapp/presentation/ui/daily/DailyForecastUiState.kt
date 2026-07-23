package com.rajedev.aiweatherapp.presentation.ui.daily

import androidx.compose.runtime.Immutable
import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.model.WeatherUnit

@Immutable
data class DailyForecastUiState(
    val cityId: String = "",
    val dailyForecasts: List<DailyForecast> = emptyList(),
    val unit: WeatherUnit = WeatherUnit.METRIC,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
