package com.rajedev.aiweatherapp.presentation.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.rajedev.aiweatherapp.R
import com.rajedev.aiweatherapp.domain.model.DailyForecast
import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import com.rajedev.aiweatherapp.presentation.common.formatTemperature
import com.rajedev.aiweatherapp.presentation.common.weatherIconFor
import com.rajedev.aiweatherapp.ui.theme.AIWeatherAppTheme
import kotlinx.datetime.LocalDate

@Composable
fun DailyForecastRoute(cityId: String, onBack: () -> Unit) {
    DailyForecastScreen(cityId = cityId, onBack = onBack, viewModel = hiltViewModel())
}

@Composable
internal fun DailyForecastScreen(cityId: String, onBack: () -> Unit, viewModel: DailyForecastViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cityId) { viewModel.onAction(DailyForecastAction.LoadCity(cityId)) }

    DailyForecastContent(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyForecastContent(
    uiState: DailyForecastUiState,
    onAction: (DailyForecastAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.daily_forecast_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.content_description_back))
                    }
                },
            )
        },
    ) { padding ->
        // List first (fixed 5 rows, no lazy loading needed), chart below expanding to fill
        // whatever vertical space remains - avoids a large empty gap at the bottom of the page.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.dailyForecasts.forEach { daily ->
                DailyForecastRow(daily = daily, unit = uiState.unit)
            }
            if (uiState.dailyForecasts.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                TrendChart(
                    dailyForecasts = uiState.dailyForecasts,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TrendChart(dailyForecasts: List<DailyForecast>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyForecasts) {
        modelProducer.runTransaction {
            lineModel {
                series(dailyForecasts.map { it.tempMin })
                series(dailyForecasts.map { it.tempMax })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun DailyForecastRow(daily: DailyForecast, unit: WeatherUnit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = daily.date.toString(), style = MaterialTheme.typography.bodyLarge)
        Icon(imageVector = weatherIconFor(daily.dominantCondition), contentDescription = daily.dominantCondition)
        Text(
            text = "${formatTemperature(daily.tempMin, unit)} / ${formatTemperature(daily.tempMax, unit)}",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@PreviewLightDark
@Composable
private fun DailyForecastContentPreview() {
    AIWeatherAppTheme {
        DailyForecastContent(
            uiState = DailyForecastUiState(
                cityId = "12.97,77.59",
                dailyForecasts = (0..4).map { offset ->
                    DailyForecast(
                        date = LocalDate(2026, 7, 24 + offset),
                        tempMin = 20.0 + offset,
                        tempMax = 30.0 + offset,
                        dominantCondition = "Clear",
                        icon = "01d",
                    )
                },
            ),
            onAction = {},
            onBack = {},
        )
    }
}
