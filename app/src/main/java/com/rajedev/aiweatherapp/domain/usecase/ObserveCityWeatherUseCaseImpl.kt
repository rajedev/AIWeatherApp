package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.SavedCity
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class ObserveCityWeatherUseCaseImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ObserveCityWeatherUseCase {
    override fun invoke(cityId: String): Flow<SavedCity?> = weatherRepository.observeCity(cityId)
}
