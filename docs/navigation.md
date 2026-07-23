# navigation.md

## Navigation 3 (nav3)

## Routes
- Define as `@Serializable` data objects / data classes inside a `sealed interface Route`.
- No string route literals anywhere.

## NavHost
- Use `rememberNavBackStack(Route.Start)` + `NavDisplay`. No `NavController`.
- Required `entryDecorators`: `rememberSceneSetupNavEntryDecorator`, `rememberSavedStateNavEntryDecorator`, `rememberViewModelStoreNavEntryDecorator`.
- `onBack = { backStack.removeLastOrNull() }`.

## Screens & navigation
- Screens receive navigation as lambdas — never manipulate `backStack` directly inside a composable.
- `AppNavHost` owns the `backStack` and wires lambdas to `backStack.add()` / `removeLastOrNull()`.
- Deep links registered at the `NavDisplay` level only.
