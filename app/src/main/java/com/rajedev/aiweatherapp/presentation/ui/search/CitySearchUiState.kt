package com.rajedev.aiweatherapp.presentation.ui.search

import androidx.compose.runtime.Immutable
import com.rajedev.aiweatherapp.domain.model.ResolvedCity

@Immutable
data class CitySearchUiState(
    val query: String = "",
    val results: List<ResolvedCity> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
)
