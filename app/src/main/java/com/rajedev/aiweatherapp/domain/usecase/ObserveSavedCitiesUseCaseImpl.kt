package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.SavedCity
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class ObserveSavedCitiesUseCaseImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ObserveSavedCitiesUseCase {
    override fun invoke(): Flow<List<SavedCity>> = weatherRepository.observeAllSavedCities()
}
