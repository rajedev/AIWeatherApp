package com.rajedev.aiweatherapp.presentation.ui.savedcities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.repository.LocationServicesUnavailable
import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveSavedCitiesUseCase
import com.rajedev.aiweatherapp.domain.usecase.RemoveCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.ResolveCurrentLocationCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.SaveCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SavedCitiesViewModel @Inject constructor(
    private val observeSavedCitiesUseCase: ObserveSavedCitiesUseCase,
    private val removeCityUseCase: RemoveCityUseCase,
    private val resolveCurrentLocationCityUseCase: ResolveCurrentLocationCityUseCase,
    private val saveCityUseCase: SaveCityUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedCitiesUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<SavedCitiesUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                observeSavedCitiesUseCase(),
                getUserPreferencesUseCase(),
            ) { cities, preferences -> cities to preferences }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { (cities, preferences) ->
                    _uiState.update { it.copy(cities = cities, unit = preferences.unit, isLoading = false) }
                }
        }
    }

    fun onAction(action: SavedCitiesAction) {
        when (action) {
            is SavedCitiesAction.RemoveCity -> viewModelScope.launch { removeCityUseCase(action.cityId) }
            SavedCitiesAction.UseCurrentLocation -> requestLocationPermission()
            is SavedCitiesAction.LocationPermissionResult -> onLocationPermissionResult(action.granted)
            SavedCitiesAction.ConsumeError -> _uiState.update { it.copy(errorMessage = null) }
            SavedCitiesAction.DismissLocationServicesDialog ->
                _uiState.update { it.copy(showLocationServicesDisabledDialog = false) }
            SavedCitiesAction.OpenLocationSettingsRequested -> openLocationSettings()
        }
    }

    private fun requestLocationPermission() {
        viewModelScope.launch { _uiEvent.send(SavedCitiesUiEvent.RequestLocationPermission) }
    }

    private fun openLocationSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(showLocationServicesDisabledDialog = false) }
            _uiEvent.send(SavedCitiesUiEvent.OpenLocationSettings)
        }
    }

    private fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) return
        viewModelScope.launch {
            resolveCurrentLocationCityUseCase()
                .mapCatching { city -> city to saveCityUseCase(city).getOrThrow() }
                .onSuccess { (city, _) -> _uiEvent.send(SavedCitiesUiEvent.CityAdded(city.cityId)) }
                .onFailure(::handleLocationFailure)
        }
    }

    private fun handleLocationFailure(error: Throwable) {
        if (error is LocationServicesUnavailable) {
            _uiState.update { it.copy(showLocationServicesDisabledDialog = true) }
        } else {
            _uiState.update { it.copy(errorMessage = error.message) }
        }
    }
}
