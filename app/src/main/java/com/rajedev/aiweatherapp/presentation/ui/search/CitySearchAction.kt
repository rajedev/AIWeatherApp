package com.rajedev.aiweatherapp.presentation.ui.search

import com.rajedev.aiweatherapp.domain.model.ResolvedCity

sealed interface CitySearchAction {
    data class QueryChanged(val query: String) : CitySearchAction
    data class CitySelected(val city: ResolvedCity) : CitySearchAction
    data object ConsumeError : CitySearchAction
}
