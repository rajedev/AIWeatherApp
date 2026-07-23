@file:OptIn(FlowPreview::class)

package com.rajedev.aiweatherapp.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajedev.aiweatherapp.domain.model.ResolvedCity
import com.rajedev.aiweatherapp.domain.usecase.CityResolutionInput
import com.rajedev.aiweatherapp.domain.usecase.ResolveCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.SaveCityUseCase
import com.rajedev.aiweatherapp.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val resolveCityUseCase: ResolveCityUseCase,
    private val saveCityUseCase: SaveCityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitySearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<CitySearchUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collectLatest { query -> performSearch(query) }
        }
    }

    fun onAction(action: CitySearchAction) {
        when (action) {
            is CitySearchAction.QueryChanged -> onQueryChanged(action.query)
            is CitySearchAction.CitySelected -> selectCity(action.city)
            CitySearchAction.ConsumeError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query, results = if (query.isBlank()) emptyList() else it.results) }
        queryFlow.value = query
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true) }
        searchCitiesUseCase(query)
            .onSuccess { results -> _uiState.update { it.copy(isSearching = false, results = results) } }
            .onFailure { e -> _uiState.update { it.copy(isSearching = false, errorMessage = e.message) } }
    }

    private fun selectCity(city: ResolvedCity) {
        viewModelScope.launch {
            resolveCityUseCase(CityResolutionInput.FromSearchSelection(city))
                .mapCatching { resolved -> saveCityUseCase(resolved).getOrThrow() }
                .onSuccess { _uiEvent.send(CitySearchUiEvent.CitySaved) }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }
}
