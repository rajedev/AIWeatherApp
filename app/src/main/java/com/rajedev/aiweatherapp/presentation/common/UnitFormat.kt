package com.rajedev.aiweatherapp.presentation.common

import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import kotlin.math.roundToInt

private const val FAHRENHEIT_PER_CELSIUS = 9.0 / 5.0
private const val FAHRENHEIT_OFFSET = 32.0

// All stored temperatures are canonical Celsius (the repository always fetches with
// units=metric) - this is the one place that converts to Fahrenheit for display.
fun celsiusToDisplayUnit(tempCelsius: Double, unit: WeatherUnit): Double = when (unit) {
    WeatherUnit.METRIC -> tempCelsius
    WeatherUnit.IMPERIAL -> tempCelsius * FAHRENHEIT_PER_CELSIUS + FAHRENHEIT_OFFSET
}

fun displayUnitToCelsius(value: Double, unit: WeatherUnit): Double = when (unit) {
    WeatherUnit.METRIC -> value
    WeatherUnit.IMPERIAL -> (value - FAHRENHEIT_OFFSET) / FAHRENHEIT_PER_CELSIUS
}

fun formatTemperature(tempCelsius: Double, unit: WeatherUnit): String {
    val symbol = when (unit) {
        WeatherUnit.METRIC -> "°C"
        WeatherUnit.IMPERIAL -> "°F"
    }
    return "${celsiusToDisplayUnit(tempCelsius, unit).roundToInt()}$symbol"
}
