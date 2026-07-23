package com.rajedev.aiweatherapp.domain.usecase

interface RefreshAllSavedCitiesUseCase {
    suspend operator fun invoke(): List<Pair<String, Result<Unit>>>
}
