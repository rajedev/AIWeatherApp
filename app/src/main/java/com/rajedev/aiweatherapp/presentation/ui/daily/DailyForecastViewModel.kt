package com.rajedev.aiweatherapp.presentation.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveDailyForecastUseCase
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
class DailyForecastViewModel @Inject constructor(
    private val observeDailyForecastUseCase: ObserveDailyForecastUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyForecastUiState())
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun onAction(action: DailyForecastAction) {
        when (action) {
            is DailyForecastAction.LoadCity -> loadCity(action.cityId)
            DailyForecastAction.Refresh -> refresh()
            DailyForecastAction.ConsumeError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadCity(cityId: String) {
        if (observeJob?.isActive == true && _uiState.value.cityId == cityId) return
        observeJob?.cancel()
        _uiState.value = DailyForecastUiState(cityId = cityId, isLoading = true)
        observeJob = viewModelScope.launch {
            combine(
                observeDailyForecastUseCase(cityId),
                getUserPreferencesUseCase(),
            ) { daily, preferences -> daily to preferences }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { (daily, preferences) ->
                    _uiState.update { it.copy(dailyForecasts = daily, unit = preferences.unit, isLoading = false) }
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
