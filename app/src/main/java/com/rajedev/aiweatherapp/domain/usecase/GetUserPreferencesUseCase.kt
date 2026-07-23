package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface GetUserPreferencesUseCase {
    operator fun invoke(): Flow<UserPreferences>
}
