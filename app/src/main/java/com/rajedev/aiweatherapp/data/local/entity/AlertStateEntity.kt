package com.rajedev.aiweatherapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_state")
data class AlertStateEntity(
    @PrimaryKey val cityId: String,
    @ColumnInfo(name = "last_alert_type") val lastAlertType: String,
    @ColumnInfo(name = "last_alerted_at") val lastAlertedAt: Long,
)
