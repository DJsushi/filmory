# Android Clean Code Guidelines (Bookmory Clone)

> **For the AI coding assistant:** Read this file fully before writing or changing any code in this project. Follow these rules unless the user explicitly overrides one. When a rule conflicts with a quick hack, follow the rule. When unsure, choose the option that keeps layers separated and code testable.

These guidelines follow the clean-code and clean-architecture style taught by Philipp Lackner: Kotlin-first, Jetpack Compose, strict layer separation, multi-module structure, MVI-style presentation, a typed `Result` for error handling, and dependency injection with Koin.

---

## 1. Project Goal

An Android app for tracking movies and series (Filmory), modeled on Bookmory. The code examples in this file use `Book` names from the original template. Apply the rules with Filmory names: `Title`, `Watching`, `Note`, `Episode`. The full product scope is in `FEATURES.md`.

- Add titles by searching TMDB or by hand
- Track status: **Wishlist**, **Watched**, **On hold**, **Dropped** (and **Watching** for series in progress)
- Several watchings per title, each with its own dates, rating, review and notes
- Series progress: a current-episode pointer plus per-episode check marks
- Notes (Scene, Summary, Thought, Question) with an optional timestamp, on movies, series and episodes
- Collections, wishlist, watch later, calendar
- Offline-first: the local database is the single source of truth
- Backup and restore as a `.filmorybkp` zip
- Later: streaks, statistics, sync

---

## 2. Tech Stack (defaults)

| Concern | Choice |
|---|---|
| Language | Kotlin only, no Java |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture (presentation / domain / data) + MVI-style UI state |
| DI | Koin |
| Local DB | Room |
| Preferences | DataStore (never SharedPreferences) |
| Networking | Ktor client (or Retrofit if the user prefers) + kotlinx.serialization |
| Async | Coroutines + Flow |
| Navigation | Navigation Compose (type-safe routes with `@Serializable`) |
| Images | Coil |
| Dependency versions | Gradle version catalog (`libs.versions.toml`) |
| Build logic | Convention plugins in `build-logic` |
| Testing | kotlin.test, hand-written fakes (MockK is JVM-only), Turbine, AssertK |

Do not add libraries that are not listed without asking the user.

**Filmory is Kotlin Multiplatform.** Read this file with these adaptations (details in `IMPLEMENTATION.md`):
- Use the multiplatform version of each library (Compose Multiplatform, Room KMP, Ktor with per-platform engines, Koin, multiplatform ViewModel and Navigation, DataStore, Coil 3).
- Common code has no Android or JVM imports: no `Context`, no `java.time`, no `SQLiteFullException`-style Android exceptions. Use kotlinx-datetime and wrap platform errors in the data layer. Platform code goes in `androidMain`/`iosMain` behind `expect`/`actual` or interfaces.
- Strings come from Compose Multiplatform resources. `UiText.StringResource` holds a resource key, not an Android `@StringRes` id, and resolves it in composables.
- Tests use `kotlin.test` in `commonTest` with Turbine and fakes, not JUnit5.

---

## 3. Architecture Overview

Three layers, with a strict dependency direction:

```
presentation  ──►  domain  ◄──  data
```

- **domain** is pure Kotlin. It knows nothing about Android, Room, Ktor, or Compose.
- **data** depends on domain and implements its interfaces.
- **presentation** depends on domain and never on data.
- `app` module wires everything together (DI, navigation, `Application` class).

### The rules that matter most

1. **The domain layer has no Android imports.** No `Context`, no `@StringRes`, no `Uri`, no Room annotations.
2. **Domain defines interfaces, data implements them.** (`BookRepository` in domain, `OfflineFirstBookRepository` in data.)
3. **Models never cross layers raw.** Each layer has its own model and mappers convert between them.
4. **ViewModels depend on interfaces from domain**, never on concrete data classes.
5. **No business logic in composables, DAOs, or DTOs.**

---

## 4. Module Structure

Prefer a multi-module setup organized by feature, then by layer.

