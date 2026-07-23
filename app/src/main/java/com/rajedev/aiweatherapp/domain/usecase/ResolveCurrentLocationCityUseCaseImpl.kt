package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.repository.LocationProvider
import javax.inject.Inject

internal class ResolveCurrentLocationCityUseCaseImpl @Inject constructor(
    private val locationProvider: LocationProvider,
    private val resolveCityUseCase: ResolveCityUseCase,
) : ResolveCurrentLocationCityUseCase {

    override suspend fun invoke(): Result<ResolvedCity> =
        locationProvider.getCurrentLocation().fold(
            onSuccess = { latLon -> resolveCityUseCase(CityResolutionInput.FromCoordinates(latLon)) },
            onFailure = { Result.failure(it) },
        )
}
