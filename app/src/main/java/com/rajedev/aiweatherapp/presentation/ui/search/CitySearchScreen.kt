package com.rajedev.aiweatherapp.presentation.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.ui.theme.AIWeatherAppTheme

@Composable
fun CitySearchRoute(onCitySaved: () -> Unit, onBack: () -> Unit) {
    CitySearchScreen(onCitySaved = onCitySaved, onBack = onBack, viewModel = hiltViewModel())
}

@Composable
internal fun CitySearchScreen(onCitySaved: () -> Unit, onBack: () -> Unit, viewModel: CitySearchViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CitySearchUiEvent.CitySaved -> onCitySaved()
            }
        }
    }

    CitySearchContent(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchContent(
    uiState: CitySearchUiState,
    onAction: (CitySearchAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.city_search_title)) },
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
                .padding(padding),
        ) {
            TextField(
                value = uiState.query,
                onValueChange = { onAction(CitySearchAction.QueryChanged(it)) },
                placeholder = { Text(stringResource(R.string.city_search_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
            )
            when {
                uiState.isSearching -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.results.isEmpty() -> Text(
                    text = stringResource(R.string.city_search_empty_message),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(uiState.results, key = { it.cityId }) { city ->
                        CityResultRow(city = city, onClick = { onAction(CitySearchAction.CitySelected(city)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CityResultRow(city: ResolvedCity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(city.name) },
        supportingContent = {
            Text(listOfNotNull(city.state, city.country).joinToString(", "))
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}

@PreviewLightDark
@Composable
private fun CitySearchContentPreview() {
    AIWeatherAppTheme {
        CitySearchContent(
            uiState = CitySearchUiState(
                query = "Lon",
                results = listOf(
                    ResolvedCity("51.51,-0.13", "London", null, "GB", 51.51, -0.13),
                    ResolvedCity("42.98,-81.25", "London", "Ontario", "CA", 42.98, -81.25),
                ),
            ),
            onAction = {},
            onBack = {},
        )
    }
}
