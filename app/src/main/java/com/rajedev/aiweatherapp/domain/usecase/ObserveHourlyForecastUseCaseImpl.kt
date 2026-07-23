package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class ObserveHourlyForecastUseCaseImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ObserveHourlyForecastUseCase {
    override fun invoke(cityId: String): Flow<List<HourlyForecast>> = weatherRepository.observeHourly(cityId)
}
