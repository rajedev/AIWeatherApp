package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import javax.inject.Inject

internal class SetUnitsUseCaseImpl @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : SetUnitsUseCase {
    override suspend fun invoke(unit: WeatherUnit) = preferencesRepository.setUnit(unit)
}
