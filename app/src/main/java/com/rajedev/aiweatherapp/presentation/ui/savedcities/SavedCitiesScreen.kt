package com.rajedev.aiweatherapp.presentation.ui.savedcities

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rajedev.aiweatherapp.R
import com.rajedev.aiweatherapp.domain.model.CurrentConditions
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.model.SavedCity
import com.rajedev.aiweatherapp.domain.model.WeatherUnit
import com.rajedev.aiweatherapp.presentation.common.LocationPermissionHandler
import com.rajedev.aiweatherapp.presentation.common.StaleDataBanner
import com.rajedev.aiweatherapp.presentation.common.formatTemperature
import com.rajedev.aiweatherapp.presentation.common.localHourFrom
import com.rajedev.aiweatherapp.presentation.common.weatherIconFor
import com.rajedev.aiweatherapp.presentation.common.weatherTintFor
import com.rajedev.aiweatherapp.ui.theme.AIWeatherAppTheme

@Composable
fun SavedCitiesRoute(onOpenCity: (String) -> Unit, onAddCity: () -> Unit, onOpenSettings: () -> Unit) {
    SavedCitiesScreen(
        onOpenCity = onOpenCity,
        onAddCity = onAddCity,
        onOpenSettings = onOpenSettings,
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun SavedCitiesScreen(
    onOpenCity: (String) -> Unit,
    onAddCity: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SavedCitiesViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingLocationPermissionRequest by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                SavedCitiesUiEvent.RequestLocationPermission -> pendingLocationPermissionRequest = true
                is SavedCitiesUiEvent.CityAdded -> onOpenCity(event.cityId)
                SavedCitiesUiEvent.OpenLocationSettings ->
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onAction(SavedCitiesAction.ConsumeError)
    }

    LocationPermissionHandler(
        shouldRequest = pendingLocationPermissionRequest,
        onGranted = {
            pendingLocationPermissionRequest = false
            viewModel.onAction(SavedCitiesAction.LocationPermissionResult(granted = true))
        },
        onDenied = {
            pendingLocationPermissionRequest = false
            viewModel.onAction(SavedCitiesAction.LocationPermissionResult(granted = false))
        },
    )

    if (uiState.showLocationServicesDisabledDialog) {
        LocationServicesDisabledDialog(
            onConfirm = { viewModel.onAction(SavedCitiesAction.OpenLocationSettingsRequested) },
            onDismiss = { viewModel.onAction(SavedCitiesAction.DismissLocationServicesDialog) },
        )
    }

    SavedCitiesContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onOpenCity = onOpenCity,
        onAddCity = onAddCity,
        onOpenSettings = onOpenSettings,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedCitiesContent(
    uiState: SavedCitiesUiState,
    onAction: (SavedCitiesAction) -> Unit,
    onOpenCity: (String) -> Unit,
    onAddCity: () -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_cities_title)) },
                actions = {
                    IconButton(onClick = { onAction(SavedCitiesAction.UseCurrentLocation) }) {
                        Icon(Icons.Filled.MyLocation, stringResource(R.string.content_description_current_location))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, stringResource(R.string.content_description_settings))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCity) {
                Icon(Icons.Filled.Add, stringResource(R.string.content_description_add_city))
            }
        },
    ) { padding ->
        if (uiState.cities.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.saved_cities_empty_message),
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.cities, key = { it.resolvedCity.cityId }) { city ->
                    SavedCityCard(
                        city = city,
                        unit = uiState.unit,
                        onOpen = { onOpenCity(city.resolvedCity.cityId) },
                        onRemove = { onAction(SavedCitiesAction.RemoveCity(city.resolvedCity.cityId)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedCityCard(
    city: SavedCity,
    unit: WeatherUnit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (city.isStale) StaleDataBanner(modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = city.resolvedCity.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = listOfNotNull(city.resolvedCity.state, city.resolvedCity.country)
                            .joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = weatherIconFor(city.current.conditionMain),
                        contentDescription = null,
                        tint = weatherTintFor(
                            conditionMain = city.current.conditionMain,
                            temp = city.current.temp,
                            localHour = localHourFrom(city.current.observedAt, city.current.tzOffsetSeconds),
                        ),
                    )
                    Text(
                        text = formatTemperature(city.current.temp, unit),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.content_description_remove_city),
                        )
                    }
                }
            }
        }
    }
}

private fun previewCity(name: String) = SavedCity(
    resolvedCity = ResolvedCity("12.97,77.59", name, null, "IN", 12.97, 77.59),
    sortOrder = 0,
    lastFetchedAt = 0L,
    current = CurrentConditions(28.0, 30.0, 22.0, 31.0, 60, "Clouds", "scattered clouds", "03d", 19800, 0L),
    isStale = false,
)

@PreviewLightDark
@Composable
private fun SavedCitiesContentPreview() {
    AIWeatherAppTheme {
        SavedCitiesContent(
            uiState = SavedCitiesUiState(
                cities = listOf(previewCity("Bengaluru"), previewCity("Mumbai")),
                isLoading = false,
            ),
            onAction = {},
            onOpenCity = {},
            onAddCity = {},
            onOpenSettings = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
