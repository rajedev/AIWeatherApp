package com.rajedev.aiweatherapp.data.remote.api

import com.rajedev.aiweatherapp.data.remote.dto.CurrentWeatherDto
import com.rajedev.aiweatherapp.data.remote.dto.ForecastResponseDto
import com.rajedev.aiweatherapp.data.remote.dto.GeocodeResultDto
import retrofit2.http.GET
import retrofit2.http.Query

private const val DEFAULT_GEOCODE_SEARCH_LIMIT = 5
private const val DEFAULT_REVERSE_GEOCODE_LIMIT = 1

interface OpenWeatherApi {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
    ): CurrentWeatherDto

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
    ): ForecastResponseDto

    @GET("geo/1.0/direct")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("limit") limit: Int = DEFAULT_GEOCODE_SEARCH_LIMIT,
    ): List<GeocodeResultDto>

    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = DEFAULT_REVERSE_GEOCODE_LIMIT,
    ): List<GeocodeResultDto>
}
