package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import kotlinx.coroutines.flow.Flow

interface ObserveHourlyForecastUseCase {
    operator fun invoke(cityId: String): Flow<List<HourlyForecast>>
}
