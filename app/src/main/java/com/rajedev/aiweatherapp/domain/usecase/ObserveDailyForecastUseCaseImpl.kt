package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class ObserveDailyForecastUseCaseImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ObserveDailyForecastUseCase {
    override fun invoke(cityId: String): Flow<List<DailyForecast>> = weatherRepository.observeDaily(cityId)
}
