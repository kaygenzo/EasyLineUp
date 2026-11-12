# Baseball softball lineups keeper

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ae373afec7fe4dacaae97a2c03cc0013)](https://www.codacy.com/manual/kaygenzo/EasyLineUp?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=kaygenzo/EasyLineUp&amp;utm_campaign=Badge_Grade) [![pipeline status](https://gitlab.com/telen.lannion/EasyLineUp/badges/develop/pipeline.svg)](https://gitlab.com/telen.lannion/EasyLineUp/-/commits/develop)

## Description and purpose

## Architecture

Three Gradle modules, dependencies pointing inward (classic ports-and-adapters /
Clean Architecture split):

```
        depends on                  depends on
  app ───────────────> domain <─────────────── repository
```

- `app` — Android application: Compose/View presentation, DI composition root,
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
  `PlayerFieldPositionRepository`, `TeamRepository`, `TournamentRepository`,
  `PlayerNumberOverlayRepository`, `TilesRepository`), plus small infra ports
  (`DispatcherProvider`, `GeocodingProvider`, `PhoneNumberValidator`,
  `StringResourcesProvider`) for the handful of things a use case needs that
  aren't persistence.
- `domain/usecases/*` — ~60 single-purpose classes, one job each, called as
  `operator fun invoke(...)` (e.g. `GetBattersState`, `SaveBattingOrderAndPositions`,
  `AssignPlayerFieldPosition`). Every one is registered as a Koin `single { }` in
  `DomainModule.kt`, wired by hand (`get(), get(), …`), not auto-generated.
- Suspend functions + `Flow` throughout (`DispatcherProvider.io()`/`.main()` for
  the real dispatchers, `TestDispatcherProvider` in tests) — the domain layer has
  no RxJava dependency; RxJava only exists above it, in parts of `app` not yet
  migrated (see Revamp status).

### `repository` — persistence, the adapter side of the domain ports

- Android library module, Room (`repository/dao/AppDatabase.kt`, KSP-generated
  DAOs), schemas exported to `repository/schemas/` for migration testing.
- `repository/model/Room*` — Room entities, intentionally kept separate from
  `domain/model/*` (no `@Entity` annotation leaking into the domain layer); each
  adapter maps between the two (`toRoom()`/the reverse, next to each model).
- `repository/adapters/impl/*RepositoryImpl` — one implementation per
  `domain/ports/repository` interface, each wrapping its DAO(s). Registered in
  `RepositoryModule.kt` alongside the `Room.databaseBuilder` setup and all 18
  schema migrations (`migration1To2()` … `migration17To18()`); `repository/androidTest`
  round-trips a selection of these against real device SQLite, not mocks.
- Two product flavors (`prod`, `demo`), orthogonal to `app`'s own flavors below:
  `demo`'s `BuildConfig.usePrefilledDatabase` makes `RepositoryModule` call
  `Room.createFromAsset("demo_database")` — a committed SQLite asset
  (`repository/src/demo/assets/demo_database`) seeding realistic rosters/lineups
  instead of booting empty, with `fallbackToDestructiveMigration()` so an old
  seed schema never blocks a migration.

### `app` — presentation, DI composition root, infra adapters

- One package per feature under `com.telen.easylineup` (`dashboard`, `team`,
  `player`, `lineup` — with `lineup.attack`/`lineup.defense`/`lineup.create`/
  `lineup.edition` sub-packages, `tournaments`, `settings`, `login`, `export`,
  `splashscreen`), plus shared infrastructure: `compose/` (see below),
  `views/` (legacy custom `View`s), `utils/` (`DialogFactory`,
  `FirebaseAnalyticsUtils`, `SharedPreferencesHelper`, the infra port `*Impl`
  classes), `application/` (DI wiring + the `Application` class).
- `application/App.kt` — `MultiDexApplication`; starts Koin with
  `ModuleProvider.modules` (`appModules` + `DomainModule.domainModules` +
  `RepositoryModule.repositoryModules`), initializes Crashlytics/Analytics/
  AppCheck, re-roots `Timber` to a Crashlytics-reporting tree in release
  builds. `appModules` (`application/ApplicationModule.kt`) is also where the
  infra ports get their `app`-side implementation (`DispatcherProviderImpl`,
  `GeocodingProviderImpl`, `PhoneNumberValidatorImpl`,
  `StringResourcesProviderImpl`) and where each constructor-injected ViewModel
  is registered via `viewModelOf(::XViewModel)`.
