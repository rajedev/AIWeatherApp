package com.rajedev.aiweatherapp.data.repository

import com.rajedev.aiweatherapp.data.local.CachedDailyForecast
import com.rajedev.aiweatherapp.data.local.CachedHourlyForecast
import com.rajedev.aiweatherapp.data.local.dao.WeatherCacheDao
import com.rajedev.aiweatherapp.data.local.toCached
import com.rajedev.aiweatherapp.data.local.toDomain
import com.rajedev.aiweatherapp.data.local.toSavedCity
import com.rajedev.aiweatherapp.data.remote.api.OpenWeatherApi
import com.rajedev.aiweatherapp.data.remote.mapper.toDailyBuckets
import com.rajedev.aiweatherapp.data.remote.mapper.toDomain
import com.rajedev.aiweatherapp.data.remote.mapper.toHourly
import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import com.rajedev.aiweatherapp.domain.model.SavedCity
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Always fetched in Celsius regardless of the user's display-unit preference - conditions.temp
// and AlertThresholds.highTempC/lowTempC must stay in the same canonical unit so the alert
// evaluator's comparison is meaningful. Fahrenheit is a presentation-layer-only conversion.
private const val CANONICAL_UNITS = "metric"

@Singleton
internal class WeatherRepositoryImpl @Inject constructor(
    private val dao: WeatherCacheDao,
    private val api: OpenWeatherApi,
    private val json: Json,
    @param:Named("io") private val ioDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    override fun observeCity(cityId: String): Flow<SavedCity?> =
        dao.observeByCityId(cityId).map { entity -> entity?.toSavedCity(isStale(entity.lastFetchedAt)) }

    override fun observeAllSavedCities(): Flow<List<SavedCity>> =
        dao.observeAll().map { entities -> entities.map { it.toSavedCity(isStale(it.lastFetchedAt)) } }

    override fun observeHourly(cityId: String): Flow<List<HourlyForecast>> =
        dao.observeByCityId(cityId).map { entity -> entity?.let { decodeHourly(it.hourlyJson) }.orEmpty() }

    override fun observeDaily(cityId: String): Flow<List<DailyForecast>> =
        dao.observeByCityId(cityId).map { entity -> entity?.let { decodeDaily(it.dailyJson) }.orEmpty() }

    // The ONE fetch-and-write path: pull-to-refresh, app-launch initial load, and the sync worker
    // all call this same method - never a parallel/divergent fetch path.
    override suspend fun refresh(cityId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val existing = dao.getByCityId(cityId) ?: error("City $cityId is not saved")
            val current = api.getCurrentWeather(existing.lat, existing.lon, CANONICAL_UNITS).toDomain()
            val forecast = api.getForecast(existing.lat, existing.lon, CANONICAL_UNITS)
            val tzOffset = forecast.city?.timezone ?: current.tzOffsetSeconds
            val hourly = forecast.toHourly()
            val daily = forecast.list.orEmpty().toDailyBuckets(tzOffset)

            dao.upsert(
                existing.copy(
                    tzOffsetSeconds = tzOffset,
                    lastFetchedAt = System.currentTimeMillis(),
                    currentTemp = current.temp,
                    feelsLike = current.feelsLike,
                    tempMin = current.tempMin,
                    tempMax = current.tempMax,
                    humidity = current.humidity,
                    conditionMain = current.conditionMain,
                    conditionDescription = current.conditionDescription,
                    conditionIcon = current.conditionIcon,
                    hourlyJson = json.encodeToString(hourly.map { it.toCached() }),
                    dailyJson = json.encodeToString(daily.map { it.toCached() }),
                ),
            )
        }
    }

    override suspend fun getSavedCityIds(): List<String> = dao.getAllCityIds()

    // Recomputed at read time, never cached - a SavedCity held in ViewModel state while the
    // screen is backgrounded must not show a frozen staleness flag.
    private fun isStale(lastFetchedAt: Long) = System.currentTimeMillis() - lastFetchedAt > STALE_THRESHOLD_MS

    private fun decodeHourly(hourlyJson: String): List<HourlyForecast> =
        json.decodeFromString<List<CachedHourlyForecast>>(hourlyJson).map { it.toDomain() }

    private fun decodeDaily(dailyJson: String): List<DailyForecast> =
        json.decodeFromString<List<CachedDailyForecast>>(dailyJson).map { it.toDomain() }

    private companion object {
        // Midpoint of the 30-60 min weather staleness-tolerance guidance: frequent enough that
        // hourly/current data doesn't visibly drift, loose enough to avoid needless refetching.
        const val STALE_THRESHOLD_MS = 45 * 60 * 1000L
    }
}
