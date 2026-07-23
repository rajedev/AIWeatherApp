package com.rajedev.aiweatherapp.presentation.ui.daily

sealed interface DailyForecastAction {
    data class LoadCity(val cityId: String) : DailyForecastAction
    data object Refresh : DailyForecastAction
    data object ConsumeError : DailyForecastAction
}
