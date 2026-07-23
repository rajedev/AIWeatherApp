package com.rajedev.aiweatherapp.domain.usecase

interface RemoveCityUseCase {
    suspend operator fun invoke(cityId: String): Result<Unit>
}
