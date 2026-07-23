package com.rajedev.aiweatherapp.data.remote.mapper

import com.rajedev.aiweatherapp.data.remote.dto.CurrentWeatherDto
import com.rajedev.aiweatherapp.domain.model.CurrentConditions

private const val MILLIS_PER_SECOND = 1000L

fun CurrentWeatherDto.toDomain(): CurrentConditions {
    val mainData = main ?: error("Missing main weather data")
    val condition = weather?.firstOrNull()
    val temp = mainData.temp ?: error("Missing temperature")
    return CurrentConditions(
        temp = temp,
        feelsLike = mainData.feelsLike ?: temp,
        tempMin = mainData.tempMin ?: temp,
        tempMax = mainData.tempMax ?: temp,
        humidity = mainData.humidity ?: 0,
        conditionMain = condition?.main ?: "Clear",
        conditionDescription = condition?.description.orEmpty(),
        conditionIcon = condition?.icon.orEmpty(),
        tzOffsetSeconds = timezone ?: 0,
        observedAt = (dt ?: 0L) * MILLIS_PER_SECOND,
    )
}
