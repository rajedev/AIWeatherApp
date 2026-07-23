package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity

interface ResolveCityUseCase {
    suspend operator fun invoke(input: CityResolutionInput): Result<ResolvedCity>
}
