package com.rajedev.aiweatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SysDto(
    val country: String? = null,
)
