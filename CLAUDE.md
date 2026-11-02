# EasyLineUp — conventions for new work

## UI: Compose only

The Fragment/XML-layout migration is complete: every screen is Jetpack Compose,
zero `Fragment` subclasses and zero `res/layout` XML files remain. Navigation is
Jetpack Navigation Compose with typed `@Serializable` routes
(`navigation/Routes.kt`, `navigation/AppNavHost.kt`) — a destination is an
`XRoute.kt` composable (obtains its ViewModel via `koinViewModel<X>()`, collects
`uiState` via `collectAsStateWithLifecycle()`, owns navigation/analytics
plumbing) wrapping a stateless `XScreen.kt` composable. See `REDESIGN.md` for
the design rationale behind the visual system.

- Bridge the existing XML Material3 theme into Compose with `DugoutTheme`
  (`app/src/main/java/com/telen/easylineup/compose/DugoutTheme.kt`) instead of
  hand-rolling a second set of color constants — it reads the same `themes.xml` /
  `material-colors.xml` tokens via `MaterialColors.getColor`, so both stay in sync.
- `androidx.compose:compose-bom` is pinned to `2025.09.01` (compose-ui 1.9.x).
  Newer BOMs (compose-ui 1.10+) raise the library's minSdk to 23, which is above
  this app's minSdk 21 — bumping the BOM past this line means deciding to drop
  API 21/22 support first, not a side effect of a routine dependency bump.
- Prefer Compose's own components (`AlertDialog`, etc.) over native
  `AlertDialog.Builder`/dialogs for anything new. Existing non-Compose dialogs
  (`DialogFactory`, the skydoves `ColorPickerDialog`) are reused as-is from a
  Compose row's `onClick` where no Compose equivalent exists yet — that's a
  normal interop boundary, not a regression.

## No RxJava — coroutines/Flow only

