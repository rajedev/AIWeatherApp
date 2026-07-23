package com.rajedev.aiweatherapp.domain.model

data class SavedCity(
    val resolvedCity: ResolvedCity,
    val sortOrder: Int,
    val lastFetchedAt: Long,
    val current: CurrentConditions,
    val isStale: Boolean,
)
