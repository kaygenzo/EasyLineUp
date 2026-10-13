# EasyLineUp — conventions for new work

## UI: Compose only for new code

All new UI code is written in Jetpack Compose, not XML layouts. This does not mean
retrofitting every existing XML screen — screens get reskinned to Compose one at a
time, in their own pass (see `REDESIGN.md` for the broader visual revamp roadmap).
`SettingsFragment`/`SettingsScreen.kt` is the reference pilot: a plain `Fragment`
hosting a single `ComposeView`, no XML layout, no ViewBinding.

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

## No new RxJava — coroutines/Flow only

RxJava3 is still used throughout the app (`BaseFragment`'s `launch(...)`
overloads for `Flowable`/`Maybe`/`Single`/`Completable`/`Subject`, every
ViewModel's event bus, `LoginViewModel`, `DialogFactory`'s task dialogs,
etc). None of that is being migrated proactively — but **no new code
introduces a new Rx usage**, including new call sites into existing
Rx-based classes beyond what already exists (e.g. don't add a new
`BaseFragment.launch(...)` call for a feature that doesn't already need
one). New code uses coroutines/`Flow` end to end: `suspend` functions,
`viewModelScope`/`lifecycleScope`, `Channel`/`Flow` for events,
`repeatOnLifecycle` for collection.

Where new code must call into an existing Rx-based class that is itself
out of scope to touch (e.g. `LoginViewModel` from `SettingsFragment`),
subscribing to its Rx `Subject` directly is the accepted interop
boundary — the rule is about not writing *new* Rx, not about refusing to
talk to code that has not been migrated yet.

## ViewModels: constructor injection + coroutines, no service locator

Every ViewModel in the app today extends `KoinComponent` and pulls its
dependencies via `by inject()` field injection, with zero Koin module
registration for the ViewModel itself (`by viewModels<XViewModel>()` works
because `KoinComponent` resolves its own fields lazily at runtime). Known
anti-pattern: it hides the real dependency graph and can't be constructor-tested
without spinning up Koin.

`SettingsViewModel` is the reference modernization:
- Plain constructor injection — no `KoinComponent`, no `by inject()`.
- Registered explicitly: `viewModelOf(::SettingsViewModel)` in
  `ApplicationModule.kt`'s `appModules`.
- Obtained in the Fragment via Koin's `by viewModel()` delegate
  (`org.koin.androidx.viewmodel.ext.android.viewModel`), not
  `by viewModels<X>()`.
- One-shot events go through a `Channel<Event>` exposed as `Flow<Event>`
  (`receiveAsFlow()`), collected with
  `viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(STARTED) { ... } }`
  — not an RxJava `PublishSubject`/`CompositeDisposable`.

This pattern is the template for the *next* ViewModel someone touches or
reskins — it is **not** being retrofitted onto the rest of the app proactively.
Screens still on the old pattern (every other ViewModel) keep working as-is
until their own pass.

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

## Every Compose screen gets previews: small/large × portrait/landscape

Any composable that renders a full screen (not a small reusable piece like a
single row or icon) carries `@DugoutPreviews`
(`app/src/main/java/com/telen/easylineup/compose/DugoutPreviews.kt`), a
multipreview annotation covering small and large screen, portrait and
landscape — four `@Preview` device specs in one annotation, so one
`@DugoutPreviews` call is equivalent to writing all four by hand.

Pattern (see `SettingsScreenPreview`/`WelcomeScreenPreview` at the bottom of
`SettingsScreen.kt`/`WelcomeScreen.kt`): a private `@Composable *Preview()`
function wraps the real screen in `DugoutTheme`, passing a representative
state and no-op lambdas for every callback.

This needs `androidx.compose.ui:ui-tooling` as a `debugImplementation` (added
back once the BOM was pinned to 2025.09.01 — the minSdk-23 conflict that made
us drop it originally was specific to the newer BOM, re-tested and confirmed
gone on this one).
