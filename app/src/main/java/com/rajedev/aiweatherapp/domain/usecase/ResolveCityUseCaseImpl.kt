package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.repository.CityRepository
import javax.inject.Inject

internal class ResolveCityUseCaseImpl @Inject constructor(
    private val cityRepository: CityRepository,
) : ResolveCityUseCase {

    override suspend fun invoke(input: CityResolutionInput): Result<ResolvedCity> = when (input) {
        // Already canonical via /geo/1.0/direct - pass through so both paths call this use case.
        is CityResolutionInput.FromSearchSelection -> Result.success(input.geocodeResult)
        // Raw device coordinates must resolve through OWM's own reverse geocoding (not the Android
        // Geocoder) to land on the same ResolvedCity shape / cityId as the search path.
        is CityResolutionInput.FromCoordinates ->
            cityRepository.resolveFromCoordinates(input.latLon.lat, input.latLon.lon)
    }
}
