# Baseball softball lineups keeper

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ae373afec7fe4dacaae97a2c03cc0013)](https://www.codacy.com/manual/kaygenzo/EasyLineUp?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=kaygenzo/EasyLineUp&amp;utm_campaign=Badge_Grade) [![pipeline status](https://gitlab.com/telen.lannion/EasyLineUp/badges/develop/pipeline.svg)](https://gitlab.com/telen.lannion/EasyLineUp/-/commits/develop)

## Description and purpose

## Architecture

## Libraries

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
  chart with per-bar counts.
- **Player form** — rewritten in Compose: single asterisk on the only required
  field, discard-confirm reskinned as a sheet.
- **Shared app bar** — every refactored screen above got the mockup's own in-content
  header (`ScreenHeader`/`HeaderIconChip`/`HeaderActionIconChip` in
  `DugoutComponents.kt`), duplicating the real toolbar's back/edit/delete actions
  rather than waiting on the drawer retirement.
- Every touched screen carries `@DugoutPreviews` (small/large × portrait/landscape)
  and has been checked on an actual small/landscape emulator override, not just the
  static preview.

### Not started yet

- **Tournaments list** — still XML/View, untouched.
- **Create lineup / edit lineup metadata sheets** — still XML/View.
- **Defense field (view + edit mode)** — the big one: `DefenseFragmentFixed` /
  `DefenseFragmentEditable` stay as two separate View implementations; this is
  "step 2" of the sequencing plan in `REDESIGN.md` §1, the single screen most
  worth the Compose investment, not started.
- **Batting order (view + edit mode)**, **add/remove/replace substitute**,
  **DH/DP-Flex toggle**, **delete lineup**, **share lineup** — all still XML/View,
  per the real behavior documented in `REDESIGN.md` §3.
- **Import/export data screens** — still XML/View; the real `ImportResult` counts
  the mockup surfaces are not yet wired into the UI.
- **Team form's `FormAppBar`** still has its own local header implementation,
  not yet refactored to call the shared `ScreenHeader` (it predates that
  component; functionally identical, just not deduplicated).
- **Drawer retirement** — the system toolbar/hamburger drawer is still the real
  navigation; the new in-content headers were deliberately built to coexist with
  it first. Switching to bottom nav (already in every mockup) is its own,
  later pass per `REDESIGN.md` §1.
- **Rx → coroutines** — per screen, as each one is reskinned to Compose; most of
  the app's ViewModels are still on the old `KoinComponent`/RxJava pattern.
