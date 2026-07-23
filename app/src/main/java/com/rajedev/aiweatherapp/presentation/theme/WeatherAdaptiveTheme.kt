package com.rajedev.aiweatherapp.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue

private const val CROSSFADE_DURATION_MS = 600

val LocalWeatherTheme = compositionLocalOf { WeatherMood.SUNNY.toThemeColors() }

// Wraps the app/nav root once - every descendant screen reads LocalWeatherTheme.current instead
// of re-deriving its own colors. 600ms crossfade so switching saved cities never hard-cuts.
@Composable
fun WeatherAdaptiveTheme(mood: WeatherMood, content: @Composable () -> Unit) {
    val target = mood.toThemeColors()
    val animatedPrimary by animateColorAsState(target.primary, tween(CROSSFADE_DURATION_MS), label = "weatherPrimary")
    val animatedTint by animateColorAsState(target.surfaceTint, tween(CROSSFADE_DURATION_MS), label = "weatherTint")

    CompositionLocalProvider(
        LocalWeatherTheme provides target.copy(primary = animatedPrimary, surfaceTint = animatedTint),
    ) {
        content()
    }
}
