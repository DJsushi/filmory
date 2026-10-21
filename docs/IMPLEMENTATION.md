# Implementation

Kotlin Multiplatform with Compose Multiplatform, built as a `:shared` KMP module with feature packages, plus a thin `:androidApp` shell.
Ktor for networking.
Koin for dependency injection.
Room (KMP) database.

Clean code principles; since this app will be pretty large, use Philipp Lackner's clean code recommendations and principles (see `CLEAN_CODE.md`).

## Platform and project layout

- **Kotlin Multiplatform from the start**, targets Android and iOS, UI in Compose Multiplatform. Shared code lives in `commonMain`. Android-only code (dynamic color, platform file and image access, TMDB key injection) goes in `androidMain` behind `expect`/`actual` or an interface. iOS targets must be declared and kept compiling in common code, but only Android can be built and tested on this Linux machine.
- **`:shared`** is the KMP library holding all code, with feature packages as `CLEAN_CODE.md` section 4 allows: `dev.gaens.filmory.feature.<name>.{domain,data,presentation}` plus `dev.gaens.filmory.core.*`. **`:androidApp`** is the Android application (applicationId `dev.gaens.filmory`, minSdk 24, targetSdk 37): `MainActivity`, `FilmoryApplication` (starts Koin), manifest, launcher icons and the `TMDB_API_KEY` `BuildConfig` field, passed to `:shared` as a `TmdbConfig` Koin binding. AGP 9 cannot combine the KMP plugin and the application plugin in one module, so this two-module split was chosen. Do not add more modules.
- iOS targets are `iosArm64` and `iosSimulatorArm64` (`sqlite-bundled` has no `iosX64`). There is no Xcode project yet; `MainViewController()` in `iosMain` is the entry point for one.
- Do not ask the user about KMP versus Android-only or module layout. Both are decided.

## Libraries

Use the multiplatform versions of the libraries named in `CLEAN_CODE.md`:

- Room KMP with the bundled SQLite driver.
- Ktor client with the OkHttp engine on Android and the Darwin engine on iOS, plus kotlinx.serialization.
- Koin (multiplatform) and the multiplatform AndroidX ViewModel.
- Navigation Compose (multiplatform) with type-safe `@Serializable` routes.
- DataStore (multiplatform) for settings.
- Coil 3 for images.
- `sh.calvin.reorderable` for drag and drop reordering of the Home layout (Compose Multiplatform, works on iOS).
- Compose Multiplatform resources for strings and drawables. `UiText` wraps a resource key, not an Android `Context`.
- kotlinx-datetime for dates (no `java.time` in common code) and Okio for the backup zip. Additions beyond `CLEAN_CODE.md`: the Compose Multiplatform Gradle plugin, kotlinx-datetime, Okio. Ask before adding anything else.
- Tests: `kotlin.test` in `commonTest`, Turbine, and hand-written fakes. JUnit5 is not used.

## Decisions

- **UI toolkit:** Compose Multiplatform with Material 3 components, the Bookmory-like cream and blue theme by default, and an optional dynamic color setting on Android 12+ (`expect`/`actual`). Keep the animations listed in `FEATURES.md` (look and feel) and respect the reduced-motion setting. Use a Bookmory-style rounded bottom bar with a circular active item, drawn as a custom composable.
- **Metadata source:** TMDB, accessed through Ktor and hidden behind a remote data source interface in the domain layer. Manual entry must work without network. The API key is read from `local.properties` at build time (never committed, `local.properties` is already ignored) and exposed to the code through a generated build config, injected with Koin. TMDB requires attribution in the app (see `FEATURES.md`).
- **Data model** (Room is the single source of truth):
  - `titles`: type (movie or series), name, year, poster and TMDB id, `status`, `is_wishlisted`, watch-later position (nullable), added date.
  - `watchings`: belongs to a title. Start and finish dates, state, rating (0 to 10 half-star steps), review, current season and episode pointer (series).
  - `seasons` and `episodes`: belong to a series title.
  - `episode_checks`: watching, episode, `checked_at`.
  - `notes`: belongs to a watching. Optional episode, optional timestamp in milliseconds, type, text, date, optional image path, favorite flag.
  - `note_comments`: belongs to a note. Text and date.
  - `collections` and `collection_titles`.
  - All records get stable ids so sync can be added later.
- **Status rules:** see "Titles" and "Watchings" in `FEATURES.md`. Implement the title status rule in one domain use case with unit tests: status mirrors the latest watching until a watching has finished, then it stays Watched. Home lists come from title status, plus Continue watching for any title with an in-progress watching.
- **Notes without a watching:** adding a note to a title with no watching creates a watching first.
- **Backup and restore:** export and import the whole database and attached images as a zip with the extension `.filmorybkp`. Include a format version number in the archive.
- **Sync:** not implemented. Keep the repositories behind domain interfaces.
- **Ratings:** stored as half-star steps (integer 0 to 10), shown as 0 to 5 stars.
- **Timestamps in notes:** stored as milliseconds from the start of the movie or episode, shown as h:mm:ss.

## Mockup

The mockup is a Design canvas artifact: https://claude.ai/artifact/4eAcUCNPXCPsExChZ7ZdiK. It has ten screens (Home, Library, Notes, Watched, Movie detail, Series detail, Add note, Edit note, Note detail with comments, Calendar). It is a reference for layout and motion, not production code. Its colors are the intended default palette. Its sample data is made up.
