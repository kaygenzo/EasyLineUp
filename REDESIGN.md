# EasyLineUp Redesign — "The Dugout Card"

This document recaps the UI/UX redesign work done in the design-review sessions, before implementation ("the revamp") starts. It is a companion to the app's main `README.md`, not a replacement for it.

**Deliverables** (Claude Code artifacts, built and iterated across the design-review conversation):
- Static mockup gallery, 34 screens, captioned with rationale: https://claude.ai/code/artifact/955f9f33-027e-49b8-969d-3930ffe35f2a
- Interactive click-through prototype, Pixel 9a viewport (412×923dp): https://claude.ai/code/artifact/31d03826-e114-4db9-b62e-d6e8ee02c9fd
- Shelved theme exploration (not part of the plan, parked — see §6): https://claude.ai/code/artifact/94db1886-dca7-46a6-bbe1-20e8c16a2166

These are private artifact links tied to the Claude account that created them — share them from the artifact's own share menu if someone outside that account needs access.

## 1. Why this exists

The long-term goal is to migrate `app` to Jetpack Compose and drop RxJava — a large, multi-version effort. Doing a visual redesign at the same time would conflate two independent risks. The decision made early on: **ship a reskin on the current View/XML system first**, prove the direction with a coach, and let the Compose/Rx migration proceed screen-by-screen behind it at whatever pace the `domain`/`repository` hardening work (a separate, already-completed effort — see git history) leaves room for.

