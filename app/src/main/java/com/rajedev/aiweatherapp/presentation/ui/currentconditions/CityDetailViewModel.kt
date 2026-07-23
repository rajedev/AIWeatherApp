package com.rajedev.aiweatherapp.presentation.ui.currentconditions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveCityWeatherUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveHourlyForecastUseCase
import com.rajedev.aiweatherapp.domain.usecase.RefreshWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CityDetailViewModel @Inject constructor(
    private val observeCityWeatherUseCase: ObserveCityWeatherUseCase,
    private val observeHourlyForecastUseCase: ObserveHourlyForecastUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun onAction(action: CityDetailAction) {
        when (action) {
            is CityDetailAction.LoadCity -> loadCity(action.cityId)
            CityDetailAction.Refresh -> refresh()
            CityDetailAction.ConsumeError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    // Navigation3 has no SavedStateHandle-based nav-arg injection like navigation-compose, so the
    // bridging composable calls this once via LaunchedEffect(cityId) instead of an init block.
    private fun loadCity(cityId: String) {
        if (observeJob?.isActive == true && _uiState.value.cityId == cityId) return
        observeJob?.cancel()
        _uiState.value = CityDetailUiState(cityId = cityId, isLoading = true)
        observeJob = viewModelScope.launch {
            combine(
                observeCityWeatherUseCase(cityId),
                observeHourlyForecastUseCase(cityId),
                getUserPreferencesUseCase(),
            ) { savedCity, hourly, preferences -> Triple(savedCity, hourly, preferences) }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { (savedCity, hourly, preferences) ->
                    _uiState.update {
                        it.copy(
                            displayName = savedCity?.resolvedCity?.name ?: it.displayName,
                            current = savedCity?.current,
                            hourly = hourly,
                            unit = preferences.unit,
                            isStale = savedCity?.isStale ?: false,
                            isLoading = false,
                        )
                    }
                    if (savedCity != null && savedCity.isStale) refresh()
                }
        }
    }

    private fun refresh() {
        val cityId = _uiState.value.cityId
        if (cityId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            refreshWeatherUseCase(cityId)
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
