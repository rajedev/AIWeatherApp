package com.rajedev.aiweatherapp.di

import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCase
import com.rajedev.aiweatherapp.domain.usecase.GetUserPreferencesUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.ObserveCityWeatherUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveCityWeatherUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.ObserveDailyForecastUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveDailyForecastUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.ObserveHourlyForecastUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveHourlyForecastUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.ObserveSavedCitiesUseCase
import com.rajedev.aiweatherapp.domain.usecase.ObserveSavedCitiesUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.RefreshAllSavedCitiesUseCase
import com.rajedev.aiweatherapp.domain.usecase.RefreshAllSavedCitiesUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.RefreshWeatherUseCase
import com.rajedev.aiweatherapp.domain.usecase.RefreshWeatherUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.RemoveCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.RemoveCityUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.ResolveCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.ResolveCityUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.ResolveCurrentLocationCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.ResolveCurrentLocationCityUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.SaveCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.SaveCityUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.SearchCitiesUseCase
import com.rajedev.aiweatherapp.domain.usecase.SearchCitiesUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.SetAlertThresholdsUseCase
import com.rajedev.aiweatherapp.domain.usecase.SetAlertThresholdsUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.SetThemeModeUseCase
import com.rajedev.aiweatherapp.domain.usecase.SetThemeModeUseCaseImpl
import com.rajedev.aiweatherapp.domain.usecase.SetUnitsUseCase
import com.rajedev.aiweatherapp.domain.usecase.SetUnitsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UseCaseModule {

    @Binds
    abstract fun bindResolveCityUseCase(impl: ResolveCityUseCaseImpl): ResolveCityUseCase

    @Binds
    abstract fun bindRefreshWeatherUseCase(impl: RefreshWeatherUseCaseImpl): RefreshWeatherUseCase

    @Binds
    abstract fun bindObserveSavedCitiesUseCase(impl: ObserveSavedCitiesUseCaseImpl): ObserveSavedCitiesUseCase

    @Binds
    abstract fun bindObserveCityWeatherUseCase(impl: ObserveCityWeatherUseCaseImpl): ObserveCityWeatherUseCase

    @Binds
    abstract fun bindObserveDailyForecastUseCase(impl: ObserveDailyForecastUseCaseImpl): ObserveDailyForecastUseCase

    @Binds
    abstract fun bindObserveHourlyForecastUseCase(impl: ObserveHourlyForecastUseCaseImpl): ObserveHourlyForecastUseCase

    @Binds
    abstract fun bindSearchCitiesUseCase(impl: SearchCitiesUseCaseImpl): SearchCitiesUseCase

    @Binds
    abstract fun bindResolveCurrentLocationCityUseCase(
        impl: ResolveCurrentLocationCityUseCaseImpl,
    ): ResolveCurrentLocationCityUseCase

    @Binds
    abstract fun bindSaveCityUseCase(impl: SaveCityUseCaseImpl): SaveCityUseCase

    @Binds
    abstract fun bindRemoveCityUseCase(impl: RemoveCityUseCaseImpl): RemoveCityUseCase

    @Binds
    abstract fun bindGetUserPreferencesUseCase(impl: GetUserPreferencesUseCaseImpl): GetUserPreferencesUseCase

    @Binds
    abstract fun bindSetUnitsUseCase(impl: SetUnitsUseCaseImpl): SetUnitsUseCase

    @Binds
    abstract fun bindSetAlertThresholdsUseCase(impl: SetAlertThresholdsUseCaseImpl): SetAlertThresholdsUseCase

    @Binds
    abstract fun bindSetThemeModeUseCase(impl: SetThemeModeUseCaseImpl): SetThemeModeUseCase

    @Binds
    abstract fun bindRefreshAllSavedCitiesUseCase(
        impl: RefreshAllSavedCitiesUseCaseImpl,
    ): RefreshAllSavedCitiesUseCase
}