```
bookmory/
├── app/                          # Application class, MainActivity, NavHost, DI wiring
├── build-logic/
│   └── convention/               # Convention plugins (android-library, compose, feature, etc.)
├── core/
│   ├── domain/                   # Shared domain models, Result, Error, DataError, utils
│   ├── data/                     # Shared repository impls, network client, mappers
│   ├── database/                 # Room database, DAOs, entities
│   ├── presentation/
│   │   ├── designsystem/         # Theme, colors, typography, reusable composables
│   │   └── ui/                   # UiText, ObserveAsEvents, shared UI helpers
│   └── test/                     # Shared fakes and test utilities
├── library/                      # Library feature (shelves, book list)
│   ├── domain/
│   ├── data/
│   └── presentation/
├── book/                         # Book detail, notes, quotes, rating
│   ├── domain/
│   ├── data/
│   └── presentation/
├── session/                      # Reading timer and session logging
│   ├── domain/
│   ├── data/
│   └── presentation/
├── stats/                        # Statistics and charts
│   ├── domain/
│   ├── data/
│   └── presentation/
└── search/                       # Search and add books from remote API
    ├── domain/
    ├── data/
    └── presentation/
```

**Filmory uses a single Kotlin Multiplatform module with the same package structure** (`dev.gaens.filmory.feature.<name>.{domain,data,presentation}` and `dev.gaens.filmory.core.*`), so splitting into modules later is trivial. This is decided. Do not ask, and do not create the multi-module layout above.

Package naming: `com.<yourname>.bookmory.<module>.<layer>` e.g. `com.example.bookmory.library.domain`.

---

## 5. Domain Layer

Pure Kotlin. Contains models, repository interfaces, use cases (only when they add value), and error types.

### 5.1 Models

Immutable data classes. Use `val` only. Use value types that express meaning.

```kotlin
data class Book(
    val id: BookId,
    val title: String,
    val authors: List<String>,
    val coverUrl: String?,
    val pageCount: Int?,
    val status: ReadingStatus,
    val rating: Rating?,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val addedAt: Instant,
)

@JvmInline value class BookId(val value: Long)

enum class ReadingStatus { WANT_TO_READ, READING, FINISHED, PAUSED, DROPPED }

@JvmInline
value class Rating(val value: Float) {
    init { require(value in 0f..5f) { "Rating must be between 0 and 5" } }
}
```

Other core models: `ReadingSession`, `Note`, `Quote`, `ReadingGoal`, `ReadingStats`.

### 5.2 Result and Error types (core/domain)

Use a typed `Result` instead of throwing exceptions across layers. Do not use `kotlin.Result`.

```kotlin
package com.example.bookmory.core.domain.util

interface Error

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.example.bookmory.core.domain.util.Error>(val error: E) : Result<Nothing, E>
}

inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> = when (this) {
    is Result.Error -> Result.Error(error)
    is Result.Success -> Result.Success(map(data))
}

fun <T, E : Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> = map { }

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> = when (this) {
    is Result.Error -> this
    is Result.Success -> { action(data); this }
}

inline fun <T, E : Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> = when (this) {
    is Result.Error -> { action(error); this }
    is Result.Success -> this
}

typealias EmptyResult<E> = Result<Unit, E>
```

```kotlin
sealed interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT, TOO_MANY_REQUESTS, NO_INTERNET,
        SERIALIZATION, SERVER_ERROR, UNAUTHORIZED, NOT_FOUND, UNKNOWN
    }
    enum class Local : DataError {
        DISK_FULL, NOT_FOUND, UNKNOWN
    }
}
```

Rules:
- Errors are **enums or sealed types**, never raw strings.
- Feature-specific errors (e.g. `PasswordError`, `BookValidationError`) implement `Error` too.
- **Never swallow `CancellationException`.** Always rethrow it (`coroutineContext.ensureActive()` in catch blocks).

### 5.3 Repository interfaces

```kotlin
interface BookRepository {
    fun observeBooks(): Flow<List<Book>>
    fun observeBook(id: BookId): Flow<Book?>
    suspend fun upsertBook(book: Book): EmptyResult<DataError.Local>
    suspend fun deleteBook(id: BookId)
}

interface RemoteBookDataSource {
    suspend fun searchBooks(query: String): Result<List<Book>, DataError.Network>
    suspend fun findByIsbn(isbn: String): Result<Book, DataError.Network>
}
```

- Reads that can change over time return `Flow`. One-shot operations are `suspend` functions.
- Suspend functions that can fail return `Result<..., SomeError>`.
- Local data sources return `Flow` from Room and rarely need `Result` for reads.

### 5.4 Use cases

Only create a use case when it holds **real business logic** or combines several repositories. Do not create pass-through use cases like `GetBooksUseCase` that just call one repository method.

Good examples: `CalculateReadingStreak`, `FinishBook` (sets status, finish date, closes active session), `ValidateBookInput`.

```kotlin
class CalculateReadingStreak(private val clock: Clock = Clock.System) {
    operator fun invoke(sessions: List<ReadingSession>): Int { /* pure logic */ }
}
```

