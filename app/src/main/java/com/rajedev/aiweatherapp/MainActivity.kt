package com.rajedev.aiweatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rajedev.aiweatherapp.presentation.navigation.AppNavHost
import com.rajedev.aiweatherapp.presentation.theme.AppThemeViewModel
import com.rajedev.aiweatherapp.presentation.theme.WeatherAdaptiveTheme
import com.rajedev.aiweatherapp.ui.theme.AIWeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: AppThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIWeatherAppTheme {
                val mood by themeViewModel.mood.collectAsStateWithLifecycle()
                WeatherAdaptiveTheme(mood = mood) {
                    AppNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
