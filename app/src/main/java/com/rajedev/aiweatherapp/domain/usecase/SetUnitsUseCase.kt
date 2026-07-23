package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.WeatherUnit

interface SetUnitsUseCase {
    suspend operator fun invoke(unit: WeatherUnit)
}
