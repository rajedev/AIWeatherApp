package com.rajedev.aiweatherapp.presentation.ui.savedcities

import androidx.compose.runtime.Immutable
import com.rajedev.aiweatherapp.domain.model.SavedCity
import com.rajedev.aiweatherapp.domain.model.WeatherUnit

@Immutable
data class SavedCitiesUiState(
    val cities: List<SavedCity> = emptyList(),
    val unit: WeatherUnit = WeatherUnit.METRIC,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showLocationServicesDisabledDialog: Boolean = false,
)
