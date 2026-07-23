package com.rajedev.aiweatherapp.domain.repository

import com.rajedev.aiweatherapp.domain.model.LatLon

interface LocationProvider {
    suspend fun getCurrentLocation(): Result<LatLon>
}
