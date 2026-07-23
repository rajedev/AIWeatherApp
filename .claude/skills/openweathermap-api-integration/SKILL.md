---
name: openweathermap-api-integration
description: Use this skill whenever integrating OpenWeatherMap's free-tier API into an app — current weather, 5-day/3-hour forecast, geocoding (city name to coordinates and back), or securely configuring the API key (local.properties, BuildConfig, request interceptor). Trigger on mentions of "OpenWeatherMap", "openweathermap.org", "forecast5", "data/2.5/weather", "data/2.5/forecast", "geo/1.0/direct", "geo/1.0/reverse", "OWM", or "OpenWeatherMap API key". Also trigger for the general technique of aggregating interval-based forecast data (e.g. 3-hourly) into daily buckets, which generalizes beyond weather to any interval-sampled forecast/timeseries API. Covers the free endpoints, which name/ID-based lookups are deprecated in favor of geocoding, the client-side daily-aggregation logic the free tier requires, and safe API key storage. Not a tech-stack tutorial — pair with whatever networking/DI/caching pattern the project already uses (e.g. an offline-first caching skill).
---

# OpenWeatherMap API Integration

## Free-tier endpoints — the only ones this skill uses

No card, no subscription activation required for any of these:

| Endpoint | Gives you |
|---|---|
| `api.openweathermap.org/data/2.5/weather` | Current conditions |
| `api.openweathermap.org/data/2.5/forecast` | 5 days, 3-hour steps (40 points) |
| `api.openweathermap.org/geo/1.0/direct` | Name → lat/lon (forward geocoding) |
| `api.openweathermap.org/geo/1.0/reverse` | Lat/lon → name (reverse geocoding) |

These four endpoints fully cover current + short-range forecast + city
resolution.

## Deprecated: city name / city ID directly on weather endpoints

```
# Deprecated — still works, but no further bug fixes or updates:
GET /data/2.5/weather?q={city name}&appid={key}
GET /data/2.5/forecast?id={city id}&appid={key}
```

Don't build new integrations on `q=`/`id=` against the weather/forecast endpoints. Use the dedicated Geocoding API to resolve a name to coordinates first, then call weather/forecast with `lat`/`lon` only — that's the supported, maintained path.

## Geocoding — forward (search) and reverse (resolve-from-coordinates)

```
GET /geo/1.0/direct?q={city name},{state code},{country code}&limit=5&appid={key}
GET /geo/1.0/reverse?lat={lat}&lon={lon}&limit=5&appid={key}
```

- `limit` matters on the forward call: same-named places are common (London, UK vs. London, US) — always request `limit>1` and let the user disambiguate rather than silently taking the first result.
- State code (`{state code}`) is only meaningful for US locations; omit it elsewhere.
- Use forward geocoding for a "search/add place" UI; use reverse geocoding to resolve a device's GPS fix (paired naturally with a location-fetching skill if the project has one) to a canonical place — prefer this over a generic on-device geocoder when the app is already talking to this API family, since it keeps place identifiers consistent between "typed search" and "detected location" entry points.

## Current + forecast — Retrofit shape

```kotlin
interface OpenWeatherApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double, @Query("lon") lon: Double,
        @Query("appid") apiKey: String, @Query("units") units: String = "metric"
    ): CurrentWeatherDto

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double, @Query("lon") lon: Double,
        @Query("appid") apiKey: String, @Query("units") units: String = "metric"
    ): ForecastResponseDto

    @GET("geo/1.0/direct")
    suspend fun searchPlaces(
        @Query("q") query: String, @Query("limit") limit: Int = 5, @Query("appid") apiKey: String
    ): List<GeocodeResultDto>
}
```

Run current + forecast concurrently per place (`async`/`awaitAll`), not sequentially — they're independent calls and the free tier's rate limit (60 calls/min, generous) has no issue with parallel requests.

## The core technique: aggregating 3-hour steps into daily buckets

`data/2.5/forecast` has **no native daily object** — this is the one piece of real logic the integration needs, and it generalizes to any interval-sampled forecast API (stock price snapshots, sensor readings, etc.), not just weather:

