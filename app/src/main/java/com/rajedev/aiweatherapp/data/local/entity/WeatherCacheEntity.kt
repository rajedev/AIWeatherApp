package com.rajedev.aiweatherapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cityId: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "state") val state: String?,
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lon") val lon: Double,
    @ColumnInfo(name = "tz_offset_seconds") val tzOffsetSeconds: Int,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "last_fetched_at") val lastFetchedAt: Long,
    @ColumnInfo(name = "current_temp") val currentTemp: Double,
    @ColumnInfo(name = "feels_like") val feelsLike: Double,
    @ColumnInfo(name = "temp_min") val tempMin: Double,
    @ColumnInfo(name = "temp_max") val tempMax: Double,
    @ColumnInfo(name = "humidity") val humidity: Int,
    @ColumnInfo(name = "condition_main") val conditionMain: String,
    @ColumnInfo(name = "condition_description") val conditionDescription: String,
    @ColumnInfo(name = "condition_icon") val conditionIcon: String,
    @ColumnInfo(name = "hourly_json") val hourlyJson: String,
    @ColumnInfo(name = "daily_json") val dailyJson: String,
)
