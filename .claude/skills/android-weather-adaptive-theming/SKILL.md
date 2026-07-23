---
name: android-weather-adaptive-theming
description: Use this skill whenever building or extending a weather app's UI where the visual theme (colors, icon, surface tint) should reflect the current weather condition — hot, sunny, rainy, cloudy, winter/snow, night, etc. Trigger on mentions of "weather theme", "dynamic weather colors", "mood-based UI", "condition-based background/theme", or when adding a new screen/component to an existing weather app that should stay visually consistent with the app's weather-driven theming. Also trigger when the request is to add a new weather mood/condition, a new screen that displays weather data, or a widget/component that should pick up the current theme. This is a UI/design-system skill scoped to one app's theming layer — pair with whatever Compose/architecture conventions the project already uses.
---

# Weather-Adaptive Theming (Compose)

The goal: exactly **one** place decides "what does hot/sunny/rainy/cloudy/winter/night look like," and every screen or component consumes that decision the same way — never a per-screen `if (condition == "Rain") Color.Blue else ...` scattered across composables. When a new screen, widget, or mood gets added later, it plugs into the existing system instead of forking it.

## Core mental model

```
Weather data (condition, temp, local hour)
        │
        ▼
  resolveMood(...)  ── ONE function, pure, deterministic
        │
        ▼
  WeatherMood enum value
        │
        ▼
  toThemeColors()  ── ONE mapping, mood → colors/icon
        │
        ▼
  CompositionLocal or shared ViewModel state ── exposed app-wide
        │
        ▼
  Every screen/component reads from the SAME theme source
  (hero card, app bar tint, widget background, notification accent — all of it)
```

## 1. Mood resolution — factors in more than just the condition string

```kotlin
enum class WeatherMood { HOT, SUNNY, RAINY, CLOUDY, WINTER, NIGHT }

fun resolveMood(conditionMain: String, currentTemp: Double, localHour: Int): WeatherMood = when {
    // Night check first — a "clear" sky at 2am shouldn't theme as SUNNY
    localHour !in 6..18 && conditionMain in setOf("Clear", "Clouds") -> WeatherMood.NIGHT
    currentTemp >= 35 -> WeatherMood.HOT
    currentTemp <= 5 || conditionMain == "Snow" -> WeatherMood.WINTER
    conditionMain in setOf("Rain", "Drizzle", "Thunderstorm") -> WeatherMood.RAINY
    conditionMain == "Clouds" -> WeatherMood.CLOUDY
    else -> WeatherMood.SUNNY
}
```

**When adding a new mood later** (e.g. `FOGGY`, `STORMY`, `HEATWAVE` as distinct from plain `HOT`): add the enum case, add one branch here, add one entry in the color map below — nothing else in the app should need to change. If a change requires touching more than these two functions, something has leaked the mood logic into a screen and should be refactored back.

## 2. Color mapping — one table, semantic not decorative

```kotlin
data class WeatherThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val surfaceTint: Color,   // subtle wash for backgrounds, not a loud gradient
    val icon: ImageVector
)

fun WeatherMood.toThemeColors(): WeatherThemeColors = when (this) {
    HOT    -> WeatherThemeColors(Color(0xFFD85A30), Color.White, Color(0xFFFAECE7), Icons.Filled.LocalFireDepartment)
    SUNNY  -> WeatherThemeColors(Color(0xFFBA7517), Color.White, Color(0xFFFAEEDA), Icons.Filled.WbSunny)
    RAINY  -> WeatherThemeColors(Color(0xFF185FA5), Color.White, Color(0xFFE6F1FB), Icons.Filled.Umbrella)
    CLOUDY -> WeatherThemeColors(Color(0xFF5F5E5A), Color.White, Color(0xFFF1EFE8), Icons.Filled.Cloud)
    WINTER -> WeatherThemeColors(Color(0xFF0F6E56), Color.White, Color(0xFFE1F5EE), Icons.Filled.AcUnit)
    NIGHT  -> WeatherThemeColors(Color(0xFF3C3489), Color.White, Color(0xFFEEEDFE), Icons.Filled.NightsStay)
}
```

