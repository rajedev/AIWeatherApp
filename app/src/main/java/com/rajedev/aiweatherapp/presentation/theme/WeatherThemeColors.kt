package com.rajedev.aiweatherapp.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val surfaceTint: Color,
    val icon: ImageVector,
)

// One mapping, semantic not decorative: warm ramps for heat, cool for cold/rain, neutral for
// overcast. The icon always accompanies the color - never encode mood by color alone.
fun WeatherMood.toThemeColors(): WeatherThemeColors = when (this) {
    WeatherMood.HOT -> WeatherThemeColors(
        Color(0xFFD85A30),
        Color.White,
        Color(0xFFFAECE7),
        Icons.Filled.LocalFireDepartment,
    )
    WeatherMood.SUNNY -> WeatherThemeColors(Color(0xFFBA7517), Color.White, Color(0xFFFAEEDA), Icons.Filled.WbSunny)
    WeatherMood.RAINY -> WeatherThemeColors(Color(0xFF185FA5), Color.White, Color(0xFFE6F1FB), Icons.Filled.Umbrella)
    WeatherMood.CLOUDY -> WeatherThemeColors(Color(0xFF5F5E5A), Color.White, Color(0xFFF1EFE8), Icons.Filled.Cloud)
    WeatherMood.WINTER -> WeatherThemeColors(Color(0xFF0F6E56), Color.White, Color(0xFFE1F5EE), Icons.Filled.AcUnit)
    WeatherMood.NIGHT -> WeatherThemeColors(
        Color(0xFF3C3489),
        Color.White,
        Color(0xFFEEEDFE),
        Icons.Filled.NightsStay,
    )
}