Recommended sequencing (unchanged from the gallery's closing footnote):
1. **Reskin in XML** — new `colors.xml`, drawables, layout tweaks. Lowest risk, fastest visible win.
2. **Compose the field first** — the defense diagram already has commented-out Compose drafts in the codebase; it's the single screen most worth the migration investment.
3. **Drop Rx per screen** — each screen moved to Compose is a natural point to swap its remaining Rx chains for coroutines/Flow. No separate "big bang" needed.
4. **Retire the drawer** — once 2–3 core screens are Compose, revisit navigation: bottom nav (already in every mockup) fits one-handed dugout use better than a hamburger drawer. Team identity/switching move to the dashboard's app bar (see §3).

## 2. Design system — "The Dugout Card"

**Thesis:** stop looking like a generic Material app that happens to manage baseball rosters. Build the interface out of the manager's own tools — the lineup card, the scorebook grid, the jersey number.

**Palette** — dark "night game" base, one accent:
| Token | Hex | Use |
|---|---|---|
| Night / Ink | `#0B1D26` | background |
| Panel | `#16323D` | cards, sheets |
| Cleat Orange | `#FF6A2C` | primary accent, only accent used for actions |
| Infield Grass | `#3C7A5B` | secondary/semantic (female, softball-adjacent, "grass" pill) |
| Scorebook Gold | `#E8B93F` | reserved for DP/DH linkage only — a second accent used sparingly for one specific concept, not a general highlight |
| Foul Line Red | `#FF5C72` | destructive actions exclusively |
| Chalk | `#F4F7F2` | primary text |

A light theme is fully defined via CSS custom properties (`:root[data-theme="light"]` + `@media (prefers-color-scheme: light)`), not just a dark-mode afterthought.

**Typography:** no custom webfont anywhere. System sans (`-apple-system` stack) at weight 900/uppercase for display, weight 500–700 for body. Every number that matters — jersey number, batting order, stat, date — uses `font-variant-numeric: tabular-nums` on a monospace stack, so digits align and never jiggle.

**Recurring visual language (used consistently, not per-screen):**
- **Dashed border = "add" or "empty."** An open field position, an empty player photo, a "create new team/tournament" row all use the same dashed-outline treatment. One visual grammar for "nothing here yet" everywhere.
- **Flat vector iconography**, no photo-textured assets — replaces the current app's stock-photo team illustration, textured grass field background, and static-map tournament thumbnail.
- **Bottom sheets replace native `AlertDialog`s** throughout — every confirm/pick/form interaction that's a dialog today becomes a `.sheet`/`.scrim` bottom sheet.
- **Destructive actions are always foul-red**, never sharing color with "Save"/"Confirm" — established after catching myself using the same orange for both in an early pass.
- **Sheet stacking rule** (this took a real back-and-forth to get right, see §4): a sheet's content swaps in place for *forward navigation into a sub-task* (e.g. picking a roster from within the edit-lineup sheet); a **second, visibly smaller** sheet is layered on top only for an *interrupt/guard on dismissing the sheet you're already in* (e.g. "discard changes?"). Never two equal-sized sheets stacked — that was flagged explicitly as confusing and fixed.

## 3. Screen inventory (34 screens in the gallery)

Grouped by flow; each bullet is what changed and, where relevant, what real fact justified it.

**Onboarding**
- **Welcome** — replaces the misnamed `LoginActivity` (there is no login; it's a background image + two buttons with zero context). New: masthead, headline, 3 value-prop rows, same two real actions (create/import a team).

**Dashboard**
- One "what's next" hero card instead of six equal-weight tiles. Team switching moves from the drawer into the app bar itself (name + chevron → Switch Team sheet), since the drawer is being retired. The number-lookup tool stays on the dashboard (kept deliberately, unlike bulk messaging — it's a glance-and-go tool used mid-conversation with another coach).

**Team**
- **Switch team sheet** — team list + "create a new team" row (dashed, same language as everywhere else). Deliberately does **not** include a per-team edit shortcut — editing lives in exactly one place (see Settings), decided after initially adding a redundant pencil here and removing it on request.
- **Team details** — promoted into Settings as its first entry (today it's only reachable via the drawer, and Settings has no link to it at all). Includes a **"Roster by gender" stat card** — `Player.sex` (`MALE`/`FEMALE`/`UNKNOWN`) is a real, already-used field (defense-field color coding, Settings' men's/women's color pickers), and `TeamDetailsFragment` already computes this breakdown today — it was just buried as plain text; this gives it the same stat-chip treatment as roster size / lineups built.
- **Team form** — type picker changes from a 3-page `ViewPager2` carousel (hides 2/3 of the options behind a swipe) to 3 visible cards.
- *Rejected during review:* an age-category label ("12U") on the team — there's no such field on `Team` (only `id`, `name`, `image`, `type`, `main`, `hash`). Parked as a memory note: revisit only if a category-selection/filter screen ever exists to consume it.

**Roster / Player**
- Roster rows gain a **photo tile or dashed placeholder** — `Player.image` is a fully real, already-wired feature (pick/persist/display in several places today) that the current roster list simply never rendered. Jersey number becomes a corner badge on the photo instead of a separate square.
- **Player form** — one asterisk on Name (the only real required field today; everything else already optional but not marked as such). **Discard-confirmation on Cancel already exists in the real app** (`DialogFactory.getDiscardDialog`, shared with team/lineup creation) — just reskinned as a sheet, with the destructive action in foul-red instead of sharing cleat-orange with Save.
- **Player profile** — position-usage bar chart now shows a **count above each bar**, not just relative height (a bar reading "twice as tall" is a shape to interpret; "15" next to "3" is a fact).

**Tournaments / Lineups**
- Tournament list: photo-textured static-map thumbnail dropped for a tappable pin + address (opens the maps app directly). Search matches lineup names too, confirmed intentional in the real app. Each ticket's lineups nest inline, disclosed via a **chevron that flips direction** on expand/collapse (an earlier "Show lineups" text link was too easy to miss and gave no way to tell the row could be collapsed again) — with a real **slide/fade reveal animation** in the prototype, not an instant show/hide.
- **Create Lineup** — date field visibly disabled until a tournament is picked (today it's tappable but errors after the fact). Extra-hitters keeps "∞" as its own segment rather than a 6th number.
- **Edit lineup (metadata: name/tournament/roster)** — reachable as a sheet from *either* the tournament list's row-level pencil *or* a pencil in the lineup screen's own app bar; both now open the exact same sheet. Tapping "Import"/pencil **no longer forces a detour through the Fixed→Editable canvas** just to rename a lineup or change its roster — fixed after the user pointed out the extra navigation was pointless for a metadata-only edit.
- **View vs. Edit mode, materialized.** The real app already has two separate implementations (`DefenseFragmentFixed`/`DefenseFragmentEditable`, two layouts, `DefenseFixedView` never registers a drag listener) — but nothing on screen ever said which one you were in beyond a pencil buried in an overflow menu. Now: View mode gets an unmistakable **"Edit ›" pill** in the app bar (Share/Delete stay as quiet equal-weight icons next to it — all three were already always-visible today, just equally weighted); Edit mode gets a persistent **"Editing positions/order" tag** plus a Save/Cancel bar replacing the bottom nav (matches `binder.bottomChoice`, real today).
- **Delete lineup** — sheet reproduces the real, already-existing confirm dialog (`DialogFactory.getWarningTaskDialog`, distinct from the discard-changes dialog) almost verbatim, just as a sheet.
- **Share** — `exportLineupToExternalStorage()` really does just screenshot whichever tab is visible and hand it to the OS share sheet; the mockup is that native sheet, with a one-line caption naming the real limitation (only one tab captured, never both) as a flagged future fix, not something silently changed.
- **Add substitute** — the bench-picker that already exists behind the field's "+" today. The redesign's real contribution: showing the `extraHitters` budget *before* you pick ("1 of 1 extra hitter slot free — next pick bats"), because today whether a substitute ends up batting or benched is decided silently by `AssignPlayerFieldPosition` with zero on-screen warning either way.
- **Substitute add/remove/replace**, worked out in detail after direct questions:
  - *Add*: pick from the sheet, exactly as today.
  - *Remove*: added a confirmation ("cannot be undone," matching the real dialog string) — removing a substitute who was batting **automatically promotes the next bench substitute**, replicating `DeletePlayerFieldPosition`'s real bookkeeping. Deliberately **not** exposed on the Batting Order list — `AttackItemTouchCallback` explicitly disables swipe there today (`isItemViewSwipeEnabled() = false`), so that screen stays reorder-only, matching reality exactly; removal only lives in the Add-substitute sheet and the field itself.
  - *Replace*: no dedicated flow in the real app either — it's remove-then-add. The roster checklist's rows for "starting" players got a **swap icon** that deep-links straight to that position's field marker for a one-tap replace, which the real app doesn't have today (a genuine addition, flagged as such).
- **Roster checklist ("Roster for this lineup")** — real bug found: unchecking a player today only edits the roster string (`UpdateLineupRoster`) and never touches their field position or batting slot, so a removed player can keep showing up assigned ("ghost assignment"). Fixed in the design: rows now show **Starting / Bench / (unchecked = sitting out)** state, and unchecking a *Starting* player asks for confirmation first, naming exactly what gets cleared.
- **DH / DP-Flex** — was a checkable overflow-menu item (`action_add_dh`/`action_add_dp_flex`), invisible unless you went looking. Now a labeled **switch** directly on the canvas. Turning it on shows the real auto-link (pitcher gets a gold lock badge — for baseball, Flex is hard-locked to whoever's currently at P). Turning it off is destructive in the real app too (`UpdatePlayersWithLineupMode` fully resets both the DP and Flex player, not just unlinks them) — this now asks for confirmation first, naming both players, which the current app does not do at all.
- **Import/Export data** (added last):
  - *Export*: real behavior is a SAF folder picker that writes **every team on the device** into one `.elu` file — not scoped to the active team — and the success dialog names only the folder. New: a sheet stating the real scope with counts, before Android's own folder picker takes over (that native picker isn't reskinned — it can't be).
  - *Import*: found a real inconsistency — opening a `.elu` file always overwrites matching records, while the Settings entry point respects a separate "update object if exists" checkbox; neither path previews what's in the file. Also found that `ImportData` already computes a full `ImportResult` (inserted/updated counts per teams/players/lineups/tournaments) that the UI throws away and replaces with a generic "Successfully imported data." Fixed with one unified confirm sheet (file preview + counts + the overwrite toggle, same regardless of entry point) and a success sheet that finally shows the real counts.

**Settings**
- Didn't exist as a drawn screen at all — every other mockup treated "Team" as if it were already the first row in Settings. Real categories (`SettingsFragment`'s Style/Data/About) kept as-is, just grouped under section labels instead of a flat `PreferenceFragmentCompat` list.

## 4. Process notes (how this gallery was built, worth preserving)

- Every screen was designed only after confirming the real current behavior via code research (use cases, fragments, view models, string resources) — never invented from the UI alone. Several "bugs" surfaced this way were real, pre-existing issues in the shipped app, not artifacts of the redesign (roster ghost-assignment, batting-order delete gap, import path inconsistency, DP/DH destructive toggle with no confirm, discarded `ImportResult`).
- Two ideas were proposed, then explicitly deprioritized by the user rather than rejected outright — kept in project memory for later: a team age-category field (no supporting screen exists yet to consume it), and a mechanical-scoreboard visual module (liked, but no clear landing spot yet).
- The interactive prototype surfaced one real implementation bug during its own build (a `null.textContent` crash when re-entering edit mode after the view-mode marker's DOM was rebuilt) — caught via an actual headless DOM test (`jsdom`), not just code review, after a first "looks correct" pass missed it.

## 5. Interactive prototype

Same visual system, wired with vanilla JS into one connected app shell (Pixel 9a frame, 412×923dp) — navigate the whole flow, not just look at stills. Real interactions: sheet open/close and content-swap vs. stacking, tab switching, the DH toggle and its destructive-confirm, add/remove/promote for substitutes, drag-to-reorder on the batting list (native HTML5 drag & drop), search-filtering the tournament list, and the full import/export sheets with a live overwrite-toggle affecting the result counts shown.

**Deliberately mocked, not simulated:** free-form 2D drag-and-drop of field positions (tap a marker instead — opens the same assign/remove sheet a real drag would); native OS pickers (folder picker, file picker, share sheet) are represented by the screen that follows them, not reproduced; multi-team switching changes only the header, not a second dataset.

## 6. Parked for later

See `.claude/projects/.../memory/idea_team_age_category.md` and `idea_scoreboard_theme.md` for the full write-ups. Short version: age-category field (no consuming screen yet), scoreboard visual module (liked, no clear landing spot yet).
