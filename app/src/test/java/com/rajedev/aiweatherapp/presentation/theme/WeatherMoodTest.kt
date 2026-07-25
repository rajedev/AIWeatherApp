package com.rajedev.aiweatherapp.presentation.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMoodTest {

    @Test
    fun `night eligible condition outside daytime hours resolves to NIGHT`() {
        assertEquals(WeatherMood.NIGHT, resolveMood("Clear", currentTemp = 20.0, localHour = 22))
        assertEquals(WeatherMood.NIGHT, resolveMood("Clouds", currentTemp = 20.0, localHour = 2))
    }

    @Test
    fun `hour 6 and hour 18 are inside the daytime range, not NIGHT`() {
        assertEquals(WeatherMood.SUNNY, resolveMood("Clear", currentTemp = 20.0, localHour = 6))
        assertEquals(WeatherMood.SUNNY, resolveMood("Clear", currentTemp = 20.0, localHour = 18))
    }

    @Test
    fun `night ineligible condition outside daytime hours does not resolve to NIGHT`() {
        assertEquals(WeatherMood.RAINY, resolveMood("Rain", currentTemp = 20.0, localHour = 23))
    }

    @Test
    fun `temp at or above 35 resolves to HOT regardless of condition`() {
        assertEquals(WeatherMood.HOT, resolveMood("Clear", currentTemp = 35.0, localHour = 12))
        assertEquals(WeatherMood.HOT, resolveMood("Clear", currentTemp = 40.0, localHour = 12))
    }

    @Test
    fun `temp at or below 5 resolves to WINTER`() {
        assertEquals(WeatherMood.WINTER, resolveMood("Clear", currentTemp = 5.0, localHour = 12))
        assertEquals(WeatherMood.WINTER, resolveMood("Clear", currentTemp = -10.0, localHour = 12))
    }

    @Test
    fun `snow condition resolves to WINTER even at a mild temperature`() {
        assertEquals(WeatherMood.WINTER, resolveMood("Snow", currentTemp = 10.0, localHour = 12))
    }

    @Test
    fun `rainy conditions resolve to RAINY`() {
        assertEquals(WeatherMood.RAINY, resolveMood("Rain", currentTemp = 20.0, localHour = 12))
        assertEquals(WeatherMood.RAINY, resolveMood("Drizzle", currentTemp = 20.0, localHour = 12))
        assertEquals(WeatherMood.RAINY, resolveMood("Thunderstorm", currentTemp = 20.0, localHour = 12))
    }

    @Test
    fun `clouds during daytime resolves to CLOUDY`() {
        assertEquals(WeatherMood.CLOUDY, resolveMood("Clouds", currentTemp = 20.0, localHour = 12))
    }

    @Test
    fun `unrecognized condition at a mild daytime temperature defaults to SUNNY`() {
        assertEquals(WeatherMood.SUNNY, resolveMood("Clear", currentTemp = 20.0, localHour = 12))
        assertEquals(WeatherMood.SUNNY, resolveMood("Mist", currentTemp = 20.0, localHour = 12))
    }
}
