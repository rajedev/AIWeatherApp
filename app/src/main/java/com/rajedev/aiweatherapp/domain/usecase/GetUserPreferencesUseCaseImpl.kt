package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.UserPreferences
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class GetUserPreferencesUseCaseImpl @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : GetUserPreferencesUseCase {
    override fun invoke(): Flow<UserPreferences> = preferencesRepository.observePreferences()
}
