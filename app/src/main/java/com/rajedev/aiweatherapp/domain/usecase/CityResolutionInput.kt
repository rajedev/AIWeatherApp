package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.LatLon
import com.rajedev.aiweatherapp.domain.model.ResolvedCity

sealed interface CityResolutionInput {
    data class FromSearchSelection(val geocodeResult: ResolvedCity) : CityResolutionInput
    data class FromCoordinates(val latLon: LatLon) : CityResolutionInput
}
