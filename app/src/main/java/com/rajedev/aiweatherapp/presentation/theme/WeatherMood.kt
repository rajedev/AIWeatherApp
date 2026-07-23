package com.rajedev.aiweatherapp.presentation.theme

enum class WeatherMood { HOT, SUNNY, RAINY, CLOUDY, WINTER, NIGHT }

private const val NIGHT_START_HOUR = 6
private const val NIGHT_END_HOUR = 18
private const val HOT_TEMP_THRESHOLD_C = 35.0
private const val WINTER_TEMP_THRESHOLD_C = 5.0
private val NIGHT_ELIGIBLE_CONDITIONS = setOf("Clear", "Clouds")
private val RAINY_CONDITIONS = setOf("Rain", "Drizzle", "Thunderstorm")

// ONE function, pure and deterministic - every screen/component derives its mood through this,
// never a per-screen if/when block re-deriving color logic locally.
fun resolveMood(conditionMain: String, currentTemp: Double, localHour: Int): WeatherMood = when {
    localHour !in NIGHT_START_HOUR..NIGHT_END_HOUR && conditionMain in NIGHT_ELIGIBLE_CONDITIONS -> WeatherMood.NIGHT
    currentTemp >= HOT_TEMP_THRESHOLD_C -> WeatherMood.HOT
    currentTemp <= WINTER_TEMP_THRESHOLD_C || conditionMain == "Snow" -> WeatherMood.WINTER
    conditionMain in RAINY_CONDITIONS -> WeatherMood.RAINY
    conditionMain == "Clouds" -> WeatherMood.CLOUDY
    else -> WeatherMood.SUNNY
}
