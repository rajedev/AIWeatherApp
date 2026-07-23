package com.rajedev.aiweatherapp.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.model.SavedCity
import com.rajedev.aiweatherapp.domain.usecase.ObserveSavedCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toLocalDateTime

private const val MOOD_SHARING_TIMEOUT_MS = 5000L

@OptIn(ExperimentalTime::class)
@HiltViewModel
class AppThemeViewModel @Inject constructor(
    observeSavedCitiesUseCase: ObserveSavedCitiesUseCase,
) : ViewModel() {

    // Follows the first saved city by default - a simpler, sufficient choice for v1 rather than
    // tracking exactly which city-detail screen is currently on top of the nav back stack.
    val mood = observeSavedCitiesUseCase()
        .map { cities -> resolveMoodFor(cities.firstOrNull()) }
        .catch { emit(WeatherMood.SUNNY) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(MOOD_SHARING_TIMEOUT_MS), WeatherMood.SUNNY)

    private fun resolveMoodFor(city: SavedCity?): WeatherMood {
        val current = city?.current ?: return WeatherMood.SUNNY
        val zone = UtcOffset(seconds = current.tzOffsetSeconds).asTimeZone()
        val localHour = Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(zone).hour
        return resolveMood(current.conditionMain, current.temp, localHour)
    }
}
