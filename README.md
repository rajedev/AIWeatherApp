# AIWeatherApp

A weather app for Android built with Kotlin and Jetpack Compose, featuring offline-first caching, background sync, and a UI that adapts its theme to the current weather.

Built with [Claude Code](https://claude.com/claude-code) — used throughout for architecture, feature implementation, and Android best practices.

## Features

- Current weather with 6-hour hourly forecast
- Daily forecast view
- City search and saved cities management
- Settings for units, alerts, and refresh interval
- Offline-first caching (works without a network connection)
- Periodic background sync via WorkManager
- Threshold-based weather alerts and notifications
- Weather-adaptive theming — colors and mood shift based on current conditions
- Current-location support with graceful fallback when unavailable

## Tech Stack

- Kotlin
- Jetpack Compose + Material3
- Hilt (dependency injection)
- Retrofit + kotlinx.serialization
- Coroutines + Flow
- Navigation 3 (nav3)
- Room (offline cache)
- DataStore (preferences)
- WorkManager (background sync)
- Play Services Location
- Vico (charts)

## Architecture

Clean Architecture with a single `app` module, following a strict inward dependency rule:

```
Presentation → Domain ← Data
```

- **Domain** — plain Kotlin, no Android dependencies; use cases and repository interfaces.
- **Data** — Retrofit services, DTOs, Room entities, and repository implementations that satisfy the domain contracts.
- **Presentation** — Compose screens and ViewModels that call use cases only.

## Getting Started

1. Clone the repository.
2. Get a free API key from [OpenWeatherMap](https://openweathermap.org/api).
3. Add it to `local.properties` in the project root:
   ```properties
   OPEN_WEATHER_API_KEY=your_key_here
   ```
   This file is gitignored, so each developer needs to add their own key. The key is picked up via `BuildConfig.OPEN_WEATHER_API_KEY` and attached to requests automatically.
4. Open the project in Android Studio, or build from the command line:
   ```bash
   ./gradlew installDebug
   ```

**Requirements:** minSdk 24, targetSdk/compileSdk 36.

## References

- [OpenWeatherMap API docs](https://openweathermap.org/api)
- [Claude Code](https://claude.com/claude-code)
