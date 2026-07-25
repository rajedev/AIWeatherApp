package com.rajedev.aiweatherapp.presentation.ui.savedcities

sealed interface SavedCitiesUiEvent {
    data object RequestLocationPermission : SavedCitiesUiEvent
    data class CityAdded(val cityId: String) : SavedCitiesUiEvent
    data object OpenLocationSettings : SavedCitiesUiEvent
}