Color choice should map to the *feeling* of the condition (warm ramps for heat, cool for cold/rain, neutral gray for overcast), not be picked arbitrarily or cycled — see "Common mistakes" for what goes wrong otherwise.

## 3. Expose the theme app-wide, not per-screen

```kotlin
val LocalWeatherTheme = compositionLocalOf { WeatherMood.SUNNY.toThemeColors() }

@Composable
fun WeatherAdaptiveTheme(mood: WeatherMood, content: @Composable () -> Unit) {
    val target = mood.toThemeColors()
    val animatedPrimary by animateColorAsState(target.primary, tween(600), label = "primary")
    val animatedTint by animateColorAsState(target.surfaceTint, tween(600), label = "tint")

    CompositionLocalProvider(
        LocalWeatherTheme provides target.copy(primary = animatedPrimary, surfaceTint = animatedTint)
    ) {
        content()
    }
}

// Wrap once, near the app/nav root — every descendant screen picks it up automatically:
// WeatherAdaptiveTheme(mood = uiState.mood) { AppNavHost(...) }
```

Any new screen or component just reads `LocalWeatherTheme.current` — it never needs to know how mood was resolved or re-derive colors itself. This is what makes "a new screen extends the same theme" actually true instead of aspirational: the mechanism is structural (CompositionLocal), not a convention someone has to remember to follow.

**600ms crossfade, not a hard cut**: weather refreshes periodically and the user may switch between saved cities with different conditions — an instant color swap reads as a glitch; a short animated transition reads as intentional.

## 4. Accessibility — non-negotiable, check every time a mood/color is added

- Every `primary`/`onPrimary` pair must pass WCAG AA (4.5:1) for text — verify with each new color, don't assume "looks fine" is "passes contrast."
- **Never encode the mood by color alone** — the icon (flame, sun, umbrella, cloud, snowflake, moon, or whatever a new mood adds) must always accompany the color, for colorblind users and anyone using a colorblind display filter.
- Respect system dark mode: derive tint/surface colors as adjustments layered on `MaterialTheme.colorScheme`, not as hardcoded absolutes that ignore the user's OS dark-mode preference.

## Extending this system — checklist for new screens/features

When a new screen, widget, or feature is added to a weather app already using this pattern:
1. Does it display current-condition-relevant content? → wrap/read from `LocalWeatherTheme`, don't introduce a new color source.
2. Does it need a mood that doesn't exist yet? → add one enum case + one `resolveMood` branch + one `toThemeColors` entry, nothing else.
3. Does it need the mood's icon somewhere (app bar, notification, widget)? → read `LocalWeatherTheme.current.icon`, don't hardcode a different icon per screen.
4. Building a home-screen widget or notification that can't use Compose theming directly? → still resolve mood through the same `resolveMood()` function so the color/icon shown there matches what the in-app UI shows for the same conditions.

## Common mistakes

- Re-deriving mood/color logic per-screen with local `if`/`when` blocks instead of reading from the shared theme source — the moment two screens' `if` chains drift apart, the app looks inconsistent.
- Mapping mood to color arbitrarily or cycling through a palette instead of choosing colors that match the condition's actual feel (warm for heat, cool for cold/rain, neutral for overcast).
- Hard-cutting colors on refresh instead of animating — reads as a rendering glitch, not a deliberate theme change.
- Deriving mood from the condition string alone, ignoring temperature and local time of day — produces a "sunny" theme at 2am or a "cloudy" theme on a 40°C overcast day that should probably read as HOT.
- Adding a new mood/condition without an accompanying icon, or with primary/onPrimary colors that fail contrast — accessibility regressions creep in exactly when new moods get added quickly.
