package com.rajedev.aiweatherapp.domain.repository

import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import com.rajedev.aiweatherapp.domain.model.SavedCity
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCity(cityId: String): Flow<SavedCity?>
    fun observeAllSavedCities(): Flow<List<SavedCity>>
    fun observeHourly(cityId: String): Flow<List<HourlyForecast>>
    fun observeDaily(cityId: String): Flow<List<DailyForecast>>
    suspend fun refresh(cityId: String): Result<Unit>
    suspend fun getSavedCityIds(): List<String>
}
