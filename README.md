# Baseball softball lineups keeper

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ae373afec7fe4dacaae97a2c03cc0013)](https://www.codacy.com/manual/kaygenzo/EasyLineUp?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=kaygenzo/EasyLineUp&amp;utm_campaign=Badge_Grade) [![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/ae373afec7fe4dacaae97a2c03cc0013)](https://app.codacy.com/gh/kaygenzo/EasyLineUp/coverage) [![CircleCI](https://dl.circleci.com/status-badge/img/gh/kaygenzo/EasyLineUp/tree/develop.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/kaygenzo/EasyLineUp/tree/develop)

## Description and purpose

## Architecture

Three Gradle modules, dependencies pointing inward (classic ports-and-adapters /
Clean Architecture split):

```
        depends on                  depends on
  app ───────────────> domain <─────────────── repository
```

- `app` — Android application: Compose presentation, DI composition root,
  the infra ports' concrete implementations.
- `domain` — pure Kotlin/JVM: no Android import at all, use cases + the ports
  both other modules implement.
- `repository` — Android library: Room persistence, implements the domain's
  repository ports.

### `domain` — business rules, framework-agnostic

- Plain `java-library` module (`domain/build.gradle`): no Android SDK dependency at
  all, so every use case is a pure JVM unit test (`domain/src/test`, JUnit +
  Mockito + `kotlinx-coroutines-test`, no Robolectric/instrumentation needed).
- `domain/model/*` — plain data classes (`Lineup`, `Player`, `PlayerWithPosition`,
  `BatterState`, `TeamStrategy`, `FieldPosition`, …), zero logic beyond what a few
  small extension functions add (`isSubstitute()`, `isDpDhOrFlex()`, …).
- `domain/ports/*` — the interfaces `app`/`repository` implement: one
  `*Repository` interface per aggregate (`LineupRepository`,
  `PlayerFieldPositionRepository`, `PlayerRepository`, `TeamRepository`,
  `TournamentRepository`, `PlayerNumberOverlayRepository`, `TilesRepository`),
  plus small infra ports (`Clock`, `DispatcherProvider`, `TransactionRunner`,
  `GeocodingProvider`, `PhoneNumberValidator`, `StringResourcesProvider`,
  `ExportFileWriter`, `ImportFileReader`) for the handful of things a use case
  needs that aren't plain persistence.
- `domain/usecases/*` — ~80 single-purpose classes, one job each, called as
  `operator fun invoke(...)` (e.g. `GetBattersState`, `SaveBattingOrderAndPositions`,
  `AssignPlayerFieldPosition`). Every one is registered as a Koin `single { }` in
  `DomainModule.kt`, wired by hand (`get(), get(), …`), not auto-generated.
- Suspend functions + `Flow` throughout (`DispatcherProvider.io()`/`.main()` for
  the real dispatchers, `TestDispatcherProvider` in tests); multi-step writes run
  in one database transaction through `TransactionRunner`.

### `repository` — persistence, the adapter side of the domain ports

- Android library module, Room (`repository/dao/AppDatabase.kt`, KSP-generated
  DAOs), schemas exported to `repository/schemas/` for migration testing.
- `repository/model/Room*` — Room entities, intentionally kept separate from
  `domain/model/*` (no `@Entity` annotation leaking into the domain layer); each
  adapter maps between the two (`toRoom()`/the reverse, next to each model).
- `repository/adapters/impl/*RepositoryImpl` — one implementation per
  `domain/ports/repository` interface, each wrapping its DAO(s). Registered in
  `RepositoryModule.kt` alongside the `Room.databaseBuilder` setup. The 22 schema
  migrations (1 → 23) live in `Migrations.kt`, a unit test checks the chain has no
  gap, and `repository/androidTest` runs the ones from the exported schemas (v16
  onward) against real device SQLite, not mocks.
- Two product flavors (`prod`, `demo`), orthogonal to `app`'s own flavors below:
  `demo`'s `BuildConfig.usePrefilledDatabase` makes `RepositoryModule` call
  `Room.createFromAsset("demo_database")` — a committed SQLite asset
  (`repository/src/demo/assets/demo_database`) seeding realistic rosters/lineups
  instead of booting empty, with `fallbackToDestructiveMigration(dropAllTables = true)`
  so an old seed schema never blocks a migration.

### `app` — presentation, DI composition root, infra adapters

- One package per feature under `com.telen.easylineup` (`dashboard`, `team`,
  `player`, `lineup` — with `lineup.attack`/`lineup.defense`/`lineup.create`/
  `lineup.edition` sub-packages, `tournaments`, `settings`, `login`, `export`,
  `splashscreen`), plus shared infrastructure: `compose/` (see below),
  `navigation/` (routes and the `NavHost`), `utils/` (`FirebaseAnalyticsUtils`,
  `UserPreferencesStore`, the infra port `*Impl` classes), `application/` (DI
  wiring + the `Application` class).
- `application/App.kt` — starts Koin with `ModuleProvider.modules` (`appModules` +
  `DomainModule.domainModules` + `RepositoryModule.repositoryModules`), initializes
  Crashlytics/Analytics/AppCheck (Play Integrity in release, the debug provider in
  debug builds), and re-roots `Timber` to a Crashlytics-reporting tree in release
  builds. `appModules` (`application/ApplicationModule.kt`) is also where the infra
  ports get their `app`-side implementation (`DispatcherProviderImpl`,
  `GeocodingProviderImpl`, `PhoneNumberValidatorImpl`,
  `StringResourcesProviderImpl`, `ExportFileWriterImpl`, `WallClock`...) and where
  every ViewModel is registered via `viewModelOf(::XViewModel)`.
- **Navigation**: `HomeActivity` hosts one Navigation Compose graph with typed
  `@Serializable` routes (`navigation/Routes.kt`, `navigation/AppNavHost.kt`):
  Dashboard, Roster, Team details, Player details/edit, the Lineup screen,
  Lineup creation, Settings, Tournament list and statistics. `DugoutBottomNav`
  switches between the top-level ones. A few screens are standalone `Activity`s
  outside that graph instead: `SplashScreenActivity` (the real launcher — routes to
  `HomeActivity` or `LoginActivity`, and separately handles `.elu` file-open `VIEW`
  intents), `LoginActivity` (misnamed — there's no login, see `REDESIGN.md` §3),
  `TeamCreationActivity`, `ImportDataActivity`.
- **UI**: everything is Compose. A destination is an `XRoute.kt` composable
  (obtains its ViewModel via `koinViewModel<X>()`, collects `uiState`, owns
  navigation/analytics plumbing) wrapping a stateless `XScreen.kt`. Shared Compose
  infra lives in `app/compose`: `DugoutTheme.kt` (bridges the XML Material3 theme's
  colors into a Compose `ColorScheme` via `MaterialColors.getColor`, plus the
  shared `Typography` scale), `DugoutComponents.kt` (`ScreenHeader`,
  `ConfirmSheet`, `ErrorDialog`, form chrome shared across screens),
  `DugoutPreviews.kt` (the small/large × portrait/landscape multipreview annotation
  every composable carries), `DataTransferSheets.kt` (the Export/Import
  confirm/success sheets).
- **ViewModels**: plain constructor injection, registered with `viewModelOf`, and
  obtained through Koin's own delegates (`koinViewModel<X>()` in a Route,
  `by viewModel<X>()` in the plain Activities). One-shot events go through a
  `Channel<Event>` exposed as `Flow<Event>`. No RxJava anywhere — see `CLAUDE.md`
  for the rules new code follows.
- `BuildConfig` + `applicationIdSuffix` give 3 flavors: `dev` (`matchingFallbacks
  = ['prod']`, i.e. an empty local database, for day-to-day development — see
  the `feedback_use_demo_flavor_for_test_data` memory on why destructive-dialog
  testing shouldn't use this one), `prod` (the real shipped app), `demo`
  (matches `repository`'s own `demo` flavor, prefilled database, used for
  screenshots, testing and on-device verification).
- Firebase: Crashlytics, Analytics and AppCheck are the ones actually wired
  (`App.kt`). Auth/Firestore/Storage and Remote Config had no real call sites
  and were dropped from `app/build.gradle`: Remote Config's only job was
  serving a `maps_api_key` for a static-map thumbnail (a Thunderforest tile
  URL, `GetTournamentMapLink`) that the revamp's tournament list replaced with
  a plain tappable pin + address (`REDESIGN.md` §3) — the pin only needs
  geocoding (`GeocodingProvider`, no key required), so the key, the use case's
  `MapApiKeyNotFoundException`/image-URL branch, and the now-pointless
  `MapInfo.url` field all came out together, not just the Firebase dependency.

### Testing

- `domain/src/test` — pure JUnit + Mockito against the use cases directly, no
  Android dependency, the fastest and most exhaustive layer (every use case has
  a test class; see the `DeletePlayerFieldPositionTests`-style per-`TeamType`/
  per-`TeamStrategy` parameterization for the lineup/field-position rules).
- `repository/src/test` + `repository/src/androidTest` — DAO behavior and the
  real Room migration chain against actual on-device SQLite.
- `app/src/test` — Mockito-based unit tests around `app`-layer classes.
- `app/src/androidTest` — Compose UI tests + Espresso + UiAutomator (for the
  system pickers), against the `demo` flavor's seeded data.

### Screenshot tests

`ScreenPreviewsScreenshotTest` renders every full-screen `@DugoutPreviews` preview
on the JVM (Roborazzi + Robolectric) in light, dark and large landscape, against
the reference images in `app/src/test/screenshots`. Plain unit test runs skip the
rendering, so these run only on demand and not in CI yet:
`./gradlew verifyRoborazziProdDebug` checks them, and
`./gradlew recordRoborazziProdDebug` regenerates them after an intended change.

### Screen gallery

`ScreenGalleryTest` walks through every screen on the device in each combination of
screen size, orientation and day/night theme. It takes several minutes, so it is
skipped unless asked for: `./gradlew connectedDemoDebugAndroidTest -PscreenGallery`.
The screenshots stay on the device until you run
`./gradlew generateScreenGallery`, which writes a single self-contained page with
filters to `app/build/reports/screen-gallery/index.html` and clears them from the
device. With several devices connected, set `ANDROID_SERIAL` to the one to read
screenshots from.

### CI & quality gates

- CircleCI (`.circleci/config.yml`), on the maintained `cimg/android` image pinned
  by digest: `jacocoFullReport` (every module's unit tests plus coverage), the prod
  release build with Android lint, `androidTest` APK assembly, Fastlane-driven
  alpha deploys, Firebase App Distribution. Each job decodes only the secrets it
  uses and keeps its own Gradle cache. Instrumented tests are built but not run in
  CI yet.
- Android lint fails the build on any error or warning (`warningsAsErrors`), in
  `app` and `repository`; only the dependency version checks are disabled.
- Jacoco (`jacoco.gradle`, root `jacocoFullReport` task) reports unit test coverage
  of all three modules in one place. Generated code, DI wiring, `domain`'s pure
  data classes and Compose UI/Activities are excluded, so the number reflects what
  unit tests are responsible for: Compose UI is covered by instrumented tests,
  which this report doesn't include yet. The `unit-tests` job uploads it to
  Codacy, whose grade and coverage badges, with CircleCI's, sit at the top of this
  file and track `develop`.

## Libraries

Versions tracked in `gradle/libs.versions.toml` (version catalog), not pinned
per-module.

- **Language/build** — Kotlin 2.3.20, KSP 2.3.13, AGP 9.1.0, JVM target 17; minSdk
  23, compileSdk 37, targetSdk 36 (see `CLAUDE.md` for what blocks the next
  minSdk).
- **UI** — Jetpack Compose (BOM `2026.09.00`), Material3, Material Components
  1.14.0 for the XML theme `DugoutTheme` reads from, Navigation Compose 2.9.8,
  `Lifecycle`/`ViewModel` 2.11.0, splash screen API 1.2.0.
- **Async** — Kotlin Coroutines 1.11.0, end to end.
- **DI** — Koin 4.2.2 (`koin-android`, `koin-androidx-compose` for
  `koinViewModel()`).
- **Persistence** — Room 2.8.5 (KSP codegen, `repository` module only).
- **Serialization** — Gson 2.15.0 for the `.elu` export files,
  kotlinx-serialization for the typed navigation routes.
- **Backend/observability** — Firebase BOM 34.19.0: Crashlytics, Analytics and
  AppCheck (Play Integrity, debug provider in debug builds).
- **Images** — Coil 3.6.3, a forked color-picker dialog (skydoves, 2.0.6).
- **Logging** — Timber 5.0.1, routed to Crashlytics in release builds.
- **Testing** — JUnit4, Mockito + `mockito-kotlin`, `kotlinx-coroutines-test`,
  Robolectric + Roborazzi for preview screenshots, Compose UI tests + Espresso +
  UiAutomator for instrumented tests, Room's own testing artifact for migrations.
- **Quality/CI** — Android lint, Jacoco (coverage, uploaded to Codacy), Fastlane (CI
  deploys) + Firebase App Distribution 5.2.1.

## Revamp status — "The Dugout Card"

The visual redesign and the Jetpack Compose/coroutines migration are complete. Full
design rationale and the mockup gallery are in [`REDESIGN.md`](REDESIGN.md); this
section tracks what landed and what's left, so it doesn't go stale inside the design
doc.

### Done

- **Theme** — Dugout Card color palette applied to the existing XML Material3 theme
  (dark + light), dashed-border/tabular-numeral/bottom-sheet styling, `DugoutTheme`
  bridging the same tokens into Compose.
- **Every screen in Compose** — zero `Fragment` subclasses and zero `res/layout`
  files remain: Welcome/Login, Dashboard, Settings, Team details and form, Roster,
  Player profile (a swipeable `HorizontalPager`, one `PlayerViewModel` per page) and
  form, Tournament list and statistics, Lineup creation, and the lineup screen
  itself — Attack (drag-to-reorder batting order) and Defense (field view and edit
  mode, DH/DP-Flex toggle and link sheet, substitutes).
- **Navigation** — Jetpack Navigation Compose with typed `@Serializable` routes
  (`navigation/Routes.kt`, `navigation/AppNavHost.kt`); the hamburger drawer is
  replaced by `DugoutBottomNav`. Each destination is an `XRoute` composable wrapping
  a stateless `XScreen`.
- **Shared components** — `ScreenHeader`/`HeaderIconChip` in-content app bar,
  `ConfirmSheet` for every discard/delete confirmation, `ErrorDialog`, unified
  import confirm/success sheets shared by Settings and `ImportDataActivity`,
  `SwapTeamSheet`, `EditLineupSheet`, Compose date pickers.
- **Coroutines and constructor injection everywhere** — RxJava is gone; every
  ViewModel is constructor-injected and registered with `viewModelOf`, with
  `Channel`/`Flow` one-shot events. See `CLAUDE.md` for the rules new code follows.
- Every composable carries `@DugoutPreviews` (small/large × portrait/landscape),
  and screens are checked on an actual small/landscape emulator override, not just
  the static preview.

### Not started yet

- **Accessibility pass** — review the ~30 `contentDescription = null` one by one:
  meaningful icons inside clickable rows get a description or are merged with
  `Modifier.semantics(mergeDescendants = true)`. Give the Defense and Attack
  drag-and-drop an accessible alternative (`customActions` such as move up / move
  down / move to…), and check that `Pill`/`SortPill` touch targets reach 48 dp.