Use cases are small, single-purpose, and pure where possible, so they are trivially unit-testable.

---

## 6. Data Layer

Implements domain interfaces. Contains Room entities, DAOs, DTOs, mappers, and repository implementations.

### 6.1 Offline-first

The local Room database is the **single source of truth**. The UI observes the database. Remote calls write into the database, and the UI updates automatically through Flow.

```kotlin
class OfflineFirstBookRepository(
    private val bookDao: BookDao,
    private val remote: RemoteBookDataSource,
    private val applicationScope: CoroutineScope,
) : BookRepository {

    override fun observeBooks(): Flow<List<Book>> =
        bookDao.observeBooks().map { entities -> entities.map { it.toBook() } }

    override suspend fun upsertBook(book: Book): EmptyResult<DataError.Local> =
        try {
            bookDao.upsert(book.toBookEntity())
            Result.Success(Unit)
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
}
```

### 6.2 Entities, DTOs, and mappers

Three separate models per concept: **domain model**, **Room entity**, **network DTO**. Never annotate domain models with Room or serialization annotations.

```kotlin
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val authors: String,          // stored via a TypeConverter or a separate table
    val coverUrl: String?,
    val pageCount: Int?,
    val status: String,           // enum name
    val rating: Float?,
    val startedAtEpochMillis: Long?,
    val finishedAtEpochMillis: Long?,
    val addedAtEpochMillis: Long,
)

@Serializable
data class BookDto(
    val id: String,
    val volumeInfo: VolumeInfoDto,
)
```

Mapper conventions (in the data module, as top-level extension functions, named `toX`):

```kotlin
fun BookEntity.toBook(): Book = Book(/* ... */)
fun Book.toBookEntity(): BookEntity = BookEntity(/* ... */)
fun BookDto.toBook(): Book = Book(/* ... */)
```

- Mappers are pure functions, easy to unit test.
- Store timestamps as epoch millis (`Long`) in Room; convert to `Instant` in mappers.
- Store enums as strings, not ordinals.

### 6.3 Network

- Wrap every network call in a `safeCall` helper that catches exceptions and maps HTTP status codes to `DataError.Network`.
- Responses are mapped from DTO to domain model **inside the data layer**.
- Never expose `HttpResponse` or DTOs to domain or presentation.

```kotlin
suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}
```

### 6.3 Dispatchers and scopes

- Room and Ktor already handle their own threading. Don't add `withContext(Dispatchers.IO)` unless you do blocking work.
- Inject dispatchers via a `DispatcherProvider` interface when needed so tests can replace them.
- For work that must outlive a screen (syncing, backups), inject an **application-scoped `CoroutineScope`** (`SupervisorJob() + Dispatchers.Default`) instead of using `GlobalScope`.

---

## 7. Presentation Layer (MVI-style)

Each screen has exactly these pieces, in this order:

1. `XState`: what the UI shows
2. `XAction`: what the user does (UI → ViewModel)
3. `XEvent`: one-time effects (ViewModel → UI): navigation, snackbar, toast
4. `XViewModel`
5. `XScreenRoot`: composable connected to the ViewModel
6. `XScreen`: stateless composable that renders state
7. `@Preview`

### 7.1 State, Action, Event

```kotlin
data class BookListState(
    val books: List<BookUi> = emptyList(),
    val selectedStatus: ReadingStatus? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
)

sealed interface BookListAction {
    data class OnBookClick(val id: Long) : BookListAction
    data class OnStatusFilterChange(val status: ReadingStatus?) : BookListAction
    data class OnSearchQueryChange(val query: String) : BookListAction
    data object OnAddBookClick : BookListAction
}

sealed interface BookListEvent {
    data class Error(val message: UiText) : BookListEvent
}
```

- State is a single immutable data class with **sensible defaults** for every field.
- Actions are named `OnSomethingClick` / `OnSomethingChange`: past-tense-free, describing what the user did.
- State contains only what the UI needs. Use UI models (`BookUi`) with formatted strings, not raw domain models, when formatting is needed.
- Don't put one-time events (snackbars, navigation) in state.

### 7.2 ViewModel

