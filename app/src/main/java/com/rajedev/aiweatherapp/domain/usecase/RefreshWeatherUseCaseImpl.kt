package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

internal class RefreshWeatherUseCaseImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : RefreshWeatherUseCase {
    override suspend fun invoke(cityId: String): Result<Unit> = weatherRepository.refresh(cityId)
}
