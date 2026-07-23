package com.rajedev.aiweatherapp.domain.alert

import com.rajedev.aiweatherapp.domain.model.AlertThresholds
import com.rajedev.aiweatherapp.domain.model.CurrentConditions
import com.rajedev.aiweatherapp.domain.model.WeatherAlert
import javax.inject.Inject

class WeatherAlertEvaluator @Inject constructor() {

    fun evaluate(conditions: CurrentConditions, thresholds: AlertThresholds, cityId: String): WeatherAlert? = when {
        !thresholds.alertsEnabled -> null
        conditions.temp >= thresholds.highTempC -> WeatherAlert.ExtremeHeat(cityId, conditions.temp)
        conditions.temp <= thresholds.lowTempC -> WeatherAlert.ExtremeCold(cityId, conditions.temp)
        conditions.conditionMain in SEVERE_CONDITIONS -> WeatherAlert.SevereCondition(cityId, conditions.conditionMain)
        else -> null
    }

    private companion object {
        val SEVERE_CONDITIONS = setOf("Thunderstorm", "Tornado", "Squall")
    }
}
