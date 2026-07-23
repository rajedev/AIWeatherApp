package com.rajedev.aiweatherapp.presentation.ui.currentconditions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rajedev.aiweatherapp.R
import com.rajedev.aiweatherapp.domain.model.CurrentConditions
import com.rajedev.aiweatherapp.domain.model.HourlyForecast
import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import com.rajedev.aiweatherapp.presentation.common.StaleDataBanner
import com.rajedev.aiweatherapp.presentation.common.formatHour
import com.rajedev.aiweatherapp.presentation.common.formatTemperature
import com.rajedev.aiweatherapp.presentation.common.weatherIconFor
import com.rajedev.aiweatherapp.presentation.theme.LocalWeatherTheme
import com.rajedev.aiweatherapp.ui.theme.AIWeatherAppTheme

@Composable
fun CityDetailRoute(cityId: String, onOpenDailyForecast: () -> Unit, onBack: () -> Unit) {
    CityDetailScreen(
        cityId = cityId,
        onOpenDailyForecast = onOpenDailyForecast,
        onBack = onBack,
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun CityDetailScreen(
    cityId: String,
    onOpenDailyForecast: () -> Unit,
    onBack: () -> Unit,
    viewModel: CityDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cityId) { viewModel.onAction(CityDetailAction.LoadCity(cityId)) }

    CityDetailContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onOpenDailyForecast = onOpenDailyForecast,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDetailContent(
    uiState: CityDetailUiState,
    onAction: (CityDetailAction) -> Unit,
    onOpenDailyForecast: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.displayName.ifEmpty { stringResource(R.string.app_name) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.content_description_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(CityDetailAction.Refresh) }) {
                        Icon(Icons.Filled.Refresh, stringResource(R.string.content_description_refresh))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isStale) StaleDataBanner(modifier = Modifier.fillMaxWidth())
            if (uiState.isLoading && uiState.current == null) {
                Row(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.current?.let { current ->
                HeroCard(current = current, unit = uiState.unit, onOpenDailyForecast = onOpenDailyForecast)
            }
            if (uiState.hourly.isNotEmpty()) {
                HourlyForecastRow(
                    hourly = uiState.hourly,
                    unit = uiState.unit,
                    tzOffsetSeconds = uiState.current?.tzOffsetSeconds ?: 0,
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    current: CurrentConditions,
    unit: WeatherUnit,
    onOpenDailyForecast: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpenDailyForecast,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalWeatherTheme.current.surfaceTint),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = LocalWeatherTheme.current.icon,
                contentDescription = current.conditionMain,
                tint = LocalWeatherTheme.current.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(text = formatTemperature(current.temp, unit), style = MaterialTheme.typography.displayLarge)
            Text(text = current.conditionDescription, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Feels like ${formatTemperature(current.feelsLike, unit)}  " +
                    "H:${formatTemperature(current.tempMax, unit)}  L:${formatTemperature(current.tempMin, unit)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun HourlyForecastRow(
    hourly: List<HourlyForecast>,
    unit: WeatherUnit,
    tzOffsetSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.hourly_forecast_heading),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(hourly) { hour ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatHour(hour.timestamp, tzOffsetSeconds), style = MaterialTheme.typography.labelMedium)
                    Icon(
                        imageVector = weatherIconFor(hour.conditionMain),
                        contentDescription = hour.conditionMain,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Text(text = formatTemperature(hour.temp, unit), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun previewUiState() = CityDetailUiState(
    cityId = "12.97,77.59",
    displayName = "Bengaluru",
    current = CurrentConditions(
        temp = 28.0,
        feelsLike = 30.0,
        tempMin = 22.0,
        tempMax = 31.0,
        humidity = 60,
        conditionMain = "Clouds",
        conditionDescription = "scattered clouds",
        conditionIcon = "03d",
        tzOffsetSeconds = 19800,
        observedAt = 0L,
    ),
    hourly = listOf(
        HourlyForecast(0L, 28.0, "Clouds", "03d"),
        HourlyForecast(3_600_000L, 27.0, "Clear", "01d"),
    ),
)

@PreviewLightDark
@Composable
private fun CityDetailContentPreview() {
    AIWeatherAppTheme {
        CityDetailContent(
            uiState = previewUiState(),
            onAction = {},
            onOpenDailyForecast = {},
            onBack = {},
        )
    }
}
