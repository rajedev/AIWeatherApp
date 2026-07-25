package com.rajedev.aiweatherapp.presentation.ui.savedcities

sealed interface SavedCitiesAction {
    data class RemoveCity(val cityId: String) : SavedCitiesAction
    data object UseCurrentLocation : SavedCitiesAction
    data class LocationPermissionResult(val granted: Boolean) : SavedCitiesAction
    data object ConsumeError : SavedCitiesAction
    data object DismissLocationServicesDialog : SavedCitiesAction
    data object OpenLocationSettingsRequested : SavedCitiesAction
}
