# AIWeatherApp — Technical & Functional Guide

This document explains **what each feature does** and **how it's implemented in code** — the goal is to let anyone (including future-you) open this file and understand the functional flow end-to-end without having to reverse-engineer it from the source tree.

For coding conventions/rules (not covered here), see the other files in `docs/`: `architecture.md`, `networking.md`, `compose.md`, `navigation.md`, `hilt.md`, `standards.md`.

## Table of contents

1. [Stack & architecture at a glance](#1-stack--architecture-at-a-glance)
2. [Navigation map](#2-navigation-map)
3. [Feature: Saved Cities list](#3-feature-saved-cities-list)
4. [Feature: City Search / Add City](#4-feature-city-search--add-city)
5. [Feature: City Detail (current conditions)](#5-feature-city-detail-current-conditions)
6. [Feature: Daily Forecast](#6-feature-daily-forecast)
7. [Feature: Settings](#7-feature-settings)
8. [Cross-cutting: Location handling](#8-cross-cutting-location-handling)
9. [Cross-cutting: Theming (Material3 + weather-mood colors)](#9-cross-cutting-theming-material3--weather-mood-colors)
10. [Cross-cutting: Networking](#10-cross-cutting-networking)
11. [Cross-cutting: Persistence & offline cache](#11-cross-cutting-persistence--offline-cache)
12. [Cross-cutting: Background sync & weather alerts](#12-cross-cutting-background-sync--weather-alerts)
13. [Dependency injection map](#13-dependency-injection-map)
14. [Common presentation utilities](#14-common-presentation-utilities)

---

## 1. Stack & architecture at a glance

**Stack**: Kotlin, Jetpack Compose + Material3, Hilt, Retrofit + kotlinx.serialization, Room, DataStore, WorkManager, Coroutines + Flow, Navigation 3 (nav3), Vico (charts).

**Layering** (dependencies always point inward — see `docs/architecture.md`):

```
Presentation → Domain ← Data
```

```
di/                              — Hilt modules (wiring only)
data/remote/api|dto|mapper/      — Retrofit service, DTOs, DTO→domain mappers
data/local/entity|dao/           — Room cache
data/preferences/                — DataStore (user prefs)
data/repository/                 — domain repository implementations
data/location/                   — FusedLocationProviderClient wrapper
domain/model/                    — plain Kotlin data classes (zero Android imports)
domain/repository/               — repository interfaces (contracts only)
domain/usecase/                  — one class per operation, `suspend operator fun invoke()`
domain/alert/                    — weather alert threshold evaluation
presentation/navigation/         — Route definitions + NavHost
presentation/ui/<feature>/       — Screen + ViewModel + UiState + Action + UiEvent, per feature
presentation/common/             — shared composables/formatters used across features
presentation/theme/              — weather-mood color system + app theme mode plumbing
notification/, sync/             — WorkManager + notifications for background alerts
```

**MVVM pattern used by every feature** (see `docs/architecture.md`):
- ViewModel exposes `val uiState: StateFlow<FeatureUiState>` (persistent, survives recomposition) and `val uiEvent: Flow<FeatureUiEvent>` (one-shot, via `Channel`, for navigation/permission-dialogs/snackbars that must not replay on rotation).
- Screens are stateless composables: `FeatureContent(uiState, onAction, ...)`. All business logic lives in the ViewModel; composables only render state and forward user intent as `FeatureAction` values via `onAction`.
- `FeatureRoute(...)` is the Nav3 entry point that resolves the ViewModel via `hiltViewModel()` and delegates to the internal `FeatureScreen`.

---

## 2. Navigation map

**File**: `presentation/navigation/Route.kt` — every destination is a `@Serializable` member of `sealed interface Route` (no string route literals anywhere):

- `Route.SavedCities` — home/start destination
- `Route.CitySearch` — add-a-city flow
- `Route.CityDetail(cityId: String)`
- `Route.DailyForecast(cityId: String)`
- `Route.Settings`

**File**: `presentation/navigation/AppNavHost.kt` — owns the back stack:
- `rememberNavBackStack(Route.SavedCities)` + `NavDisplay` (no `NavController` — this is Nav3, not the old Navigation-Compose library).
- Entry decorators: `rememberSceneSetupNavEntryDecorator`, `rememberSavedStateNavEntryDecorator`, `rememberViewModelStoreNavEntryDecorator`.
- Each `Route` maps to a `entryProvider` case that instantiates the corresponding `*Route(...)` composable, wiring navigation as **lambdas** (`onOpenCity = { backStack.add(Route.CityDetail(it)) }`, `onBack = { backStack.removeLastOrNull() }`) — screens never touch `backStack` directly.

**Graph**:
```
SavedCities ──(tap city)───────────► CityDetail(cityId) ──(tap hero card)──► DailyForecast(cityId)
     │                                     │
     ├──(FAB "add city")──► CitySearch ────┘ (on save, pops back to SavedCities)
     │
     └──(settings icon)───► Settings
```

---

## 3. Feature: Saved Cities list

**Files**: `presentation/ui/savedcities/{SavedCitiesScreen, SavedCitiesViewModel, SavedCitiesUiState, SavedCitiesAction, SavedCitiesUiEvent, LocationServicesDisabledDialog}.kt`

This is the home screen — a list of saved cities with current temperature, an icon tinted to that city's own weather condition, and a "use current location" shortcut.

**State** (`SavedCitiesUiState`): `cities: List<SavedCity>`, `unit: WeatherUnit`, `isLoading`, `errorMessage: String?`, `showLocationServicesDisabledDialog: Boolean`.

**Actions**: `RemoveCity(cityId)`, `UseCurrentLocation`, `LocationPermissionResult(granted)`, `ConsumeError`, `DismissLocationServicesDialog`, `OpenLocationSettingsRequested`.

**Events** (one-shot): `RequestLocationPermission`, `CityAdded(cityId)`, `OpenLocationSettings`.

### Functional flow — list rendering
1. `SavedCitiesViewModel.init` launches a coroutine that `combine`s `ObserveSavedCitiesUseCase()` (Flow from Room) with `GetUserPreferencesUseCase()` (Flow from DataStore, for the display unit).
2. Every emission updates `_uiState` with the latest `cities` + `unit`. Because both are Flows, adding/removing a city or changing units anywhere in the app updates this screen automatically — no manual refresh needed.
3. Each `SavedCityCard` shows a `StaleDataBanner` if `city.isStale` (computed at read time in `WeatherRepositoryImpl`, see [§11](#11-cross-cutting-persistence--offline-cache)), and tints its weather icon via `weatherTintFor(city.current.conditionMain, city.current.temp, localHourFrom(...))` — this city's own condition, not some global app mood (see [§9](#9-cross-cutting-theming-material3--weather-mood-colors)).

### Functional flow — "use current location"
1. User taps the location icon in the `TopAppBar` → dispatches `UseCurrentLocation`.
2. ViewModel sends the `RequestLocationPermission` event → screen sets `pendingLocationPermissionRequest = true`, which drives the `LocationPermissionHandler` composable (see [§8](#8-cross-cutting-location-handling)) to check/request `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION`.
3. The handler's result comes back as `LocationPermissionResult(granted)`.
4. If denied → no-op (early return).
5. If granted → ViewModel calls `ResolveCurrentLocationCityUseCase()`, which internally calls `LocationProvider.getCurrentLocation()` then reverse-geocodes the coordinates into a `ResolvedCity`.
6. On success: `SaveCityUseCase(city)` persists it, then `CityAdded(cityId)` event fires and the screen navigates straight to that city's detail screen.
7. On failure, `handleLocationFailure(error)` discriminates the failure type:
   - `error is LocationServicesUnavailable` (device GPS/location toggle is off) → `showLocationServicesDisabledDialog = true` → `LocationServicesDisabledDialog` renders with an "Open settings" button that fires `OpenLocationSettingsRequested` → ViewModel sends `OpenLocationSettings` → screen launches `Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)`.
   - anything else (no GPS fix, network error, etc.) → `errorMessage` is set → a `Snackbar` shows it, then dispatches `ConsumeError` to clear it.

---

## 4. Feature: City Search / Add City

**Files**: `presentation/ui/search/{CitySearchScreen, CitySearchViewModel, CitySearchUiState, CitySearchAction, CitySearchUiEvent}.kt`

**State**: `query: String`, `results: List<ResolvedCity>`, `isSearching: Boolean`, `errorMessage: String?`.
**Actions**: `QueryChanged(query)`, `CitySelected(city)`, `ConsumeError`.
**Events**: `CitySaved` (one-shot — triggers navigation back to Saved Cities).

### Functional flow
1. User types → `QueryChanged` updates `uiState.query` and pushes the value into an internal `MutableStateFlow` that's **debounced 300ms** and filtered for non-blank/distinct values before triggering a search — avoids firing an API call on every keystroke.
2. After the debounce settles: `isSearching = true` → `SearchCitiesUseCase(query)` → `CityRepositoryImpl.searchCities()` hits `GET geo/1.0/direct` (OpenWeatherMap geocoding).
3. Results are mapped `GeocodeResultDto → ResolvedCity?` (nullable — a malformed entry is dropped, not fatal) and **deduplicated by `cityId`** (two near-identical geocode matches can round to the same rounded-coordinate `cityId`; `cityId` is also the Compose `LazyColumn` key for this list, so a duplicate would crash the UI — see the fix note in [§10](#10-cross-cutting-networking)).
4. Tapping a result dispatches `CitySelected(city)` → `ResolveCityUseCase(FromSearchSelection(city))` enriches it, then `SaveCityUseCase(resolvedCity)` persists it → `CitySaved` event fires → screen calls `onCitySaved()` → `AppNavHost` pops back to Saved Cities, where the new city now appears via the reactive `ObserveSavedCitiesUseCase` flow.

---

## 5. Feature: City Detail (current conditions)

**Files**: `presentation/ui/currentconditions/{CityDetailScreen, CityDetailViewModel, CityDetailUiState, CityDetailAction}.kt`

**State**: `cityId`, `displayName`, `current: CurrentConditions?`, `hourly: List<HourlyForecast>`, `unit`, `isStale`, `isLoading`, `errorMessage`.
**Actions**: `LoadCity(cityId)`, `Refresh`, `ConsumeError`.

### Functional flow
1. `LaunchedEffect(cityId)` dispatches `LoadCity(cityId)` on first composition (and again if `cityId` changes, e.g. navigating between cities without leaving the stack).
2. ViewModel cancels any previous observe job, resets state, then launches a new job combining `ObserveCityWeatherUseCase(cityId)` + `ObserveHourlyForecastUseCase(cityId)` + `GetUserPreferencesUseCase()`.
3. If the emitted data is stale (`isStale == true`), the ViewModel **automatically calls `refresh()`** — the user never has to manually pull-to-refresh just because the cache aged out.
4. **`Refresh` action** (tap the refresh icon) calls `RefreshWeatherUseCase(cityId)`, which fetches fresh current + forecast data from the API and writes it back to Room; the same reactive Flows above then emit the updated values automatically.
5. **Hero card**: derives its own color mood from *this screen's own* `current` conditions — `resolveMood(current.conditionMain, current.temp, localHourFrom(current.observedAt, current.tzOffsetSeconds)).toThemeColors()` — see [§9](#9-cross-cutting-theming-material3--weather-mood-colors) for why this matters (it used to read a global, possibly-mismatched app-wide mood; that bug was fixed and the old global-mood plumbing was later deleted as dead code).
6. **Hourly forecast row**: a horizontal `LazyRow`; each hour's icon is tinted via `weatherTintFor(hour.conditionMain, hour.temp, localHourFrom(hour.timestamp, tzOffsetSeconds))` — every hour gets its *own* color, since a rainy 3pm slot shouldn't look sunny just because right now (or some other hour) is sunny.
7. Tapping the hero card navigates to `Route.DailyForecast(cityId)`.

---

## 6. Feature: Daily Forecast

**Files**: `presentation/ui/daily/{DailyForecastScreen, DailyForecastViewModel, DailyForecastUiState, DailyForecastAction}.kt`

**State**: `cityId`, `dailyForecasts: List<DailyForecast>` (5 entries), `unit`, `isLoading`, `errorMessage`.

### Functional flow
1. Same `LaunchedEffect(cityId)` → `LoadCity` → combine-flows pattern as City Detail, observing `ObserveDailyForecastUseCase(cityId)` + `GetUserPreferencesUseCase()`.
2. Each `DailyForecastRow` shows date, min/max temp, and an icon tinted via `weatherTintFor(daily.dominantCondition, daily.tempMax, DAILY_ROW_REFERENCE_HOUR = 12)`. A fixed "noon" reference hour is used (not the real hour) because a whole-day bucket has no single hour of its own, and using an arbitrary hour could otherwise resolve to the NIGHT mood, which doesn't make sense for a full-day summary; `tempMax` (not `tempMin`) is used so a day that peaks hot reads as HOT even if its low was mild.
3. Below the list, `TrendChart` renders a Vico `CartesianChartHost` with two line series (daily min temps, daily max temps).
4. **Chart theming fix**: the chart is wrapped in `ProvideVicoTheme(rememberM3VicoTheme())`. Vico's own default theme resolution calls `isSystemInDarkTheme()` directly — completely decoupled from this app's `ThemeMode` override (System/Light/Dark, see [§9](#9-cross-cutting-theming-material3--weather-mood-colors)). Without this, forcing the app into Dark mode while the device's system setting is Light (or vice versa) made the chart's axis text/lines pick the *wrong* palette and become invisible against the actual (different) background. `rememberM3VicoTheme()` derives the chart's colors from `MaterialTheme.colorScheme` instead, so it always matches whatever theme is actually active.

---

## 7. Feature: Settings

**Files**: `presentation/ui/settings/{SettingsScreen, SettingsViewModel, SettingsUiState, SettingsAction}.kt`

**State**: `unit: WeatherUnit`, `highTempC: Double`, `lowTempC: Double`, `alertsEnabled: Boolean`, `themeMode: ThemeMode`.
**Actions**: `SetUnit`, `SetHighTempThreshold`, `SetLowTempThreshold`, `ToggleAlerts`, `SetThemeMode`.

Each preference follows the exact same one-preference-one-use-case shape: dispatch action → ViewModel calls a dedicated `Set*UseCase` → `PreferencesRepository` writes to DataStore → the same `GetUserPreferencesUseCase()` Flow the ViewModel is already observing in `init` emits the new value → `uiState` updates → UI reflects it. This means **every screen reading preferences updates live**, without any explicit "reload" step.

- **Units** (`SetUnit`): `WeatherUnit.METRIC`/`IMPERIAL`, rendered as two `FilterChip`s. All temperature values are stored in Celsius everywhere upstream; conversion to the display unit happens only in the presentation layer (`celsiusToDisplayUnit`/`displayUnitToCelsius`/`formatTemperature` in `presentation/common/UnitFormat.kt`).
- **Alert thresholds** (`SetHighTempThreshold` / `SetLowTempThreshold`): two `Slider`s (25–45°C high, -10–15°C low in storage terms, converted to/from the display unit for the visible range/label). Both write through `SetAlertThresholdsUseCase(AlertThresholds(high, low, enabled))`.
- **Enable Alerts** (`ToggleAlerts`): turning this **on** first triggers `NotificationPermissionHandler` to request `POST_NOTIFICATIONS` (Android 13+); only once that resolves does the screen actually dispatch `ToggleAlerts(enabled = true)`. Turning it off requires no permission dance.
- **Theme mode** (`SetThemeMode`): `ThemeMode.SYSTEM`/`LIGHT`/`DARK` as a `FilterChip` row — see [§9](#9-cross-cutting-theming-material3--weather-mood-colors) for how this actually repaints the whole app.

---

## 8. Cross-cutting: Location handling

**Files**: `presentation/common/LocationPermissionHandler.kt`, `data/location/{FusedLocationProviderImpl, LocationServicesDisabledException, LocationUnavailableException}.kt`, `domain/repository/{LocationProvider, LocationServicesUnavailable}.kt`, `domain/usecase/ResolveCurrentLocationCityUseCaseImpl.kt`.

There are **three distinct failure modes** the app has to tell apart, each with a different user-facing behavior:

| Scenario | Where detected | Type | UI behavior |
|---|---|---|---|
| User denies the runtime permission | `LocationPermissionHandler` | (no exception; just `granted=false`) | Silent no-op |
| Device location services (GPS/network) are switched off | `FusedLocationProviderImpl.isLocationServicesEnabled()` | `LocationServicesDisabledException` (implements marker `LocationServicesUnavailable`) | `LocationServicesDisabledDialog` with an "Open settings" deep link |
| Permission + services both fine, but no GPS fix / timeout / network error | `FusedLocationProviderImpl` fused-client callback | `LocationUnavailableException` (or the raw exception) | Generic `Snackbar` with the error message |

**Flow**, step by step:
1. `LocationPermissionHandler(shouldRequest, onGranted, onDenied)` is a composable that, when `shouldRequest` flips true, checks `ContextCompat.checkSelfPermission(ACCESS_FINE_LOCATION)`; if not already granted, it launches `ActivityResultContracts.RequestMultiplePermissions()` for both `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` (either one being granted counts as success).
2. Once permission is confirmed, `FusedLocationProviderImpl.getCurrentLocation()` runs:
   ```kotlin
   override suspend fun getCurrentLocation(): Result<LatLon> {
       if (!isLocationServicesEnabled()) return Result.failure(LocationServicesDisabledException())
       return fetchCurrentLocation()   // one-shot FusedLocationProviderClient.getCurrentLocation()
   }
   ```
   The `isLocationServicesEnabled()` check (`LocationManagerCompat.isLocationEnabled(locationManager)`) runs **before** ever touching the fused client — this is what lets the app distinguish "GPS toggle is off" from "toggle is on but no fix yet."
3. `ResolveCurrentLocationCityUseCaseImpl` chains the location fetch into `ResolveCityUseCase` (reverse geocoding) — any failure short-circuits and propagates as-is, preserving its concrete type.
4. `SavedCitiesViewModel.handleLocationFailure(error)` is the single place that discriminates via `error is LocationServicesUnavailable` — a **domain-owned marker interface**, so the presentation layer doesn't need to import a data-layer exception type to make this decision (keeps the dependency rule intact: Presentation → Domain ← Data).

---

## 9. Cross-cutting: Theming (Material3 + weather-mood colors)

Two independent theming systems exist and are easy to conflate — they solve different problems:

### 9a. App-wide light/dark theme (user-controlled)

**Files**: `ui/theme/{Theme, Color, Type}.kt`, `domain/model/ThemeMode.kt`, `presentation/theme/AppThemeViewModel.kt`, `MainActivity.kt`.

```
Settings screen → SetThemeModeUseCase → PreferencesRepository (DataStore "theme_mode" key)
   → AppThemeViewModel.themeMode: StateFlow<ThemeMode>
   → MainActivity computes darkTheme: Boolean (SYSTEM→isSystemInDarkTheme(), LIGHT→false, DARK→true)
   → AIWeatherAppTheme(darkTheme = darkTheme) { AppNavHost(...) }
```

`AIWeatherAppTheme` picks Material3's dynamic color scheme on Android 12+ (`dynamicDarkColorScheme`/`dynamicLightColorScheme`), falling back to static `DarkColorScheme`/`LightColorScheme` on older API levels or when dynamic color is disabled.

`AppThemeViewModel` is intentionally tiny — it exists *only* to expose `themeMode` to `MainActivity` (it used to also expose a global weather "mood" derived from the first saved city, but that was dead code after 9b below made it obsolete, and was deleted during a cleanup pass — don't resurrect that pattern without a reason).

### 9b. Per-item weather-mood coloring (condition-driven, not user-controlled)

**Files**: `presentation/theme/{WeatherMood, WeatherThemeColors}.kt`, `presentation/common/WeatherIcon.kt`.

This is what makes a "Rain" icon look blue and a "Clear/hot" icon look orange, **per item**, independent of whichever Material3 light/dark scheme is active.

**`resolveMood(conditionMain, currentTemp, localHour): WeatherMood`** — one deterministic function, evaluated in this order (first match wins):

| # | Condition | Mood |
|---|---|---|
| 1 | `localHour` outside `6..18` **and** condition is `"Clear"` or `"Clouds"` | `NIGHT` |
| 2 | `currentTemp >= 35.0` | `HOT` |
| 3 | `currentTemp <= 5.0` **or** condition is `"Snow"` | `WINTER` |
| 4 | condition is `"Rain"`, `"Drizzle"`, or `"Thunderstorm"` | `RAINY` |
| 5 | condition is `"Clouds"` | `CLOUDY` |
| 6 | (default) | `SUNNY` |

**`WeatherMood.toThemeColors(): WeatherThemeColors`** — the single color table every screen draws from:

| Mood | primary | surfaceTint | icon |
|---|---|---|---|
| HOT | `#D85A30` | `#FAEAE7` | `LocalFireDepartment` |
| SUNNY | `#BA7517` | `#FAEEEA` | `WbSunny` |
| RAINY | `#185FA5` | `#E6F1FB` | `Umbrella` |
| CLOUDY | `#5F5E5A` | `#F1EFE8` | `Cloud` |
| WINTER | `#0F6E56` | `#E1F5EE` | `AcUnit` |
| NIGHT | `#3C3489` | `#EEEEFE` | `NightsStay` |

`onPrimary` is white for every mood. `onSurfaceTint` is a **fixed dark ink color (`#1C1B1F`) for every mood**, deliberately *not* following `MaterialTheme.colorScheme` — because `surfaceTint` is always a light pastel regardless of whether the app is in light or dark mode, so its paired text color must stay dark too, or it becomes unreadable the moment the surrounding UI (and the ambient `LocalContentColor` it would otherwise inherit) switches to dark mode. (This is exactly the bug that was fixed on the City Detail hero card — text was inheriting the dark-theme's light-colored default instead of an explicit dark one.)

**`presentation/common/WeatherIcon.kt`** ties it together for callers:
- `weatherIconFor(conditionMain): ImageVector` — the glyph only (`"Clear"→WbSunny`, `"Clouds"→Cloud`, `"Rain"/"Drizzle"→Umbrella`, `"Thunderstorm"→Bolt`, `"Snow"→AcUnit`, else→`WaterDrop`). Note this icon set is intentionally simpler/different from the mood table above — it's driven purely by the raw API condition string, not by the resolved mood.
- `weatherTintFor(conditionMain, temp, localHour): Color` = `resolveMood(...).toThemeColors().primary` — the per-item color.
- `localHourFrom(timestampMillis, tzOffsetSeconds): Int` — converts a UTC timestamp into the *place's own* local hour (not the device's), so a city in a different timezone gets correctly NIGHT-tinted at its own night, not the viewer's.

**Call sites**: hourly forecast row (`CityDetailScreen`), saved-city card (`SavedCitiesScreen`), daily forecast row (`DailyForecastScreen`), and the hero card (`CityDetailScreen`, using the same `resolveMood`/`toThemeColors` pipeline directly rather than through `weatherTintFor`, since it also needs `icon`/`surfaceTint`/`onSurfaceTint`, not just `primary`).

---

## 10. Cross-cutting: Networking

**Files**: `data/remote/api/OpenWeatherApi.kt`, `data/remote/dto/*.kt`, `data/remote/mapper/*.kt`, `di/NetworkModule.kt`.

**Endpoints** (OpenWeatherMap free tier):

| Function | Endpoint | Purpose |
|---|---|---|
| `getCurrentWeather(lat, lon, units)` | `GET data/2.5/weather` | Current conditions for a point |
| `getForecast(lat, lon, units)` | `GET data/2.5/forecast` | 5-day / 3-hour-interval forecast (40 entries) |
| `searchPlaces(query, limit=5)` | `GET geo/1.0/direct` | Name → coordinates (city search) |
| `reverseGeocode(lat, lon, limit=1)` | `GET geo/1.0/reverse` | Coordinates → name ("use current location") |

**Client setup** (`NetworkModule`): `Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }` + an `ApiKeyInterceptor` (injects `appid` from `BuildConfig.OPEN_WEATHER_API_KEY`, itself sourced from `local.properties` at build time — never hardcoded) + `HttpLoggingInterceptor` (BODY in debug, NONE in release).

**Mapper resilience pattern** — the important, non-obvious bit: `GeocodeResultDto.toDomain(): ResolvedCity?` is **nullable**. A single malformed entry in a multi-result response (e.g. searching "Sydney" returns several near-duplicate matches, one of which may be missing `lat`/`lon`/`name`/`country`) must not crash the whole search — the mapper returns `null` for that one entry instead of throwing, and `CityRepositoryImpl.searchCities()` uses `.mapNotNull { it.toDomain() }` to drop only the bad entries. It then also `.distinctBy { it.cityId }` — because coordinates are rounded to a ~1.1km grid for `cityId` generation, two genuinely-different-but-nearby geocode matches can collapse to the *same* `cityId`, which is also used as the Compose `LazyColumn` key for the results list; an un-deduped list would crash with a "duplicate key" error at render time, a completely different failure mode from the malformed-field case. `ForecastMapper.kt` already followed this "drop the bad entry, don't fail the batch" convention for hourly/daily forecast slices; `GeocodeMapper` was the one outlier fixed to match it.

---

## 11. Cross-cutting: Persistence & offline cache

**Files**: `data/local/{AppDatabase, entity/*, dao/*}.kt`, `data/repository/WeatherRepositoryImpl.kt`, `data/preferences/UserPreferencesDataStore.kt`.

**Room** (`AppDatabase`, entities `WeatherCacheEntity` + `AlertStateEntity`):
- `WeatherCacheEntity` is the single row per saved city: city metadata (id/name/state/country/lat/lon/tz offset), a `sortOrder`, `lastFetchedAt`, the current-conditions fields flattened onto the row, and `hourlyJson`/`dailyJson` — the hourly and daily forecasts serialized as JSON strings rather than separate tables (simpler for a single-owner-per-city cache with no relational queries needed across cities).
- **Staleness** is *not* a stored flag — `WeatherRepositoryImpl` recomputes it on every read: `stale = (now - lastFetchedAt) > 45 minutes`. This means staleness always reflects wall-clock time relative to now, never a value that could itself go stale.
- **Eviction**: `CityRepositoryImpl.saveCity()` calls `dao.evictOldestBeyondLimit(MAX_SAVED_CITIES = 15)` after every upsert — oldest-by-`lastFetchedAt` cities beyond the cap are dropped automatically; there's no explicit "storage full" error path for the user to hit.
- `AlertStateEntity` is unrelated to the weather cache — it exists purely to debounce repeat notifications (see [§12](#12-cross-cutting-background-sync--weather-alerts)).

**DataStore** (`UserPreferencesDataStore`, file `user_prefs`): five keys — `unit`, `high_temp_c`, `low_temp_c`, `alerts_enabled`, `theme_mode` — each a simple scalar (`stringPreferencesKey`/`doublePreferencesKey`/`booleanPreferencesKey`), all with sensible defaults so a fresh install never needs an explicit "first run setup" step. Alert threshold defaults (30°C high / 5°C low) intentionally sit close to the weather-mood HOT/WINTER thresholds (35°C/5°C) so "the UI looks hot" and "you get alerted" roughly agree out of the box.

---

## 12. Cross-cutting: Background sync & weather alerts

**Files**: `sync/{WeatherSyncWorker, WeatherSyncScheduler}.kt`, `domain/alert/WeatherAlertEvaluator.kt`, `notification/{WeatherAlertNotifier, NotificationPermissionHandler}.kt`, `domain/usecase/RefreshAllSavedCitiesUseCase*.kt`.

**Scheduling**: `WeatherSyncScheduler.schedule()` enqueues a periodic `WeatherSyncWorker` (`"weather_sync"`, every 60 minutes, requires `NetworkType.CONNECTED`, exponential backoff) with `ExistingPeriodicWorkPolicy.KEEP` — calling `schedule()` again on every app launch is safe and does **not** reset the interval timer, it's a no-op if already scheduled.

**Each run** (`WeatherSyncWorker`, a Hilt-injected `CoroutineWorker`):
1. `RefreshAllSavedCitiesUseCase()` refreshes every saved city's weather, 5 cities at a time (chunked concurrency, not fully sequential or fully unbounded parallel).
2. If `alertsEnabled` is on, each *successfully*-refreshed city's fresh `CurrentConditions` is passed to `WeatherAlertEvaluator.evaluate(conditions, thresholds, cityId)`, which returns one of `WeatherAlert.ExtremeHeat` / `ExtremeCold` / `SevereCondition` (thunderstorm/tornado/squall), or `null`.
3. `dispatchWithDebounce(cityId, alert, cityName)`: if `alert == null`, any existing debounce record for that city is cleared (the condition resolved — next time it recurs, it's treated as fresh, not suppressed). If the *same* alert type fired again within `ALERT_COOLDOWN_MS` (6 hours) of the last one, it's suppressed — otherwise `WeatherAlertNotifier.showAlert(...)` fires and the debounce state is updated.
4. The worker returns `Result.retry()` only if **every** city failed to refresh; a partial failure (e.g. one city's API call times out) still counts as overall success so one flaky city doesn't spin the whole job into a retry loop.

**Notifications**: `WeatherAlertNotifier.ensureChannel()` creates the `"weather_alerts"` channel (IMPORTANCE_HIGH) once; `showAlert()` checks `POST_NOTIFICATIONS` at send time and silently skips the system notification if not granted (the debounce state is still recorded either way, so re-granting permission later doesn't cause a flood of backlogged alerts). The permission itself is only ever requested from an explicit user action — toggling "Enable Alerts" on in Settings — never on cold app launch (see [§7](#7-feature-settings)).

---

## 13. Dependency injection map

All modules live in `di/`, installed in `SingletonComponent`:

| Module | Provides / binds |
|---|---|
| `DispatcherModule` | `@Named("io"/"default"/"main")` `CoroutineDispatcher`s |
| `NetworkModule` | `Json`, `ApiKeyInterceptor`, `HttpLoggingInterceptor`, `OkHttpClient`, `Retrofit`, `OpenWeatherApi` |
| `DatabaseModule` | `AppDatabase`, `WeatherCacheDao`, `AlertStateDao` |
| `PreferencesModule` | `UserPreferencesDataStore` → `PreferencesRepository` |
| `LocationModule` | `FusedLocationProviderImpl` → `LocationProvider` |
| `RepositoryModule` | `CityRepositoryImpl` → `CityRepository`, `WeatherRepositoryImpl` → `WeatherRepository` |
| `UseCaseModule` | every `*UseCaseImpl` → its `*UseCase` interface (one `@Binds` per use case — see `docs/hilt.md` for the convention) |

---

## 14. Common presentation utilities

**File**: `presentation/common/`

| File | Purpose |
|---|---|
| `UnitFormat.kt` | `celsiusToDisplayUnit`/`displayUnitToCelsius` (Celsius ⇄ user's chosen unit), `formatTemperature` (→ `"28°C"`/`"82°F"`) |
| `HourFormat.kt` | `formatHour(timestampMillis, tzOffsetSeconds)` → 12-hour local label (e.g. `"3 PM"`) in the **place's** timezone, not the device's |
| `WeatherIcon.kt` | `weatherIconFor`, `weatherTintFor`, `localHourFrom` — see [§9b](#9b-per-item-weather-mood-coloring-condition-driven-not-user-controlled) |
| `StaleDataBanner.kt` | Small banner shown when cached data has aged past the 45-minute threshold |
| `LocationPermissionHandler.kt` | See [§8](#8-cross-cutting-location-handling) |

Settings has its own `NotificationPermissionHandler` (in the `notification/` package, not `presentation/common/`) following the identical request-on-explicit-action pattern as `LocationPermissionHandler`.
