package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity

interface ResolveCurrentLocationCityUseCase {
    suspend operator fun invoke(): Result<ResolvedCity>
}
