package com.rajedev.aiweatherapp.data.repository

import com.rajedev.aiweatherapp.data.local.dao.WeatherCacheDao
import com.rajedev.aiweatherapp.data.local.entity.WeatherCacheEntity
import com.rajedev.aiweatherapp.data.remote.api.OpenWeatherApi
import com.rajedev.aiweatherapp.data.remote.dto.CurrentWeatherDto
import com.rajedev.aiweatherapp.data.remote.dto.ForecastResponseDto
import com.rajedev.aiweatherapp.data.remote.dto.GeocodeResultDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeOpenWeatherApi(private val searchResults: List<GeocodeResultDto>) : OpenWeatherApi {

    override suspend fun getCurrentWeather(lat: Double, lon: Double, units: String): CurrentWeatherDto =
        throw NotImplementedError("not exercised by searchCities()")

    override suspend fun getForecast(lat: Double, lon: Double, units: String): ForecastResponseDto =
        throw NotImplementedError("not exercised by searchCities()")

    override suspend fun searchPlaces(query: String, limit: Int): List<GeocodeResultDto> = searchResults

    override suspend fun reverseGeocode(lat: Double, lon: Double, limit: Int): List<GeocodeResultDto> =
        throw NotImplementedError("not exercised by searchCities()")
}

private class FakeWeatherCacheDao : WeatherCacheDao {

    override fun observeAll(): Flow<List<WeatherCacheEntity>> = flowOf(emptyList())

    override fun observeByCityId(cityId: String): Flow<WeatherCacheEntity?> = flowOf(null)

    override suspend fun getByCityId(cityId: String): WeatherCacheEntity? = null

    override suspend fun getAllCityIds(): List<String> = emptyList()

    override suspend fun upsert(entity: WeatherCacheEntity) = Unit

    override suspend fun delete(cityId: String) = Unit

    override suspend fun evictOldestBeyondLimit(maxSavedCities: Int) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class CityRepositoryImplTest {

    private fun repositoryWith(searchResults: List<GeocodeResultDto>) = CityRepositoryImpl(
        api = FakeOpenWeatherApi(searchResults),
        dao = FakeWeatherCacheDao(),
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun `results whose coordinates round to the same cityId are deduped`() = runTest {
        val nearDuplicates = listOf(
            GeocodeResultDto(name = "Sydney", lat = -33.8688, lon = 151.2093, country = "AU"),
            GeocodeResultDto(name = "Sydney", lat = -33.86881, lon = 151.20929, country = "AU"),
        )

        val result = repositoryWith(nearDuplicates).searchCities("Sydney").getOrThrow()

        assertEquals(1, result.size)
    }

    @Test
    fun `a malformed entry is dropped without failing the whole search`() = runTest {
        val mixed = listOf(
            GeocodeResultDto(name = "Sydney", lat = -33.8688, lon = 151.2093, country = "AU"),
            GeocodeResultDto(name = "Broken", lat = 1.0, lon = 2.0, country = null),
        )

        val result = repositoryWith(mixed).searchCities("Sydney").getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Sydney", result.single().name)
    }

    @Test
    fun `genuinely distinct cities all map through unchanged`() = runTest {
        val distinctCities = listOf(
            GeocodeResultDto(name = "Sydney", lat = -33.8688, lon = 151.2093, country = "AU"),
            GeocodeResultDto(name = "Toronto", lat = 43.6532, lon = -79.3832, country = "CA"),
        )

        val result = repositoryWith(distinctCities).searchCities("Sydney").getOrThrow()

        assertEquals(2, result.size)
    }
}
