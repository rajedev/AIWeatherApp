package com.rajedev.aiweatherapp.data.remote.mapper

import com.rajedev.aiweatherapp.data.remote.dto.GeocodeResultDto
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import kotlin.math.round

// 2 decimal places (~1.1km grid) - coarse enough that the same city queried two different ways
// (typed search vs. reverse geocode from device location) always converges on the same cityId,
// fine enough that two genuinely different nearby cities essentially never collide.
private const val COORD_PRECISION = 100.0

// Nullable so a single malformed entry in a multi-result geocode response (e.g. searching
// "Sydney") can be dropped via mapNotNull instead of poisoning the whole result list.
fun GeocodeResultDto.toDomain(): ResolvedCity? {
    val resolvedLat = lat ?: return null
    val resolvedLon = lon ?: return null
    val resolvedName = name ?: return null
    val resolvedCountry = country ?: return null
    val roundedLat = round(resolvedLat * COORD_PRECISION) / COORD_PRECISION
    val roundedLon = round(resolvedLon * COORD_PRECISION) / COORD_PRECISION
    return ResolvedCity(
        cityId = "$roundedLat,$roundedLon",
        name = resolvedName,
        state = state,
        country = resolvedCountry,
        lat = resolvedLat,
        lon = resolvedLon,
    )
}
