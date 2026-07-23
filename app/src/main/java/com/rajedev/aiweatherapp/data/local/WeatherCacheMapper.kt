package com.rajedev.aiweatherapp.data.local

import com.rajedev.aiweatherapp.data.local.entity.WeatherCacheEntity
import com.rajedev.aiweatherapp.domain.model.CurrentConditions
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.model.SavedCity

fun WeatherCacheEntity.toResolvedCity(): ResolvedCity = ResolvedCity(
    cityId = cityId,
    name = displayName,
    state = state,
    country = country,
    lat = lat,
    lon = lon,
)

fun WeatherCacheEntity.toCurrentConditions(): CurrentConditions = CurrentConditions(
    temp = currentTemp,
    feelsLike = feelsLike,
    tempMin = tempMin,
    tempMax = tempMax,
    humidity = humidity,
    conditionMain = conditionMain,
    conditionDescription = conditionDescription,
    conditionIcon = conditionIcon,
    tzOffsetSeconds = tzOffsetSeconds,
    observedAt = lastFetchedAt,
)

fun WeatherCacheEntity.toSavedCity(isStale: Boolean): SavedCity = SavedCity(
    resolvedCity = toResolvedCity(),
    sortOrder = sortOrder,
    lastFetchedAt = lastFetchedAt,
    current = toCurrentConditions(),
    isStale = isStale,
)
