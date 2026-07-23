# networking.md

## Retrofit + kotlinx.serialization

- Converter: `json.asConverterFactory("application/json".toMediaType())`.
- `Json` config: `ignoreUnknownKeys = true`, `isLenient = true`, `explicitNulls = false` — provide as `@Singleton`.
- All Retrofit functions are `suspend`. One interface per API domain.
- Use `Response<T>` only when HTTP status codes must be inspected; prefer `T` directly otherwise.
- `BASE_URL` via `BuildConfig`. Logging: `BODY` in debug, `NONE` in release.

## DTOs
- Annotate with `@Serializable`. Use `@SerialName` on every field.
- Nullable fields must default to `null`; optional fields must have a default value.
- DTOs never leave the data layer — always map to domain models first.

## Mappers
- Pure extension functions: `fun UserDto.toDomain(): User`.
- Live in `data/remote/mapper/`. No Android dependencies.

## Repository
- Wrap API calls with `runCatching {}` — in the repository only, never in use cases or ViewModels.
- Inject `CoroutineDispatcher` via Hilt; use `withContext(dispatcher)`.
- Return `Result<T>`. Map exceptions to domain-meaningful errors where needed.
