# hilt.md

## Annotations
- `@HiltAndroidApp` on `Application`.
- `@AndroidEntryPoint` on `Activity` / `Fragment`.
- `@HiltViewModel` + `@Inject constructor` on every ViewModel.

## Modules
- `NetworkModule` — provides `Json`, `OkHttpClient`, `Retrofit`, API services. Scope: `@Singleton`.
- `RepositoryModule` — binds repository interfaces to implementations. Scope: `@Singleton`.
- Use `@Binds` for interface → impl; use `@Provides` for third-party / builder types.

## Scoping
- `@Singleton` for network, repository, and shared app-wide dependencies.
- `@ViewModelScoped` for dependencies tied to a single ViewModel lifecycle.
- Never use `@ActivityScoped` unless the dependency truly needs activity context.

## Dispatcher
- Provide named `CoroutineDispatcher` bindings (`@Named("io")`, `@Named("default")`).
- Inject dispatchers — never hardcode `Dispatchers.IO` in production code.
