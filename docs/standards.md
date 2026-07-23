# standards.md

## Naming
| Artifact                    | Pattern                                                  |
|-----------------------------|----------------------------------------------------------|
| DTO                         | `UserDto`                                                |
| Mapper                      | `fun UserDto.toDomain()`                                 |
| Use case                    | `GetUsersUseCase`                                        |
| Repository interface / impl | `UserRepository` / `UserRepositoryImpl`                  |
| ViewModel / Screen          | `UserListViewModel` / `UserListScreen`                   |
| State / Event / Action      | `UserListUiState` / `UserListUiEvent` / `UserListAction` |
| Route                       | `Route.UserDetail`                                       |
| Hilt module                 | `NetworkModule`, `RepositoryModule`                      |

## Resource naming
- Icons: `ic_*`, backgrounds: `bg_*`, images: `img_*`. All `snake_case`.

## SonarQube & Lint Standards

### Code style
- Max line length: **120 characters**.
- No wildcard imports (`import foo.bar.*`).
- One class / top-level declaration per file.
- All `when` expressions must be **exhaustive** — add an `else` branch only for truly open hierarchies.

### Nullability
- Prefer non-null types; use `?` only when `null` is a meaningful state.
- Never use `!!` — use `?: return`, `?: error(...)`, or safe-call chains instead.
- Avoid `lateinit var` in production code; prefer `by lazy` or constructor injection.

### Coroutines
- Inject `CoroutineDispatcher` via Hilt — never hardcode `Dispatchers.IO`.
- Never use `GlobalScope`.
- All `Flow` operators that can throw must be wrapped with `.catch { }`.

### Functions & classes
- Functions: max **30 lines** (Sonar default cognitive complexity ≤ 15).
- Classes: max **200 lines** — split large classes by responsibility.
- No more than **5 parameters** per function; use a data class for grouped params.
- No magic numbers — extract to named `const val`.

### Compose-specific lint
- Annotate UI state classes with `@Immutable` or `@Stable` to suppress recomposition warnings.
- Every `@Composable` that accepts a `Modifier` must expose it as a parameter with `Modifier` as default.
- `PaddingValues` from `Scaffold` must always be applied — unused padding triggers a lint error.
- Previews must use `@PreviewLightDark` (or `@Preview`) and be in a dedicated `*Preview.kt` file or at the bottom of the screen file.

### Sonar suppressions
- Never suppress a Sonar issue without a `// NOSONAR: <reason>` comment.
- Do not suppress `kotlin:S1192` (duplicate string literals) — extract to constants instead.

### Resource naming (lint)
- Compose `stringResource` IDs: `snake_case` in `strings.xml`.
- Drawable IDs: `ic_` prefix for icons, `bg_` for backgrounds, `img_` for images.

---

## Do's and Don'ts

**Do**
- Use `@SerialName` on every DTO field.
- Return `Result<T>` from repositories; propagate it through use cases.
- Annotate `@Stable` / `@Immutable` on all Compose UI state classes.
- Inject `CoroutineDispatcher` via Hilt (`@Named("io")` etc.).
- Write mapper extension functions in a dedicated `mapper/` package.
- Keep routes as `@Serializable` data objects/classes — no string literals.

**Don't**
- Don't use `!!` — ever.
- Don't use wildcard imports.
- Don't import `android.*` in the domain layer.
- Don't call repository methods from a ViewModel — always go through a use case.
- Don't hardcode `Dispatchers.IO` — inject the dispatcher.
- Don't put `Flow` collection or coroutine launching inside composables.
- Don't share a `UiState` class across multiple screens.
- Don't use `GlobalScope`.

