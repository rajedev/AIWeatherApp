package com.rajedev.aiweatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rajedev.aiweatherapp.data.local.entity.AlertStateEntity

@Dao
interface AlertStateDao {

    @Query("SELECT * FROM alert_state WHERE cityId = :cityId")
    suspend fun get(cityId: String): AlertStateEntity?

    @Upsert
    suspend fun upsert(entity: AlertStateEntity)

    @Query("DELETE FROM alert_state WHERE cityId = :cityId")
    suspend fun clear(cityId: String)
}
