package com.rajedev.aiweatherapp.data.remote.mapper

import com.rajedev.aiweatherapp.data.remote.dto.GeocodeResultDto
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import kotlin.math.round

// 2 decimal places (~1.1km grid) - coarse enough that the same city queried two different ways
// (typed search vs. reverse geocode from device location) always converges on the same cityId,
// fine enough that two genuinely different nearby cities essentially never collide.
private const val COORD_PRECISION = 100.0

fun GeocodeResultDto.toDomain(): ResolvedCity {
    val resolvedLat = lat ?: error("Missing latitude in geocode result")
    val resolvedLon = lon ?: error("Missing longitude in geocode result")
    val roundedLat = round(resolvedLat * COORD_PRECISION) / COORD_PRECISION
    val roundedLon = round(resolvedLon * COORD_PRECISION) / COORD_PRECISION
    return ResolvedCity(
        cityId = "$roundedLat,$roundedLon",
        name = name ?: error("Missing city name in geocode result"),
        state = state,
        country = country ?: error("Missing country in geocode result"),
        lat = resolvedLat,
        lon = resolvedLon,
    )
}
