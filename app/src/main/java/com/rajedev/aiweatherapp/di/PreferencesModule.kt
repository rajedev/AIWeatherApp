package com.rajedev.aiweatherapp.di

import com.rajedev.aiweatherapp.data.preferences.UserPreferencesDataStore
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: UserPreferencesDataStore): PreferencesRepository
}
