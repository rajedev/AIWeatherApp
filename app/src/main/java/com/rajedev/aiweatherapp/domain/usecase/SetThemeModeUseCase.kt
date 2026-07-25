package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ThemeMode

interface SetThemeModeUseCase {
    suspend operator fun invoke(themeMode: ThemeMode)
}
