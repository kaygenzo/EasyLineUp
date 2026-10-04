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
