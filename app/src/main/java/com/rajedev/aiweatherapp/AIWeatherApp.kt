package com.rajedev.aiweatherapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.rajedev.aiweatherapp.notification.WeatherAlertNotifier
import com.rajedev.aiweatherapp.sync.WeatherSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AIWeatherApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var syncScheduler: WeatherSyncScheduler

    @Inject lateinit var alertNotifier: WeatherAlertNotifier

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        alertNotifier.ensureChannel()
        syncScheduler.schedule()
    }
}
