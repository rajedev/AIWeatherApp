package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class RefreshAllSavedCitiesUseCaseImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : RefreshAllSavedCitiesUseCase {

    override suspend fun invoke(): List<Pair<String, Result<Unit>>> = coroutineScope {
        val ids = weatherRepository.getSavedCityIds()
        ids.chunked(SYNC_CHUNK_SIZE).flatMap { chunk ->
            chunk.map { id -> async { id to weatherRepository.refresh(id) } }.awaitAll()
        }
    }

    private companion object {
        const val SYNC_CHUNK_SIZE = 5
    }
}
