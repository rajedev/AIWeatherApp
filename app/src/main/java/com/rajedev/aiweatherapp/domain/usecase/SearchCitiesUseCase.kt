package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity

interface SearchCitiesUseCase {
    suspend operator fun invoke(query: String): Result<List<ResolvedCity>>
}
