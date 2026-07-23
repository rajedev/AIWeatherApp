package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.repository.CityRepository
import javax.inject.Inject

internal class RemoveCityUseCaseImpl @Inject constructor(
    private val cityRepository: CityRepository,
) : RemoveCityUseCase {
    override suspend fun invoke(cityId: String): Result<Unit> = cityRepository.removeCity(cityId)
}
