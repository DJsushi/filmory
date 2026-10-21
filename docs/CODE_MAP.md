# Code map

Where things live. Paths are under `shared/src/commonMain/kotlin/dev/gaens/filmory/` unless noted. Every feature follows `feature/<name>/{domain,data,presentation}`; screens are `XScreen`, `XState`, `XViewModel` (plus `XAction` where the screen has many actions). Update this file when a feature or package is added, moved or removed.

## Entry points

- `App.kt`: root composable, theme, `NavHost` with every route.
- `navigation/Route.kt`: typed routes.
- `di/`: Koin modules (`CoreDataModule`, `FeatureDataModule`, `DomainModule`, `PresentationModule`, `PlatformModule`, `InitKoin`). A new class must be bound here.
- `androidApp/src/main/kotlin/dev/gaens/filmory/`: `MainActivity`, `FilmoryApplication` (starts Koin). Debug-only `DebugSeeder` and `SeedActivity` are in `androidApp/src/debug/`.
- Platform code: `shared/src/androidMain` (image resizer, dynamic color, picker, share), `shared/src/iosMain` (stubs).
- Tests: `shared/src/commonTest` (unit), `shared/src/androidDeviceTest` (Room `DatabaseTest`).

## core

- `core/domain`: `Result`, `DataError`, `IdGenerator`, `ImageResizer`.
- `core/database`: `AppDatabase`; `dao/` (Title, Watching, Episode, Note, Collection, Backup); `entity/` (one entity per table, `NoteRow` joined row, `DatabaseSnapshot` for backup).
- `core/data`: `HttpClientFactory`, `SafeCall`, `SafeDatabaseCall`, `TmdbConfig`, `AppDirectories`, mappers helpers.
- `core/presentation/designsystem`: theme (`Theme`, `Color`, `Type`, `Shape`, `Shadow`, `Motion`, `DynamicColor`, `Icons`); `components/` (bottom bar, app bar, cards, chips, FAB, poster, progress bar, star rating, header, badges, tiles).
- `core/presentation/richtext`: Markdown parsing and rich text field for notes.
- `core/presentation/ui`: `UiText`, `ObserveAsEvents`, formatting, labels, image picker and sharing interfaces.

## feature

| Feature | What it owns | Key files |
|---|---|---|
| `title` | Title (movie or series), status, detail screen, add/edit form, watch-later flag | `domain/ChangeTitleStatus`, `SaveTitleForm`, `WatchLater`; `presentation/TitleDetailScreen`, `TitleDetailViewModel`, `SeriesSection`, `form/TitleFormScreen` |
| `watching` | One viewing of a title: dates, state, rating, review; status derivation | `domain/StartWatching`, `ChangeWatchingState`, `SaveWatching`, `DetermineTitleStatus`, `SyncTitleStatus`, `FindContinueWatching` |
| `episode` | Seasons, episodes, per-episode checks (watched, skipped), current-episode marker | `domain/AdvanceEpisode`, `PlaceMarker`, `EpisodeCheck`, `Season` |
| `note` | Notes and comments on titles, watchings, episodes; list, detail, editor | `domain/AddNote`, `DeleteNote`; `presentation/list` (with `TitlePickerSheet`, the searchable "add a note to" picker, built on `BuildLibrary`), `detail`, `editor`, `components/NoteCard` |
| `home` | Home tab: watch-later slots, continue watching, drag and drop | `presentation/HomeScreen`, `HomeViewModel`, `WatchLaterEditor` |
| `library` | Library tab with presets, filters, sort and layout settings. The collection screen reuses its list. | `domain/BuildLibrary`; `presentation/LibraryScreen`, `LibrarySettingsSheet`, `components/TitleFilters` (search field and status, type and collection chips, also used by the add note picker) |
| `watched` | Watched list screen | `domain/BuildWatchedList`; `presentation/WatchedScreen` |
| `collection` | Collections of titles, picker sheet, look (icon, color) | `domain/CreateCollection`; `presentation/CollectionsScreen`, `detail/`, `picker/` |
| `calendar` | Calendar tab and its settings | `domain/BuildCalendarMonth`; `presentation/CalendarScreen`, `CalendarSheets` |
| `search` | TMDB search, add from result, manual entry fallback | `data/TmdbRemoteTitleDataSource`, `TmdbDtos`; `domain/AddTitleFromRemote`; `presentation/SearchScreen` |
| `settings` | Profile and Settings screens, DataStore-backed app settings, series settings | `domain/AppSettings`; `data/DataStoreSettingsRepository`; `presentation/SettingsScreen`, `AppViewModel` |
| `backup` | `.filmorybkp` zip export and import | `data/ZipBackupRepository`, `StoredZipWriter`; `presentation/BackupSection` |

On hold and dropped lists are filters of the library screen, not separate features. Wishlist is a flag on a title.

## Where to look for a change

- UI of one screen: `feature/<name>/presentation/`.
- A business rule (status, watching state, series progress): the matching `domain/` use case, never the UI.
- A new column or table: `core/database/entity` + `dao` + `AppDatabase` (bump the version, add a migration) + the feature `data/*Mappers.kt`, and `DatabaseSnapshot` plus `ZipBackupRepository` so backup keeps working.
- A shared widget: `core/presentation/designsystem/components`.
- A new setting: `settings/domain/AppSettings`, `DataStoreSettingsRepository`, `SettingsScreen`.
