# architecture.md

## Architecture Rules

### Structure
```
di/
data/remote/api|dto|mapper/
data/repository/
domain/model|repository|usecase/
presentation/navigation/
presentation/ui/<feature>/
presentation/common/
```
### Dependency Rule

Dependencies always point **inward**:
```
Presentation → Domain ← Data
```

- **Domain** has zero Android / framework dependencies.
- **Data** implements domain interfaces; it never imports presentation classes.
- **Presentation** calls use cases only — never repositories directly.

### Layer Responsibilities

**Data Layer**
- Define Retrofit service interfaces under `api/`.
- DTOs are annotated with `@Serializable` and stay within the data layer.
- Mappers are pure extension functions: `fun UserDto.toDomain(): User`.
- Repositories catch all exceptions and return `Result<T>`.

**Domain Layer**
- Plain Kotlin — zero `android.*` imports.
- Use cases expose `suspend operator fun invoke()` or return `Flow<T>`.
- Repository interfaces define the contract only.

**Presentation Layer**
- ViewModels hold `StateFlow<UiState>` and expose `Channel<UiEvent>` for one-shot events.
- Screens are stateless composables receiving state and lambda callbacks.
- No business logic inside composables.
