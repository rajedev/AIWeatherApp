package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity

interface SaveCityUseCase {
    suspend operator fun invoke(city: ResolvedCity): Result<Unit>
}
