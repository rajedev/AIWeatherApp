# compose.md

## Screens
- Stateless composables — receive `UiState` + lambda callbacks only.
- Never pass a ViewModel below the screen-level composable.
- No business logic inside composables.
- Every composable accepting layout must expose `modifier: Modifier = Modifier`.
- Always consume `PaddingValues` from `Scaffold`.

## State & Events
- ViewModel exposes `StateFlow<UiState>` — collect with `collectAsStateWithLifecycle()`, never `collectAsState()`.
- One-shot events via `Channel<UiEvent>(Channel.BUFFERED)` — collect in `LaunchedEffect(Unit)`.
- UI state classes annotated `@Immutable` or `@Stable`.

## ViewModel
- Annotate `@HiltViewModel` + `@Inject constructor`.
- Single `onAction(action: Action)` entry point; delegate to private functions.
- `init` block triggers initial load via `onAction`.
- Never use `GlobalScope`. Flows must have `.catch {}`.

## Previews
- Use `@PreviewLightDark`. Keep previews at the bottom of the screen file or in a `*Preview.kt` file.
- Feed previews with hardcoded `UiState` — never a real ViewModel.
