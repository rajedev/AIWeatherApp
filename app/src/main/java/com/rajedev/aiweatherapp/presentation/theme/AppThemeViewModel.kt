package com.rajedev.aiweatherapp.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val THEME_MODE_SHARING_TIMEOUT_MS = 5000L

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
) : ViewModel() {

    val themeMode = getUserPreferencesUseCase()
        .map { it.themeMode }
        .catch { emit(ThemeMode.SYSTEM) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(THEME_MODE_SHARING_TIMEOUT_MS), ThemeMode.SYSTEM)
}
