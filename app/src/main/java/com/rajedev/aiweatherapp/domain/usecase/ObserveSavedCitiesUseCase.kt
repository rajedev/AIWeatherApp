package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.SavedCity
import kotlinx.coroutines.flow.Flow

interface ObserveSavedCitiesUseCase {
    operator fun invoke(): Flow<List<SavedCity>>
}