```kotlin
class BookListViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookListState())
    val state = _state
        .onStart { observeBooks() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            BookListState()
        )

    private val eventChannel = Channel<BookListEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnStatusFilterChange ->
                _state.update { it.copy(selectedStatus = action.status) }
            is BookListAction.OnSearchQueryChange ->
                _state.update { it.copy(searchQuery = action.query) }
            else -> Unit // handled in the Root via navigation callbacks
        }
    }

    private fun observeBooks() {
        bookRepository.observeBooks()
            .onEach { books ->
                _state.update { it.copy(books = books.map { book -> book.toBookUi() }) }
            }
            .launchIn(viewModelScope)
    }
}
```

Rules:
- Expose `StateFlow<State>` publicly, keep the mutable version private.
- Expose a **single** `onAction(action)` entry point. Don't expose many public functions.
- Use `_state.update { it.copy(...) }`, never `_state.value = _state.value.copy(...)`.
- Use `Channel` + `receiveAsFlow()` for events, not `SharedFlow` (events must not be lost or replayed).
- ViewModels never hold references to `Context`, `View`, or composable state.
- Navigation is **not** done inside the ViewModel. The ViewModel passes through actions and the Root composable invokes navigation lambdas.

### 7.3 Root + Screen composables

Split every screen in two:

```kotlin
@Composable
fun BookListScreenRoot(
    onBookClick: (Long) -> Unit,
    onAddBookClick: () -> Unit,
    viewModel: BookListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BookListEvent.Error ->
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
        }
    }

    BookListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is BookListAction.OnBookClick -> onBookClick(action.id)
                BookListAction.OnAddBookClick -> onAddBookClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
private fun BookListScreen(
    state: BookListState,
    onAction: (BookListAction) -> Unit,
) { /* stateless UI */ }
```

- `ScreenRoot`: knows the ViewModel, collects state, handles events and navigation.
- `Screen`: stateless, receives only `state` and `onAction`. This is what previews and UI tests use.
- Always collect with `collectAsStateWithLifecycle()`.

### 7.4 Events helper

```kotlin
@Composable
fun <T> ObserveAsEvents(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle, key1, key2, flow) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}
```

### 7.5 UiText (strings from ViewModel without Context)

ViewModels must not use `Context` or `getString`. Use `UiText`:

```kotlin
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(@StringRes val id: Int, val args: Array<Any> = arrayOf()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(id, *args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(id, *args)
    }
}
```

Map domain errors to `UiText` in the presentation layer: `fun DataError.asUiText(): UiText`. Domain never knows about string resources.

### 7.6 Navigation

- Type-safe routes with `@Serializable` objects/data classes.
- Each feature exposes a `NavGraphBuilder.xxxGraph(navController)` extension. The `app` module composes them.
- Screens receive navigation as **lambdas** (`onBookClick: (Long) -> Unit`), never a `NavController`.

---

## 8. Compose Conventions

- **State hoisting**: composables are stateless where possible; state lives in the ViewModel or is hoisted to the caller.
- Every reusable composable takes `modifier: Modifier = Modifier` as the **first optional parameter** and applies it to the root element.
- Parameters order: required params, `modifier`, optional params, trailing content lambda.
- Never pass the ViewModel into child composables. Pass state and lambdas.
- Use `LazyColumn`/`LazyVerticalGrid` with stable `key`s for lists.
- Use `remember`/`derivedStateOf` only when there's a measurable reason.
- Mark UI models with `@Immutable`/`@Stable` or use immutable collections if recomposition becomes an issue.
- **Every screen and reusable component gets a `@Preview`**, wrapped in the app theme, with realistic fake data.
- No hardcoded colors, text sizes, or dimensions in screens. Use `MaterialTheme` and design system tokens.
- No hardcoded user-visible strings. Use `strings.xml` (and `UiText` from ViewModels).
- Reusable UI (buttons, text fields, book cover card, rating bar, status chip, top bar) lives in `core/presentation/designsystem`, prefixed with the app name (e.g. `BookmoryButton`) or a clear role name.

---

## 9. Dependency Injection (Koin)

- One Koin `module` per Gradle module, named `<feature><Layer>Module`.
- Use the constructor DSL (`singleOf`, `viewModelOf`, `factoryOf`) with `.bind<Interface>()`.
- Bind implementations to their **domain interfaces**.

```kotlin
val libraryDataModule = module {
    singleOf(::OfflineFirstBookRepository).bind<BookRepository>()
}

val libraryPresentationModule = module {
    viewModelOf(::BookListViewModel)
}

val coreDataModule = module {
    single { HttpClientFactory.build() }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
```

- The `Application` class calls `startKoin { androidContext(this@App); modules(...) }`.
- Never use a service locator (`get()`/`inject()`) inside domain classes or composables other than `koinViewModel()` in Root composables.