```kotlin
fun aggregateToDailyBuckets(entries: List<ForecastEntryDto>, tzOffsetSeconds: Int): List<DailyBucket> {
    val zone = ZoneOffset.ofTotalSeconds(tzOffsetSeconds)   // use the PLACE's timezone, not the device's —
                                                              // matters when checking weather for a place in
                                                              // a different timezone than the user
    return entries
        .groupBy { Instant.ofEpochSecond(it.dt).atZone(zone).toLocalDate() }
        .map { (date, dayEntries) ->
            DailyBucket(
                date = date,
                min = dayEntries.minOf { it.main.tempMin },
                max = dayEntries.maxOf { it.main.tempMax },
                // dominant condition = most frequent across the day's slices, NOT just the first slice —
                // avoids "shows sunny" when only a 3am slice happened to be clear
                dominantCondition = dayEntries.flatMap { it.weather }
                    .groupingBy { it.main }.eachCount().maxByOrNull { it.value }?.key,
                // representative icon: pick a mid-day slice, not the first (often midnight) one
                icon = dayEntries.getOrNull(dayEntries.size / 2)?.weather?.firstOrNull()?.icon
            )
        }
        .sortedBy { it.date }
        // first/last groups are usually partial days — decide explicitly whether to display them,
        // and be consistent regardless of what time of day the aggregation runs
}
```

Two decisions to make deliberately, not by accident, every time this technique is applied:
1. **Whose timezone** — group by the *place's* local date (via the API's provided UTC offset), not the device's, whenever the place being queried can differ from the user's own location.
2. **Partial boundary buckets** — the first and last groups from an N-day interval series are rarely full calendar days; decide once whether to show/hide them and apply that rule consistently.

## Rate limits & scaling math (reference — only relevant at multi-city/high-frequency scale)

Free tier: 60 calls/min, 1,000,000 calls/month. Each "refresh" of one place costs **2 calls** (current + forecast) plus occasional geocoding calls during search. If syncing periodically across multiple saved places, multiply accordingly: 10 saved places × 2 calls × hourly sync = 480 calls/day for one user — comfortably within free tier even at that scale, but worth having the arithmetic ready if asked.

## API key configuration

Store the key in `local.properties` (already gitignored by default in Android
Studio projects — confirm with `grep local.properties .gitignore`), expose it
via `BuildConfig` at compile time, and attach it to every request with a
single OkHttp interceptor rather than a `@Query("appid")` param repeated on
every endpoint method:

```kotlin
// build.gradle.kts (module level)
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }
        ?.let { load(FileInputStream(it)) }
}
android {
    defaultConfig {
        buildConfigField("String", "OPEN_WEATHER_API_KEY",
            "\"${localProperties.getProperty("OPEN_WEATHER_API_KEY", "")}\"")
    }
    buildFeatures { buildConfig = true }   // required explicitly on AGP 8+
}
```

```kotlin
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newUrl = chain.request().url.newBuilder()
            .addQueryParameter("appid", apiKey).build()
        return chain.proceed(chain.request().newBuilder().url(newUrl).build())
    }
}
// Hilt module: OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor(BuildConfig.OPEN_WEATHER_API_KEY))
```

Retrofit interface methods then never declare an `appid`/`apiKey` parameter —
one interceptor, one place the key is referenced in app code. If a real key
is ever pasted into chat, a script, or briefly committed, rotate it on the
OpenWeatherMap dashboard rather than assuming it's fine.

## Common mistakes

- Hardcoding the API key as a string literal in a `.kt` file or committing `local.properties` — use `BuildConfig` + an interceptor instead (see above).
- Using `q=`/`id=` directly against `/data/2.5/weather` or `/data/2.5/forecast` instead of resolving through `/geo/1.0/direct` first.
- Forward-geocoding without `limit>1` and silently taking the first result — breaks on same-named places.
- Aggregating 3-hour slices into daily buckets using device timezone instead of the place's own offset.
- Picking the first (often midnight) slice's icon/condition as the "day's" representative instead of the mid-day slice or the most frequent condition.
