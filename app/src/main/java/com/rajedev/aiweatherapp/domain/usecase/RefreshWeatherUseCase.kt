package com.rajedev.aiweatherapp.domain.usecase

interface RefreshWeatherUseCase {
    suspend operator fun invoke(cityId: String): Result<Unit>
}
