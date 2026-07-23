package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.DailyForecast
import kotlinx.coroutines.flow.Flow

interface ObserveDailyForecastUseCase {
    operator fun invoke(cityId: String): Flow<List<DailyForecast>>
}
