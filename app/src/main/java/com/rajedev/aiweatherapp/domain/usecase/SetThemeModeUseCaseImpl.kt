package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import javax.inject.Inject

internal class SetThemeModeUseCaseImpl @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : SetThemeModeUseCase {
    override suspend fun invoke(themeMode: ThemeMode) = preferencesRepository.setThemeMode(themeMode)
}
