package com.rajedev.aiweatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rajedev.aiweatherapp.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<WeatherCacheEntity>>

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    fun observeByCityId(cityId: String): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    suspend fun getByCityId(cityId: String): WeatherCacheEntity?

    @Query("SELECT cityId FROM weather_cache")
    suspend fun getAllCityIds(): List<String>

    @Upsert
    suspend fun upsert(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cityId = :cityId")
    suspend fun delete(cityId: String)

    // Generous cap so a search-heavy user can't grow the cache unbounded.
    @Query(
        """
        DELETE FROM weather_cache WHERE cityId IN (
            SELECT cityId FROM weather_cache
            ORDER BY last_fetched_at ASC
            LIMIT MAX(0, (SELECT COUNT(*) FROM weather_cache) - :maxSavedCities)
        )
        """,
    )
    suspend fun evictOldestBeyondLimit(maxSavedCities: Int)
}
