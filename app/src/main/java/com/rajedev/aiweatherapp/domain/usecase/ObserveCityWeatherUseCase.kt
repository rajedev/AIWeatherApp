package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.SavedCity
import kotlinx.coroutines.flow.Flow

interface ObserveCityWeatherUseCase {
    operator fun invoke(cityId: String): Flow<SavedCity?>
}
