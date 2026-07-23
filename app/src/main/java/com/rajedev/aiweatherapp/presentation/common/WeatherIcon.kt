package com.rajedev.aiweatherapp.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

// Independent of the app-wide weather mood - a single hourly/daily slice must reflect its own
// actual condition, not the current screen's overall theme mood.
fun weatherIconFor(conditionMain: String): ImageVector = when (conditionMain) {
    "Clear" -> Icons.Filled.WbSunny
    "Clouds" -> Icons.Filled.Cloud
    "Rain", "Drizzle" -> Icons.Filled.Umbrella
    "Thunderstorm" -> Icons.Filled.Bolt
    "Snow" -> Icons.Filled.AcUnit
    else -> Icons.Filled.WaterDrop
}