RxJava3 has been fully migrated away: zero `Disposable`/`CompositeDisposable`/
`PublishSubject`/`Flowable`/`Completable` usage remains anywhere in the repo.
Every event bus is a `Channel<Event>` exposed as `Flow<Event>`
(`receiveAsFlow()`), collected with
`lifecycleScope.launch { repeatOnLifecycle(STARTED) { ... } }` in the handful
of plain Activities outside the Compose Navigation graph (`BaseImportActivity`
and its subclasses), or `flowWithLifecycle(lifecycle, STARTED)` in Compose.
All platform I/O that used to go through Rx (`LineupViewModel`'s screenshot
export, `LoginViewModel`'s import flow) is `suspend` end to end. New code
must not introduce any Rx usage — there is no longer an existing Rx surface
to interop with, so this is a hard rule, not an exemption boundary.

## ViewModels: constructor injection + coroutines, no service locator

Every ViewModel is plain constructor injection — no `KoinComponent`, no
`by inject()` field injection anywhere in the app (that service-locator
pattern, where `by viewModels<X>()` worked only because `KoinComponent`
resolved its own fields lazily at runtime, has been fully retired; it hid the
real dependency graph and couldn't be constructor-tested without spinning up
Koin). Each ViewModel is registered explicitly via `viewModelOf(::XViewModel)`
in `ApplicationModule.kt`'s `appModules`, and obtained through Koin's own
delegates — `koinViewModel<X>()` in an `XRoute.kt` composable
(`org.koin.androidx.compose.koinViewModel`), or `by viewModel<X>()` in the
handful of plain Activities outside the Compose Navigation graph
(`org.koin.androidx.viewmodel.ext.android.viewModel`) — never AndroidX's own
`by viewModels<X>()`. One-shot events go through a
`Channel<Event>` exposed as `Flow<Event>` (`receiveAsFlow()`), collected with
`flowWithLifecycle(lifecycle, STARTED)` in Compose — not an RxJava
`PublishSubject`/`CompositeDisposable`.

This is a hard rule for any new ViewModel, not just a template to follow
eventually — there is no longer an old-pattern ViewModel left to use as an
excuse.

## Business logic: domain only, never ViewModel or Compose

Any calculation, validation, sort/filter, bitwise operation, or state
transition that a product person could describe as a rule independent of a
specific screen's rendering belongs in `domain` — as a `domain/usecases`
class (`operator fun invoke`, registered in `DomainModule.kt`) or a pure
`domain/model` extension function (e.g. `FieldPosition.matches()`,
`Tournament.hasDateWindow()`). Platform I/O a domain rule needs to trigger
goes behind a `domain/ports` interface, implemented in `app/utils` and
registered in `ApplicationModule.kt` (see `ExportFileWriter`); platform I/O
with no domain-side caller (e.g. writing a shareable screenshot) still goes
behind an injected `app/utils` interface, just not a `domain/ports` one (see
`LineupShareImageWriter`) — a ViewModel never calls `FileOutputStream`/
`FileProvider`/`ContentResolver` directly, **even inside a method already
accepted as legacy Rx** (the Rx exemption covers not migrating off Rx, not
skipping a check of what the method's body actually does). ViewModels and
Compose Screens/Routes/Sheets only shape state, map use-case `Result`s into
`UiState`, handle navigation/analytics/platform plumbing, and localize
strings.

This was found to need real enforcement: six rounds of business-logic audits
in one session each turned up a few more cases reimplemented in the UI layer
instead of reused from domain (including one real bug from a divergent
duplicate), and a 7th pass — prompted by a direct question, not the
checklist — found `LineupViewModel` writing files straight to disk inside an
Rx method that every prior audit had skipped specifically because it was
Rx-wrapped. See the `business-logic-boundary` skill for the full checklist
(what counts, what doesn't, where new logic/I/O goes, and the audit
procedure) before writing or reviewing ViewModel/Compose code.

## Code style: AOSP, 100-column hard limit

All code (Kotlin, Java, XML, Gradle) follows the official AOSP style. Concretely:
100 characters per line, **including comments** — a standalone URL is the only
accepted exception, since wrapping it would make it unusable. This is enforced
for Kotlin via `LONG_LINE` in `Formatting.yml` (diktat, run through `spotless`),
set to `100` (was `120`).

This applies to all new code going forward, and was applied retroactively to
every line added across the Settings and Welcome/Login Compose pilots. It does
not apply to prose documentation (`REDESIGN.md`) or to a TOML inline table that
must stay on one line to parse (a couple of entries in `libs.versions.toml`
slightly exceed 100 for this reason — a format constraint, not an exception to
the rule).

## Every composable gets previews: small/large × portrait/landscape

Every single `@Composable` function carries `@DugoutPreviews`
(`app/src/main/java/com/telen/easylineup/compose/DugoutPreviews.kt`), a
multipreview annotation covering small and large screen, portrait and
landscape — four `@Preview` device specs in one annotation, so one
`@DugoutPreviews` call is equivalent to writing all four by hand. This is not
limited to full screens: bottom sheets/dialogs (`ConfirmSheet`,
`EditLineupSheet`, `DpFlexLinkSheet`, ...) and small reusable pieces (a single
row, chip, marker, icon button, badge) all get their own preview too — an
earlier version of this rule scoped previews to full screens only; it was
broadened deliberately to cover every composable, no exceptions.

Pattern (see `SettingsScreenPreview`/`WelcomeScreenPreview` at the bottom of
`SettingsScreen.kt`/`WelcomeScreen.kt`): a private `@Composable *Preview()`
function wraps the real composable in `DugoutTheme`, passing representative
state and a no-op lambda matching the exact arity for every callback. The
preview lives in the same file as the composable it previews, right after it
(or grouped at the bottom if that reads better) — a `private` composable's
preview is necessarily `private` too and can't live anywhere else. A
composable with two meaningfully different, cheap-to-show states (editable vs
fixed, populated vs empty) gets two preview functions instead of one; default
to one otherwise. For the rare composable that returns a non-UI value (e.g. a
`String`, a `FontFamily?`) rather than rendering anything, the preview still
calls it inside `DugoutTheme` — wrap the result in a `Text` so the preview
shows something, matching the already-established treatment of
`PlayerSide.shortLabel()`/`arrayLabel`/`mostPlayedPositionLabel` — rather than
skipping it.

This needs `androidx.compose.ui:ui-tooling` as a `debugImplementation` (added
back once the BOM was pinned to 2025.09.01 — the minSdk-23 conflict that made
us drop it originally was specific to the newer BOM, re-tested and confirmed
gone on this one).

## Every Compose screen must be resilient on small screens

A fixed, non-scrolling layout that only fits by coincidence on the device it
was built against is not acceptable — a screen must actually be checked
against a small/landscape config, not just assumed to work because the
`@DugoutPreviews` small-screen variants render without an obvious visual
glitch in a static preview.

`WelcomeScreen` originally used a non-scrolling `Column` with a weighted
spacer to pin two buttons to the bottom; checked against a small-screen
emulator override (`adb shell wm size 360x640` / `640x360` for landscape, then
`adb shell wm size reset`), the buttons and part of the content were entirely
off-screen and unreachable in landscape. Fixed by wrapping the screen in
`verticalScroll(rememberScrollState())` (same pattern `SettingsScreen` already
used) and dropping the weighted spacer — `Modifier.weight` does not work
inside a scrollable, unbounded-height `Column`.

So for every new screen: wrap the root layout in `verticalScroll` unless it is
already a `LazyColumn`/`LazyRow` (which scrolls natively), never rely on
`Modifier.weight` to pin content to an edge inside a scrollable container, and
actually verify on a small/landscape screen size before considering the
screen done — the four `@DugoutPreviews` variants are the visual check, a
real small-screen run (device override or the preview's interaction mode) is
the functional one.
