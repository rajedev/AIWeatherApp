package com.rajedev.aiweatherapp.data.repository

import com.rajedev.aiweatherapp.data.local.dao.WeatherCacheDao
import com.rajedev.aiweatherapp.data.local.entity.WeatherCacheEntity
import com.rajedev.aiweatherapp.data.remote.api.OpenWeatherApi
import com.rajedev.aiweatherapp.data.remote.mapper.toDomain
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.repository.CityRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private const val REVERSE_GEOCODE_LIMIT = 1
private const val EMPTY_JSON_ARRAY = "[]"

// Generous cap on saved cities so a search-heavy user can't grow the cache unbounded.
private const val MAX_SAVED_CITIES = 15

@Singleton
internal class CityRepositoryImpl @Inject constructor(
    private val api: OpenWeatherApi,
    private val dao: WeatherCacheDao,
    @param:Named("io") private val ioDispatcher: CoroutineDispatcher,
) : CityRepository {

    override suspend fun searchCities(query: String): Result<List<ResolvedCity>> = withContext(ioDispatcher) {
        runCatching { api.searchPlaces(query).map { it.toDomain() } }
    }

    override suspend fun resolveFromCoordinates(lat: Double, lon: Double): Result<ResolvedCity> =
        withContext(ioDispatcher) {
            runCatching {
                api.reverseGeocode(lat, lon, limit = REVERSE_GEOCODE_LIMIT).firstOrNull()?.toDomain()
                    ?: error("No place found for coordinates $lat,$lon")
            }
        }

    override suspend fun saveCity(city: ResolvedCity): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val existing = dao.getByCityId(city.cityId)
            dao.upsert(buildEntity(city, existing))
            dao.evictOldestBeyondLimit(MAX_SAVED_CITIES)
        }
    }

    override suspend fun removeCity(cityId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { dao.delete(cityId) }
    }

    private suspend fun buildEntity(city: ResolvedCity, existing: WeatherCacheEntity?): WeatherCacheEntity =
        WeatherCacheEntity(
            cityId = city.cityId,
            displayName = city.name,
            state = city.state,
            country = city.country,
            lat = city.lat,
            lon = city.lon,
            tzOffsetSeconds = existing?.tzOffsetSeconds ?: 0,
            sortOrder = existing?.sortOrder ?: dao.getAllCityIds().size,
            // lastFetchedAt = 0L on first save marks the row immediately stale so the first
            // RefreshWeatherUseCase call (fired right after save) has real data to overwrite this with.
            lastFetchedAt = existing?.lastFetchedAt ?: 0L,
            currentTemp = existing?.currentTemp ?: 0.0,
            feelsLike = existing?.feelsLike ?: 0.0,
            tempMin = existing?.tempMin ?: 0.0,
            tempMax = existing?.tempMax ?: 0.0,
            humidity = existing?.humidity ?: 0,
            conditionMain = existing?.conditionMain.orEmpty(),
            conditionDescription = existing?.conditionDescription.orEmpty(),
            conditionIcon = existing?.conditionIcon.orEmpty(),
            hourlyJson = existing?.hourlyJson ?: EMPTY_JSON_ARRAY,
            dailyJson = existing?.dailyJson ?: EMPTY_JSON_ARRAY,
        )
}
