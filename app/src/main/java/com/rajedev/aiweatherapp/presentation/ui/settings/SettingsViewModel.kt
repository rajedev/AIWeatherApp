package com.rajedev.aiweatherapp.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.model.AlertThresholds
import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCase
import com.rajedev.aiweatherapp.domain.usecase.SetAlertThresholdsUseCase
import com.rajedev.aiweatherapp.domain.usecase.SetThemeModeUseCase
import com.rajedev.aiweatherapp.domain.usecase.SetUnitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setUnitsUseCase: SetUnitsUseCase,
    private val setAlertThresholdsUseCase: SetAlertThresholdsUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUserPreferencesUseCase()
                .catch { /* keep last known-good settings on read failure */ }
                .collect { preferences ->
                    _uiState.update {
                        it.copy(
                            unit = preferences.unit,
                            highTempC = preferences.thresholds.highTempC,
                            lowTempC = preferences.thresholds.lowTempC,
                            alertsEnabled = preferences.thresholds.alertsEnabled,
                            themeMode = preferences.themeMode,
                        )
                    }
                }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetUnit -> viewModelScope.launch { setUnitsUseCase(action.unit) }
            is SettingsAction.SetHighTempThreshold -> updateThresholds(highTempC = action.value)
            is SettingsAction.SetLowTempThreshold -> updateThresholds(lowTempC = action.value)
            is SettingsAction.ToggleAlerts -> updateThresholds(alertsEnabled = action.enabled)
            is SettingsAction.SetThemeMode -> viewModelScope.launch { setThemeModeUseCase(action.themeMode) }
        }
    }

    private fun updateThresholds(
        highTempC: Double = _uiState.value.highTempC,
        lowTempC: Double = _uiState.value.lowTempC,
        alertsEnabled: Boolean = _uiState.value.alertsEnabled,
    ) {
        viewModelScope.launch {
            setAlertThresholdsUseCase(AlertThresholds(highTempC, lowTempC, alertsEnabled))
        }
    }
}
