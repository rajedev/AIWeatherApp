package com.rajedev.aiweatherapp.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rajedev.aiweatherapp.data.local.dao.AlertStateDao
import com.rajedev.aiweatherapp.data.local.entity.AlertStateEntity
import com.rajedev.aiweatherapp.domain.alert.WeatherAlertEvaluator
import com.rajedev.aiweatherapp.domain.model.AlertThresholds
import com.rajedev.aiweatherapp.domain.model.WeatherAlert
import com.rajedev.aiweatherapp.domain.repository.PreferencesRepository
import com.rajedev.aiweatherapp.domain.repository.WeatherRepository
import com.rajedev.aiweatherapp.domain.usecase.RefreshAllSavedCitiesUseCase
import com.rajedev.aiweatherapp.notification.WeatherAlertNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refreshAllSavedCitiesUseCase: RefreshAllSavedCitiesUseCase,
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val weatherAlertEvaluator: WeatherAlertEvaluator,
    private val alertStateDao: AlertStateDao,
    private val weatherAlertNotifier: WeatherAlertNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val outcomes = refreshAllSavedCitiesUseCase()
        if (outcomes.isEmpty()) return Result.success()

        val thresholds = preferencesRepository.observePreferences().first().thresholds
        if (thresholds.alertsEnabled) {
            outcomes.filter { (_, result) -> result.isSuccess }
                .forEach { (cityId, _) -> evaluateAndNotify(cityId, thresholds) }
        }

        // Partial failure shouldn't trigger a full retry storm - only retry if every city failed.
        val allFailed = outcomes.all { (_, result) -> result.isFailure }
        return if (allFailed) Result.retry() else Result.success()
    }

    private suspend fun evaluateAndNotify(cityId: String, thresholds: AlertThresholds) {
        val savedCity = weatherRepository.observeCity(cityId).first() ?: return
        val alert = weatherAlertEvaluator.evaluate(savedCity.current, thresholds, cityId)
        dispatchWithDebounce(cityId, alert, savedCity.resolvedCity.name)
    }

    private suspend fun dispatchWithDebounce(cityId: String, alert: WeatherAlert?, cityName: String) {
        val lastState = alertStateDao.get(cityId)
        if (alert == null) {
            // Condition resolved - clear debounce state so the next distinct occurrence isn't
            // suppressed by a stale cooldown.
            if (lastState != null) alertStateDao.clear(cityId)
            return
        }
        val sameType = lastState?.lastAlertType == alert::class.simpleName
        val withinCooldown = lastState != null &&
            (System.currentTimeMillis() - lastState.lastAlertedAt) < ALERT_COOLDOWN_MS
        if (sameType && withinCooldown) return

        weatherAlertNotifier.showAlert(alert, cityName)
        alertStateDao.upsert(AlertStateEntity(cityId, alert::class.simpleName.orEmpty(), System.currentTimeMillis()))
    }

    private companion object {
        const val ALERT_COOLDOWN_MS = 6 * 60 * 60 * 1000L
    }
}
