package com.rajedev.aiweatherapp.presentation.ui.currentconditions

sealed interface CityDetailAction {
    data class LoadCity(val cityId: String) : CityDetailAction
    data object Refresh : CityDetailAction
    data object ConsumeError : CityDetailAction
}
