package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.repository.CityRepository
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

internal class SaveCityUseCaseImpl @Inject constructor(
    private val cityRepository: CityRepository,
    private val weatherRepository: WeatherRepository,
) : SaveCityUseCase {

    override suspend fun invoke(city: ResolvedCity): Result<Unit> {
        val saveResult = cityRepository.saveCity(city)
        if (saveResult.isSuccess) weatherRepository.refresh(city.cityId)
        return saveResult
    }
}
