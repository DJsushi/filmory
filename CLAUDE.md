All the documentation is in /docs. Read it before proceeding.

# Filmory

Android and iOS app (Kotlin Multiplatform), a clone of [Bookmory](https://play.google.com/store/apps/details?id=net.tonysoft.bookmory) (book tracker) for **movies and series**. Local-first: all data lives on the device. Package `dev.gaens.filmory`.

A reference Bookmory build (`.apkm` bundle) is in `apks/`. It is installed on the `emulator-5554` AVD for UI comparison. Do not edit it.

A clickable mockup (ten screens) is in the "Filmory Mockup" artifact: https://claude.ai/artifact/4eAcUCNPXCPsExChZ7ZdiK. Use it as the reference for layout and motion. A local copy of its source is in `docs/mockup/` (open the `.dc.html` files for markup, colors and animations, `m3.css` holds the shared styles). `docs/FEATURES.md` wins where the two disagree.

## Docs

- `docs/FEATURES.md`: product scope and decisions. The source of truth for what to build and what to skip.
- `docs/IMPLEMENTATION.md`: platform, libraries, data model and technical decisions.
- `docs/CLEAN_CODE.md`: architecture and code rules (Philipp Lackner style), with the KMP adaptations at the top. Read in full before writing code.

## Decided (do not ask the user again)

- Kotlin Multiplatform from the start, Compose Multiplatform UI, targets Android and iOS. Only Android can be built and tested on this Linux machine.
- Two Gradle modules: `:shared` (all Kotlin Multiplatform code, organised in feature packages) and `:androidApp` (thin Android shell: `MainActivity`, `Application`, manifest, TMDB key `BuildConfig`). AGP 9 does not allow one module to be both the KMP code and the Android app. No further modules.
- Bookmory-like cream and blue theme by default, optional dynamic color on Android 12+.
- Statuses, watchings, notes, comments, wishlist and watch-later rules are in `docs/FEATURES.md`.

## Tech stack

Kotlin Multiplatform, Compose Multiplatform (Material 3 components), Ktor, Koin, Room KMP. Navigation Compose, DataStore, Coil 3, coroutines + Flow, kotlinx.serialization, kotlinx-datetime, Okio, `sh.calvin.reorderable` (Home drag and drop), Gradle version catalog (`gradle/libs.versions.toml`). Do not add libraries outside this list without asking.

Current state: all four milestones are built and run on the Android emulator: every screen in the mockup, the list screens, Profile and Settings, TMDB search with manual add, and backup and restore. Not built: iOS app shell and iOS actuals for the photo picker and share sheet (stubs), the Xcode project. iOS targets are `iosArm64` and `iosSimulatorArm64` only (`sqlite-bundled` has no `iosX64`). iOS cannot be built on this machine.

## Product scope

Summary only. `docs/FEATURES.md` has the full rules.

- **Titles:** movies and series. Status: Not watched, Watching (series only), Watched, On hold, Dropped. Wishlist and watch later are separate flags. Movies have no progress tracking.
- **Watchings:** a title can be watched several times. Each watching has its own dates, state, rating (0 to 5 stars, half steps), review and notes. All notes of all watchings can be viewed together. Once a title is Watched it stays Watched when a rewatch is dropped.
- **Series progress:** per watching, a current-episode pointer plus per-episode check marks with a date.
- **Notes:** on movies, series and episodes. Types: Scene, Summary, Thought, Question. Optional timestamp. Never required. Editable, with the user's own comments shown on the note detail screen only, never under the note in lists.
- **Also in scope:** collections (titles only, not episodes), calendar with Bookmory's settings, TMDB search with manual entry as fallback, backup and restore as a `.filmorybkp` zip.
- **Sync:** not built, but keep the data layer easy to extend.
- **Out of scope for now:** streaks, statistics, rewind, premium features, purchase/gift/loan records.
- **Not designed yet:** Profile and Settings, the list screens (wishlist, collection, on hold, dropped), and the TMDB search and add flow. Build them in the same style, following Bookmory's equivalents.

## Look and feel

Follow Bookmory's look closely (cream background, solid blue curved header, shadowed cards, underlined section titles, rounded bottom bar with a circular active item), built with Material 3 components. Watch-later slots are squircles, the On hold icon is a bold filled pause. Keep Bookmory's animations (staggered card entrance, growing progress bars, animated nav indicator, pop-in checks and FAB). Respect reduced motion. Light and dark theme.

## Domain notes

- Book to title mapping: "Want to read" is Not watched, "Reading" is Watching, "Finished" is Watched, "Paused" is On hold, "Gave up" is Dropped. Bookmory's heart wishlist is the wishlist flag.
- Series have seasons and episodes. Movies do not. Notes attach to a watching and optionally to an episode.
- Reading sessions, pages and ISBN from Bookmory have no equivalent here. Skip them.
- Bookmory's "Series" (grouping books of one series) is not copied. Collections cover franchise grouping.
- The TMDB API key goes in `local.properties`, never in git.

## Architecture

Clean Architecture with strict direction `presentation -> domain <- data`, MVI-style screens (State / Action / Event / ViewModel / ScreenRoot / Screen / Preview), a typed `Result<D, E : Error>`, Koin bindings to domain interfaces. Full rules in `docs/CLEAN_CODE.md`. Its code examples use `Book`. Apply it with title, watching and note names and the `dev.gaens.filmory` package.

## Git

The user has given standing permission to commit in this repository. Do not wait to be asked.

- Use [Conventional Commits](https://www.conventionalcommits.org/): `type(scope): summary`, imperative mood, lowercase, no trailing period, summary under 72 characters. Types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `chore`, `style`, `perf`. Scope is the feature or layer, for example `feat(notes): add comments to note detail` or `build: convert project to kotlin multiplatform`. Add a body when the reason is not obvious from the summary. Use `!` or a `BREAKING CHANGE:` footer for breaking changes.
- Commit regularly: after every coherent, working unit of work, such as one feature, one screen, one domain use case with its tests, or one build change. Do not batch a whole milestone into one commit, and do not leave work uncommitted at the end of a task. Keep unrelated changes in separate commits.
- Before each commit, the project must build and the relevant tests must pass. Do not commit broken code. If a build cannot be run, say so in the commit body.
- Stage files by name (`git add <paths>`), not `git add -A`, and check `git status` and `git diff --staged` first. Never commit `local.properties`, API keys, keystores, `apks/` or build output.
- Make new commits. Do not amend, rebase, force-push or rewrite history unless the user asks. Never skip hooks (`--no-verify`).
- Do not push unless the user asks. There is no remote yet.
- The first commit should contain the docs, the mockup and the project files as they are now (`chore: add project docs, mockup and template`), before the KMP conversion starts.

## Commands

- Build: `./gradlew :androidApp:assembleDebug`
- Unit tests: `./gradlew :shared:testAndroidHostTest`
- Common-code check (catches JVM-only APIs): `./gradlew :shared:compileCommonMainKotlinMetadata`
- Room tests on a device: `./gradlew :shared:connectedAndroidDeviceTest`
- Sample data (debug builds only, empty database): `adb shell am start -n dev.gaens.filmory/.SeedActivity`
- Install on device: `./gradlew :androidApp:installDebug`
- Fully optimized build (R8 over the whole app), temporarily signed with the debug key until a release key exists, not for publishing: `./gradlew :androidApp:assembleOptimized` or `installOptimized`. Keep rules are in `androidApp/src/main/keepRules/rules.keep`.

Gradle needs network access (outside the Claude sandbox).
