package com.rajedev.aiweatherapp.di

import android.content.Context
import androidx.room.Room
import com.rajedev.aiweatherapp.data.local.AppDatabase
import com.rajedev.aiweatherapp.data.local.dao.AlertStateDao
import com.rajedev.aiweatherapp.data.local.dao.WeatherCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "ai_weather_app.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideWeatherCacheDao(database: AppDatabase): WeatherCacheDao = database.weatherCacheDao()

    @Provides
    @Singleton
    fun provideAlertStateDao(database: AppDatabase): AlertStateDao = database.alertStateDao()
}
