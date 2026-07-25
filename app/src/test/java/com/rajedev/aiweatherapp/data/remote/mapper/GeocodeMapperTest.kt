package com.rajedev.aiweatherapp.data.remote.mapper

import com.rajedev.aiweatherapp.data.remote.dto.GeocodeResultDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private fun completeDto(
    name: String? = "Sydney",
    lat: Double? = -33.8688,
    lon: Double? = 151.2093,
    country: String? = "AU",
    state: String? = "New South Wales",
) = GeocodeResultDto(name = name, lat = lat, lon = lon, country = country, state = state)

class GeocodeMapperTest {

    @Test
    fun `missing latitude maps to null`() {
        assertNull(completeDto(lat = null).toDomain())
    }

    @Test
    fun `missing longitude maps to null`() {
        assertNull(completeDto(lon = null).toDomain())
    }

    @Test
    fun `missing name maps to null`() {
        assertNull(completeDto(name = null).toDomain())
    }

    @Test
    fun `missing country maps to null`() {
        assertNull(completeDto(country = null).toDomain())
    }

    @Test
    fun `complete dto maps to a fully populated ResolvedCity`() {
        val resolved = completeDto().toDomain()

        assertEquals("Sydney", resolved?.name)
        assertEquals("AU", resolved?.country)
        assertEquals("New South Wales", resolved?.state)
        assertEquals(-33.8688, resolved?.lat)
        assertEquals(151.2093, resolved?.lon)
        assertEquals("-33.87,151.21", resolved?.cityId)
    }

    @Test
    fun `null state is preserved rather than defaulted`() {
        val resolved = completeDto(state = null).toDomain()

        assertNull(resolved?.state)
    }

    @Test
    fun `coordinates round to 2 decimal places for the cityId grid`() {
        val resolved = completeDto(lat = -33.86123, lon = 151.20987).toDomain()

        assertEquals("-33.86,151.21", resolved?.cityId)
    }
}
