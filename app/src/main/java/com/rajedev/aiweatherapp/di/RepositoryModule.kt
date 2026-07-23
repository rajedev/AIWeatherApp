package com.rajedev.aiweatherapp.di

import com.rajedev.aiweatherapp.data.repository.CityRepositoryImpl
import com.rajedev.aiweatherapp.data.repository.WeatherRepositoryImpl
import com.rajedev.aiweatherapp.domain.repository.CityRepository
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindCityRepository(impl: CityRepositoryImpl): CityRepository
}
