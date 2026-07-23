package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.AlertThresholds
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import javax.inject.Inject

internal class SetAlertThresholdsUseCaseImpl @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : SetAlertThresholdsUseCase {
    override suspend fun invoke(thresholds: AlertThresholds) = preferencesRepository.setAlertThresholds(thresholds)
}
