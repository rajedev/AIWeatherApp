package com.rajedev.aiweatherapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.rajedev.aiweatherapp.presentation.ui.currentconditions.CityDetailRoute
import com.rajedev.aiweatherapp.presentation.ui.daily.DailyForecastRoute
import com.rajedev.aiweatherapp.presentation.ui.savedcities.SavedCitiesRoute
import com.rajedev.aiweatherapp.presentation.ui.search.CitySearchRoute
import com.rajedev.aiweatherapp.presentation.ui.settings.SettingsRoute

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Route.SavedCities)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.SavedCities> {
                SavedCitiesRoute(
                    onOpenCity = { cityId -> backStack.add(Route.CityDetail(cityId)) },
                    onAddCity = { backStack.add(Route.CitySearch) },
                    onOpenSettings = { backStack.add(Route.Settings) },
                )
            }
            entry<Route.CityDetail> { route ->
                CityDetailRoute(
                    cityId = route.cityId,
                    onOpenDailyForecast = { backStack.add(Route.DailyForecast(route.cityId)) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Route.DailyForecast> { route ->
                DailyForecastRoute(cityId = route.cityId, onBack = { backStack.removeLastOrNull() })
            }
            entry<Route.CitySearch> {
                CitySearchRoute(
                    onCitySaved = { backStack.removeLastOrNull() },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Route.Settings> {
                SettingsRoute(onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
