package com.rajedev.aiweatherapp.di

import com.rajedev.aiweatherapp.data.location.FusedLocationProviderImpl
import com.rajedev.aiweatherapp.domain.repository.LocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: FusedLocationProviderImpl): LocationProvider
}