---

## 10. Coroutines and Flow Rules

- Use `viewModelScope` in ViewModels; inject an `applicationScope` for work that must outlive the UI.
- No `GlobalScope`. No `runBlocking` in production code.
- Never catch `Exception` broadly without rethrowing `CancellationException`.
- Prefer `Flow` over callbacks. Use `combine`, `flatMapLatest`, `debounce` for search.
- Convert cold flows to state with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), initial)`.
- Search input: debounce ~300ms, `distinctUntilChanged`, `flatMapLatest`.
- Suspend functions are **main-safe**: callers never need to switch dispatchers.

---

## 11. Code Style and Naming

### Clean code rules

- **Names reveal intent.** `finishedBooksThisYear`, not `list2`. No abbreviations except universally known ones (`id`, `url`).
- **Small functions that do one thing.** If a function needs a comment to explain a section, extract that section into a named function.
- **No comments that restate code.** Comment the *why*, not the *what*. Prefer self-explanatory code.
- **Immutability by default.** `val` over `var`, `List` over `MutableList` in public APIs, `copy()` for changes.
- **Early returns** over deep nesting.
- **No magic numbers or strings.** Extract named constants.
- **No god classes.** One class, one responsibility. If a ViewModel grows beyond ~250 lines, split logic into use cases or delegates.
- **Avoid nullability when a sealed type or default is clearer.** Never use `!!`.
- **Prefer sealed interfaces/classes and enums** over booleans and strings that encode state.
- **Prefer composition over inheritance.**
- **Expose interfaces, hide implementations.** Mark implementation classes `internal` where modules allow.
- **Visibility:** default to `private`, then `internal`, then public only if needed.
- **Dead code gets deleted**, not commented out.

### Naming conventions

| Thing | Convention | Example |
|---|---|---|
| Screen composable (connected) | `<Name>ScreenRoot` | `BookListScreenRoot` |
| Screen composable (stateless) | `<Name>Screen` | `BookListScreen` |
| State / Action / Event | `<Name>State`, `<Name>Action`, `<Name>Event` | `BookDetailState` |
| ViewModel | `<Name>ViewModel` | `BookDetailViewModel` |
| Repository interface | `<Thing>Repository` | `BookRepository` |
| Repository implementation | Describes strategy | `OfflineFirstBookRepository` |
| Data source | `<Local/Remote><Thing>DataSource` | `RemoteBookDataSource` |
| Room entity / DAO | `<Thing>Entity`, `<Thing>Dao` | `BookEntity`, `BookDao` |
| Network DTO | `<Thing>Dto` | `BookDto` |
| UI model | `<Thing>Ui` | `BookUi` |
| Mappers | `toX()` extension | `BookEntity.toBook()` |
| Koin modules | `<feature><Layer>Module` | `libraryDataModule` |
| Boolean | reads like a question | `isLoading`, `hasNotes` |
| Test functions | backtick sentences | `` `finishing a book sets finish date` `` |

### Formatting

- Follow the official Kotlin style guide. Use trailing commas in multi-line declarations.
- One top-level public class per file, file named after it. Related extensions/mappers can share a file named for their purpose (`BookMappers.kt`).
- Imports: no wildcards, remove unused.
- Max line length 120.

---

## 12. Testing

- **Test behavior, not implementation.** Prefer hand-written **fakes** (in `core/test`) over heavy mocking for repositories.
- Test names are backtick sentences: `` `when search query changes, results are filtered` ``.
- Structure: Arrange / Act / Assert (or Given / When / Then).
- Unit tests required for: use cases, mappers, validators, ViewModels, `Result` helpers.
- ViewModel tests use `Turbine` for flows and a `MainDispatcherExtension`/`TestDispatcher` rule.
- Room DAOs: instrumented tests with an in-memory database.
- Compose UI: test the stateless `Screen` composable with a given `State`.

```kotlin
class FakeBookRepository : BookRepository {
    private val books = MutableStateFlow<List<Book>>(emptyList())
    override fun observeBooks(): Flow<List<Book>> = books
    override fun observeBook(id: BookId) = books.map { list -> list.find { it.id == id } }
    override suspend fun upsertBook(book: Book): EmptyResult<DataError.Local> {
        books.update { (it.filterNot { b -> b.id == book.id }) + book }
        return Result.Success(Unit)
    }
    override suspend fun deleteBook(id: BookId) { books.update { it.filterNot { b -> b.id == id } } }
}
```

---

## 13. Bookmory-Specific Guidance

### Screens (see the mockup, eight screens drawn so far)

1. **Home**: continue-watching card, watch later, wishlist, calendar, collections, on hold, dropped, library
2. **Library**: grid/list of titles, filters (status, movie/series), sort, display settings
3. **Movie detail** and **Series detail**: poster, status, watchings, notes, and for series the seasons and episodes
4. **Add note**: type, season/episode, optional timestamp, text, image, date
5. **Notes tab**: all notes, random and favorite shortcuts, type filter
6. **Watched tab**: finished titles
7. **Calendar** and calendar settings
8. Not designed yet: Profile and Settings (theme, backup and restore), wishlist and collection lists, on-hold and dropped lists, TMDB search and add

### Domain rules to encode (in domain, not in UI)

- Until a watching has finished, the title status mirrors its latest watching. Once any watching has finished, the title stays **Watched** when a later watching is dropped or put on hold. Only that watching changes. A title with an in-progress watching also appears under Continue watching.
- Wishlist and watch later are flags on the title, not statuses.
- A note belongs to a watching. Adding a note to a title with no watching creates one first.
- Moving a watching to **finished** sets its finish date. Starting a watching sets its start date if empty.
- Movies have no progress. Series progress is a current-episode pointer plus per-episode check marks, both per watching. Advancing the pointer checks the current episode.
- Rating range is 0.0 to 5.0 in 0.5 steps (validate in the `Rating` type or a validator).
- A note belongs to a watching. The timestamp and episode are optional.
- Which titles appear on which calendar day follows the three calendar settings (finish dates, start to finish range, logged dates). Compute it in a use case, not in the ViewModel.

### Data suggestions

- Tables: `titles` (with `is_wishlisted` and a nullable watch-later position), `watchings`, `notes` (with optional image path), `note_comments`, `seasons`, `episodes`, `episode_checks` (with `checked_at`), `collections`, `collection_titles`. Use foreign keys with `onDelete = CASCADE` and indices on foreign keys.
- Use Room relations (`@Relation`, `@Embedded`) for "title with watchings and notes" queries and map to domain models in the repository.
- Use TMDB for search. Hide it behind `RemoteTitleDataSource` so it can be swapped.
- Keep migrations explicit (`AutoMigration` or manual). Never use `fallbackToDestructiveMigration` in release code.
- Give every record a stable id so sync can be added later.

---

## 14. Workflow: Adding a Feature

When asked to add or change a feature, follow this order:

1. **Domain**: model changes, repository interface methods, error types, use cases (if real logic).
2. **Data**: entity/DTO, DAO queries, mappers, repository implementation.
3. **DI**: register new bindings in the relevant Koin module.
4. **Presentation**: State, Action, Event, ViewModel, Screen + Root, UiText mapping for errors.
5. **Design system**: add reusable composables if a UI piece is used more than once.
6. **Navigation**: add route and wire it in the feature's nav graph.
7. **Previews** for every new composable.
8. **Tests**: use case, mapper, and ViewModel tests; add fakes if needed.

Before finishing, run through the checklist below.

---

## 15. Final Checklist (verify before submitting code)

- [ ] Domain layer has zero Android / Room / Ktor / Compose imports
- [ ] No layer depends "upward" (domain → data/presentation is forbidden)
- [ ] ViewModel depends only on domain interfaces
- [ ] Errors flow as `Result<D, E : Error>`, mapped to `UiText` in presentation
- [ ] `CancellationException` is never swallowed
- [ ] Screen has State / Action / Event / ViewModel / Root / stateless Screen / Preview
- [ ] State is immutable and updated with `.update { copy(...) }`
- [ ] One-time effects use a `Channel`, not state
- [ ] No hardcoded strings, colors, or dimensions in composables
- [ ] Every composable has a `modifier` parameter where appropriate
- [ ] Models are separated: domain / entity / DTO / UI, with mappers
- [ ] Koin bindings point to interfaces
- [ ] No `!!`, no `GlobalScope`, no `runBlocking`, no commented-out code
- [ ] Names are descriptive and follow the table in section 11
- [ ] New logic has unit tests; fakes are used instead of heavy mocking

---

## 16. When Requirements Are Unclear

- Ask the user **one focused question** rather than guessing on architectural choices (module layout, Retrofit vs Ktor, which API to use).
- For small ambiguities, pick the default from these guidelines, state the assumption in one line, and continue.
- Never silently break a rule in this file. If a rule must be bent, say so and explain why.