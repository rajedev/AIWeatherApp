package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.repository.CityRepository
import javax.inject.Inject

internal class SearchCitiesUseCaseImpl @Inject constructor(
    private val cityRepository: CityRepository,
) : SearchCitiesUseCase {
    override suspend fun invoke(query: String): Result<List<ResolvedCity>> = cityRepository.searchCities(query)
}
