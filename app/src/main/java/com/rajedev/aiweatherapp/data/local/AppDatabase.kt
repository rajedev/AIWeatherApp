package com.rajedev.aiweatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rajedev.aiweatherapp.data.local.dao.AlertStateDao
import com.rajedev.aiweatherapp.data.local.dao.WeatherCacheDao
import com.rajedev.aiweatherapp.data.local.entity.AlertStateEntity
import com.rajedev.aiweatherapp.data.local.entity.WeatherCacheEntity

@Database(
    entities = [WeatherCacheEntity::class, AlertStateEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun alertStateDao(): AlertStateDao
}
