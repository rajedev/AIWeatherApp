package com.rajedev.aiweatherapp.presentation.common

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

// 2024-01-01T00:00:00Z
private const val MIDNIGHT_UTC_MILLIS = 1_704_067_200_000L
private const val HOURS_5_IN_SECONDS = 5 * 3600

class WeatherIconTest {

    @Test
    fun `zero offset returns the UTC hour unchanged`() {
        assertEquals(0, localHourFrom(MIDNIGHT_UTC_MILLIS, tzOffsetSeconds = 0))
    }

    @Test
    fun `positive offset shifts the hour forward within the same day`() {
        assertEquals(5, localHourFrom(MIDNIGHT_UTC_MILLIS, tzOffsetSeconds = HOURS_5_IN_SECONDS))
    }

    @Test
    fun `positive offset can roll the local hour into the next UTC day`() {
        val tenPmUtc = MIDNIGHT_UTC_MILLIS + 22 * 3600 * 1000L // 2024-01-01T22:00:00Z

        assertEquals(3, localHourFrom(tenPmUtc, tzOffsetSeconds = HOURS_5_IN_SECONDS))
    }

    @Test
    fun `negative offset can roll the local hour into the previous UTC day`() {
        val twoAmUtc = MIDNIGHT_UTC_MILLIS + 2 * 3600 * 1000L // 2024-01-01T02:00:00Z

        assertEquals(21, localHourFrom(twoAmUtc, tzOffsetSeconds = -HOURS_5_IN_SECONDS))
    }

    @Test
    fun `weatherTintFor matches the mood-derived primary color for a hot condition`() {
        assertEquals(Color(0xFFD85A30), weatherTintFor("Clear", temp = 40.0, localHour = 12))
    }

    @Test
    fun `weatherTintFor matches the mood-derived primary color for a rainy condition`() {
        assertEquals(Color(0xFF185FA5), weatherTintFor("Rain", temp = 20.0, localHour = 12))
    }

    @Test
    fun `weatherTintFor matches the mood-derived primary color for a winter condition`() {
        assertEquals(Color(0xFF0F6E56), weatherTintFor("Snow", temp = 0.0, localHour = 12))
    }
}
