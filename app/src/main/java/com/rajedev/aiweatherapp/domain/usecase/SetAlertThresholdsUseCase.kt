package com.rajedev.aiweatherapp.domain.usecase

import com.rajedev.aiweatherapp.domain.model.AlertThresholds

interface SetAlertThresholdsUseCase {
    suspend operator fun invoke(thresholds: AlertThresholds)
}
