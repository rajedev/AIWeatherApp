package com.rajedev.aiweatherapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rajedev.aiweatherapp.R
import com.rajedev.aiweatherapp.domain.model.WeatherAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherAlertNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_weather_alerts),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_weather_alerts_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Gracefully skips the OS notification if permission was denied - debounce state is still
    // recorded by the caller regardless, so in-app badging keeps working either way.
    fun showAlert(alert: WeatherAlert, cityName: String) {
        val hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val (title, text) = displayTextFor(alert, cityName)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(alert.cityId.hashCode(), notification)
    }

    private fun displayTextFor(alert: WeatherAlert, cityName: String): Pair<String, String> = when (alert) {
        is WeatherAlert.ExtremeHeat ->
            context.getString(R.string.alert_extreme_heat_title) to
                context.getString(R.string.alert_extreme_heat_body, cityName, alert.temp)
        is WeatherAlert.ExtremeCold ->
            context.getString(R.string.alert_extreme_cold_title) to
                context.getString(R.string.alert_extreme_cold_body, cityName, alert.temp)
        is WeatherAlert.SevereCondition ->
            context.getString(R.string.alert_severe_condition_title) to
                context.getString(R.string.alert_severe_condition_body, cityName, alert.conditionMain)
    }

    private companion object {
        const val CHANNEL_ID = "weather_alerts"
    }
}
