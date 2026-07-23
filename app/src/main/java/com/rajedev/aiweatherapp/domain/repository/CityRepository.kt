package com.rajedev.aiweatherapp.domain.repository

import com.rajedev.aiweatherapp.domain.model.ResolvedCity

interface CityRepository {
    suspend fun searchCities(query: String): Result<List<ResolvedCity>>
    suspend fun resolveFromCoordinates(lat: Double, lon: Double): Result<ResolvedCity>
    suspend fun saveCity(city: ResolvedCity): Result<Unit>
    suspend fun removeCity(cityId: String): Result<Unit>
}
