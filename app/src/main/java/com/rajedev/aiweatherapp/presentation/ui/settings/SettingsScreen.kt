package com.rajedev.aiweatherapp.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rajedev.aiweatherapp.R
import com.rajedev.aiweatherapp.domain.model.ThemeMode
import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import com.rajedev.aiweatherapp.notification.NotificationPermissionHandler
import com.rajedev.aiweatherapp.presentation.common.celsiusToDisplayUnit
import com.rajedev.aiweatherapp.presentation.common.displayUnitToCelsius
import com.rajedev.aiweatherapp.ui.theme.AIWeatherAppTheme
import kotlin.math.roundToInt

@Composable
fun SettingsRoute(onBack: () -> Unit) {
    SettingsScreen(onBack = onBack, viewModel = hiltViewModel())
}

@Composable
internal fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingNotificationPermissionRequest by remember { mutableStateOf(false) }

    NotificationPermissionHandler(
        shouldRequest = pendingNotificationPermissionRequest,
        onResult = {
            pendingNotificationPermissionRequest = false
            viewModel.onAction(SettingsAction.ToggleAlerts(enabled = true))
        },
    )

    SettingsContent(
        uiState = uiState,
        onAction = { action ->
            if (action is SettingsAction.ToggleAlerts && action.enabled) {
                pendingNotificationPermissionRequest = true
            } else {
                viewModel.onAction(action)
            }
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.content_description_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = stringResource(R.string.settings_units_label), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.unit == WeatherUnit.METRIC,
                    onClick = { onAction(SettingsAction.SetUnit(WeatherUnit.METRIC)) },
                    label = { Text(stringResource(R.string.settings_units_metric)) },
                )
                FilterChip(
                    selected = uiState.unit == WeatherUnit.IMPERIAL,
                    onClick = { onAction(SettingsAction.SetUnit(WeatherUnit.IMPERIAL)) },
                    label = { Text(stringResource(R.string.settings_units_imperial)) },
                )
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.settings_theme_label), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.themeMode == ThemeMode.SYSTEM,
                    onClick = { onAction(SettingsAction.SetThemeMode(ThemeMode.SYSTEM)) },
                    label = { Text(stringResource(R.string.settings_theme_system)) },
                )
                FilterChip(
                    selected = uiState.themeMode == ThemeMode.LIGHT,
                    onClick = { onAction(SettingsAction.SetThemeMode(ThemeMode.LIGHT)) },
                    label = { Text(stringResource(R.string.settings_theme_light)) },
                )
                FilterChip(
                    selected = uiState.themeMode == ThemeMode.DARK,
                    onClick = { onAction(SettingsAction.SetThemeMode(ThemeMode.DARK)) },
                    label = { Text(stringResource(R.string.settings_theme_dark)) },
                )
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.settings_alerts_label), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = uiState.alertsEnabled,
                    onCheckedChange = { enabled -> onAction(SettingsAction.ToggleAlerts(enabled)) },
                )
            }

            val unitSymbol = stringResource(
                if (uiState.unit == WeatherUnit.METRIC) R.string.unit_symbol_celsius else R.string.unit_symbol_fahrenheit,
            )
            val highTempDisplay = celsiusToDisplayUnit(uiState.highTempC, uiState.unit)
            val lowTempDisplay = celsiusToDisplayUnit(uiState.lowTempC, uiState.unit)

            ThresholdSlider(
                label = stringResource(R.string.settings_high_temp_label, highTempDisplay.roundToInt(), unitSymbol),
                value = highTempDisplay,
                range = celsiusRangeToDisplay(HIGH_TEMP_RANGE_C, uiState.unit),
                onValueChange = {
                    onAction(SettingsAction.SetHighTempThreshold(displayUnitToCelsius(it, uiState.unit)))
                },
            )
            ThresholdSlider(
                label = stringResource(R.string.settings_low_temp_label, lowTempDisplay.roundToInt(), unitSymbol),
                value = lowTempDisplay,
                range = celsiusRangeToDisplay(LOW_TEMP_RANGE_C, uiState.unit),
                onValueChange = {
                    onAction(SettingsAction.SetLowTempThreshold(displayUnitToCelsius(it, uiState.unit)))
                },
            )
        }
    }
}

@Composable
private fun ThresholdSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range,
        )
    }
}

// Threshold range bounds are defined in Celsius; converted to the display unit at read time so
// the slider's draggable range (and the value shown in its label) always matches uiState.unit.
private val HIGH_TEMP_RANGE_C = 25f..45f
private val LOW_TEMP_RANGE_C = (-10f)..15f

private fun celsiusRangeToDisplay(rangeC: ClosedFloatingPointRange<Float>, unit: WeatherUnit): ClosedFloatingPointRange<Float> {
    val start = celsiusToDisplayUnit(rangeC.start.toDouble(), unit).toFloat()
    val end = celsiusToDisplayUnit(rangeC.endInclusive.toDouble(), unit).toFloat()
    return if (start <= end) start..end else end..start
}

@PreviewLightDark
@Composable
private fun SettingsContentPreview() {
    AIWeatherAppTheme {
        SettingsContent(
            uiState = SettingsUiState(
                unit = WeatherUnit.METRIC,
                highTempC = 35.0,
                lowTempC = 5.0,
                alertsEnabled = true,
                themeMode = ThemeMode.SYSTEM,
            ),
            onAction = {},
            onBack = {},
        )
    }
}
