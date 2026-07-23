package com.rajedev.aiweatherapp.presentation.ui.search

sealed interface CitySearchUiEvent {
    data object CitySaved : CitySearchUiEvent
}