- **Navigation**: single `HomeActivity` hosting one Navigation Component graph
  (`res/navigation/app_navigation.xml`, 11 destinations: Dashboard, Roster,
  Team details, Player details/edit, the Lineup screen in both its Fixed/Editable
  variants, Lineup creation, Settings, Tournament list, the statistics table) +
  `DugoutBottomNav` (Compose) switching between the four top-level ones. A few
  screens are standalone `Activity`s outside that graph instead:
  `SplashScreenActivity` (the real launcher — routes to `HomeActivity` or
  `LoginActivity`, and separately handles `.elu` file-open `VIEW` intents),
  `LoginActivity` (misnamed — there's no login, see `REDESIGN.md` §3),
  `TeamCreationActivity`, `ImportDataActivity`. The Lineup screen's own
  Defense/Attack tabs are a second, nested navigation layer: a `ViewPager2` +
  `LineupPagerAdapter` (`FragmentStateAdapter`) inside `LineupFragment`, which is
  why Attack could move to Compose on its own while Defense stays a plain View
  on the adjacent page — `FragmentStateAdapter` doesn't care what a page
  `Fragment` inflates.
- **UI, mid-migration**: new/reskinned screens are a thin `Fragment` hosting one
  `ComposeView` (`SettingsFragment` is the reference pilot, see `CLAUDE.md`);
  screens not yet touched keep their XML layout + `ViewBinding`. Shared Compose
  infra lives in `app/compose`: `DugoutTheme.kt` (bridges the existing XML
  Material3 theme's colors into a Compose `ColorScheme` via
  `MaterialColors.getColor`, plus the shared `Typography` scale),
  `DugoutComponents.kt` (`ScreenHeader`, `ConfirmSheet`, form chrome shared across
  screens), `DugoutPreviews.kt` (the small/large × portrait/landscape multipreview
  annotation every full-screen composable carries), `DataTransferSheets.kt`
  (the Export/Import confirm/success sheets).
- **ViewModels**: the established (legacy) pattern is `class XViewModel :
  ViewModel(), KoinComponent` pulling dependencies via `by inject()` field
  injection, with zero Koin registration for the ViewModel itself —
  `by viewModels<XViewModel>()` just works because `KoinComponent` resolves its
  own fields lazily. New/reskinned screens instead use plain constructor
  injection, explicit `viewModelOf(::XViewModel)` registration, and Koin's
  `by viewModel()` delegate — see the Revamp status section above for exactly
  which ViewModels have made the jump.
- **Rx interop**: `BaseFragment`'s `launch(...)` overloads
  (`Flowable`/`Maybe`/`Single`/`Completable`/`Subject`, each wiring a
  `CompositeDisposable` cleared in `onDestroy()`) are the accepted boundary for
  call sites into ViewModels that still expose RxJava3 — new code never adds a
  *new* such call site, per `CLAUDE.md`.
- `BuildConfig` + `applicationIdSuffix` give 3 flavors: `dev` (`matchingFallbacks
  = ['prod']`, i.e. an empty local database, for day-to-day development — see
  the `feedback_use_demo_flavor_for_test_data` memory on why destructive-dialog
  testing shouldn't use this one), `prod` (the real shipped app), `demo`
  (matches `repository`'s own `demo` flavor, prefilled database, used for
  screenshots/testing/this revamp's own on-device verification).
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
- `app/src/androidTest` — Espresso + Barista + UiAutomator +
  `cloudtestingscreenshotter`/Screengrab, run with a custom
  `EasylineupAndroidTestRunner`, against the `demo` flavor's seeded data.

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

- CircleCI (`.circleci/config.yml`): `jacocoFullReport` (every module's unit tests
  plus coverage), `androidTest` APK assembly, Fastlane-driven alpha deploys,
  Firebase App Distribution. Instrumented tests are built but not run in CI yet.
- Jacoco (`jacoco.gradle`, root `jacocoFullReport` task) reports unit test coverage
  of all three modules in one place. Generated code, DI wiring, `domain`'s pure
  data classes and Compose UI/Activities are excluded, so the number reflects what
  unit tests are responsible for: Compose UI is covered by instrumented tests,
  which this report doesn't include yet. Codacy and the GitLab pipeline badges at
  the top of this file track both on `develop`.

## Libraries

Versions tracked in `gradle/libs.versions.toml` (version catalog), not pinned
per-module.

- **Language/build** — Kotlin 2.3.20, KSP 2.3.2, AGP 9.1.0, JVM target 17.
- **UI** — Jetpack Compose (BOM `2025.09.01`, deliberately pinned — see
  `CLAUDE.md` for why: newer BOMs raise `compose-ui`'s `minSdk` above this app's
  21), Material Components 1.13.0 for the surviving XML screens, Navigation
  Component 2.9.7, `ViewPager2`/`RecyclerView` 1.4.0, `ConstraintLayout` 2.2.1,
  `Lifecycle`/`ViewModel` 2.9.1, splash screen API 1.0.1.
- **Async** — Kotlin Coroutines 1.9.0 (new code, end to end), RxJava3 3.1.12 +
  RxAndroid 3.0.2 (legacy ViewModels/`BaseFragment`, no new usage — see
  `CLAUDE.md`).
- **DI** — Koin 4.1.0 (`koin-core`, `koin-android`,
  `koin-androidx-viewmodel`, `koin-androidx-compose` for `koinViewModel(key = …)`).
- **Persistence** — Room 2.7.1 (KSP codegen, `repository` module only).
- **Backend/observability** — Firebase BOM 32.2.2: Crashlytics, Analytics and
  AppCheck (Play Integrity). Auth/Firestore/Storage/Remote Config were unused
  dependencies, removed (see Architecture above).
- **Images** — Picasso 2.8, a rounded `ImageView` helper, a forked color-picker
  dialog (skydoves, 2.0.6).
- **Other UI helpers (legacy screens)** — TapTargetView 1.13.3 (one-time
  feature callouts — being dropped in favor of simpler in-Compose affordances
  as screens migrate, not added to new ones), Dexter 6.2.3 (runtime
  permissions), a multi-scroll table view (statistics screen).
- **Logging** — Timber 5.0.1, routed to Crashlytics in release builds.
- **Testing** — JUnit4, Mockito + `mockito-kotlin`, `kotlinx-coroutines-test`,
  Espresso + Barista 4.3.0 + UiAutomator + Screengrab for instrumented UI tests,
  Room's own testing artifact for migrations.
- **Quality/CI** — Jacoco (coverage), OWASP dependency-check 6.5.3, Fastlane (CI
  deploys) + Firebase App Distribution 5.2.1.

## Revamp status — "The Dugout Card"

The app is mid-way through a visual redesign + Jetpack Compose/coroutines migration,
done screen by screen on the `revamp` branch. Full design rationale and the mockup
gallery are in [`REDESIGN.md`](REDESIGN.md); this section just tracks what's actually
landed vs. what's left, so it doesn't go stale inside the design doc.

### Done

- **Theme** — Dugout Card color palette applied to the existing XML Material3 theme
  (dark + light), dashed-border/tabular-numeral/bottom-sheet styling, `DugoutTheme`
  bridging the same tokens into Compose.
- **Welcome / Login** — rewritten in Compose.
- **Settings** — rewritten in Compose (plain `Fragment` + `ComposeView` pilot),
  `SettingsViewModel` modernized to constructor injection + `Flow` events (the
  reference pattern for future ViewModels).
- **Dashboard** — rewritten in Compose: one "what's next" hero card, 2×2 mini-grid,
  number-lookup row, team-switch header (replaces the drawer's team switcher) +
  its "Switch team" bottom sheet.
- **Team details** — rewritten in Compose, promoted into Settings, roster-by-gender
  stat card added.
- **Team create/edit form** — rewritten in Compose.
- **Roster** — rewritten in Compose: photo tiles with jersey-number badge or dashed
  placeholder, name/number sort pills, "Message the team" bottom sheet (SMS/email/
  share-as-text, with before-you-send contact-coverage counts).
- **Player profile** — rewritten in Compose: grouped info cells, position-usage bar
  chart with per-bar counts; `PlayersDetailsContainerFragment` later replaced by a
  swipeable `HorizontalPager` carousel (one Fragment hosting every player, each
  page its own `PlayerViewModel` via `koinViewModel(key = playerId)`).
- **Player form** — rewritten in Compose: single asterisk on the only required
  field, discard-confirm reskinned as a sheet.
- **Shared app bar** — every refactored screen above got the mockup's own in-content
  header (`ScreenHeader`/`HeaderIconChip`/`HeaderActionIconChip` in
  `DugoutComponents.kt`), duplicating the real toolbar's back/edit/delete actions
  rather than waiting on the drawer retirement.
- **Confirm/destructive dialogs** — shared `ConfirmSheet` composable; discard-changes
  (team form, player form), delete (team details, player, settings "delete all
  data"), and the dashboard's number-lookup dialog all reskinned as Compose sheets
  instead of native `AlertDialog`s.
- **Navigation** — hamburger drawer retired; `DugoutBottomNav` (shared composable)
  is the real bottom navigation now, wired from `HomeActivity`.
- **Tournament list** — rewritten in Compose: ticket-stub date chip, search
  (matches lineup names too), inline-expand lineup rows.
- **Lineup creation** — new Compose screen (`LineupCreationScreen.kt`) plus the
  roster/tournament picker sheets (search, per-player shirt-number override,
  inline "create tournament").
- **Lineup edition (name/tournament/roster)** — collapsed into one Compose bottom
  sheet (`EditLineupSheet`), reachable from either the tournament list's row-level
  pencil or the lineup screen's own app-bar pencil; `LineupEditionViewModel`
  modernized to constructor injection + `Channel` events.
- **Export/Import** — `ImportDataActivity` and the Settings export/import entry
  points rewritten in Compose: one unified confirm sheet (file preview + the real
  `ImportResult` counts + overwrite toggle) and success sheet shared across both
  entry points; `LoginViewModel`'s import flow split into peek/confirm steps.
- **Team switch sheet** — `SwapTeamFragment` (a `BottomSheetDialogFragment`)
  replaced by `SwapTeamSheet`, a Compose `ModalBottomSheet` rendered from
  `HomeActivity`.
- **Batting order (Attack tab)** — rewritten in Compose (`AttackScreen.kt`), view
  and edit mode both: hand-rolled drag-to-reorder, the classic scorebook position
  number next to its short name (e.g. "8 · CF"), an "Editing order" badge, and an
  on-demand (i) column legend in place of a persistent header. Deliberately scoped
  to Attack only — `AttackFragment` hosts the Compose content inside the same
  `ViewPager2`/`LineupPagerAdapter` that still shows `DefenseFragmentFixed`/
  `DefenseFragmentEditable` as plain Views on the other tab; the shared
  `LineupViewModel`, the tab bar, the toolbar menu (Share/Edit/Delete/"Use
  DP/Flex"), and the Save/Cancel bar are all still native Views, since they're
  shared with Defense and move to Compose when Defense does.
- Building the Attack screen surfaced and fixed three pre-existing bugs, all
  predating this branch: `PlayerWithPosition.reset()` left a disabled DP/Flex
  player in an orphan state (neither on the field nor counted as a substitute)
  instead of putting them back on the bench; `LineupViewModel`'s shared
  `_lineup`/`_players` flows were cold instead of `shareIn`-shared, so toggling
  "Use DP/Flex" from one tab could silently revert when read from the other; and
  `SaveBattingOrderAndPositions` never wrote a newly-inserted row's id back onto
  the in-memory player, so a repeat save (e.g. a Save double-tap) could insert a
  duplicate `playerFieldPosition` row. `GetBattersState` also got a defensive
  de-dupe so a similar data slip degrades instead of crashing the Compose list.
- Every touched screen carries `@DugoutPreviews` (small/large × portrait/landscape)
  and has been checked on an actual small/landscape emulator override, not just the
  static preview.

### Not started yet

- **Defense field (view + edit mode)** — the big one: `DefenseFragmentFixed` /
  `DefenseFragmentEditable` stay as two separate View implementations; this is
  "step 2" of the sequencing plan in `REDESIGN.md` §1, the single screen most
  worth the Compose investment, not started.
- **DH/DP-Flex toggle, add/remove/replace substitute, delete lineup, share
  lineup** — all still native Views/dialogs, reached through the shared
  `LineupFragment`'s toolbar `Menu` and `ListAvailablePlayersBottomSheet`; per
  `REDESIGN.md` §3, these move to Compose alongside Defense.
- **Team form's `FormAppBar`** still has its own local header implementation,
  not yet refactored to call the shared `ScreenHeader` (it predates that
  component; functionally identical, just not deduplicated).
- **Rx → coroutines / constructor injection**, per screen. Modernized so far:
  `SettingsViewModel`, `SetupViewModel` (Welcome/Login), `TeamViewModel`,
  `PlayerViewModel`, `DashboardViewModel`, `LineupCreationViewModel`,
  `LineupEditionViewModel`. Still on the old `KoinComponent` + `by inject()`
  pattern: `HomeViewModel`, `LoginViewModel` (kept as the accepted interop
  boundary, see `CLAUDE.md`), `TournamentStatisticsViewModel`, the tournament
  list's own `LineupViewModel` (already Flow-based, not yet constructor-injected),
  and the big `lineup.LineupViewModel` shared by Attack/Defense — deliberately
  left as-is until Defense gets its own Compose pass, so it isn't modernized twice.
- **Fragment → Compose Navigation** — replacing `HomeActivity`'s `NavHostFragment`/
  `app_navigation.xml` with a real Compose `NavHost` is deferred until every
  screen above (Defense, the stats table) is Compose; until then, new/reskinned
  screens keep using the Fragment-hosts-`ComposeView` pattern so a Compose and a
  View-based `NavController` never have to share one back stack.
