# UI Improvement Plan — Overlay Panel + Main Screen

> Status: **shipped in v1.2.0 (`v1.2.0` tagged + GitHub release); `just ci` green; verified on-device
> (Redmi K30 5G / MIUI 12.5 + Xiaomi 2304FPN6DC / Android 13).** Main + detail screens checked via screenshots + the full
> instrumented suite; the overlay (flat chips, sections, dropdown list, flat header buttons,
> timed-flash ✓) checked live floating over the system APN editor. Two redesigns: (1) the float-over-editor overlay panel, (2) the main preset-list screen.
> Each direction below was chosen interactively from an option set; rejected options are kept for
> the record. Cross-cutting: teal brand accent (`ui/theme/Color.kt`), FOSS-safe generated avatars
> (carrier initials + tint, **no trademark logos**), localized dates via `ui/common/ApnDateFormat`.
> Nothing here changes app behavior — apply tiers, the MIUI clipboard hybrid, and the
> favorites/last-applied semantics are all untouched.

---

## 1. Float overlay panel

**Decision: tap-to-copy chips.**
Rejected: _polished list_ (same layout, just prettier), _guided one-field wizard_, _progress
checklist_.

### Current state (`ui/overlay/OverlayPanel.kt`, `ApnOverlay.kt`)
Dark 300dp card, draggable header (title · collapse · close) → one-line hint → one row per field.
Copyable rows = `"label: value"` + a stock `Button`; the button flashes `✓` for 1.2s
(`flashCopied`, `COPIED_FEEDBACK_MS`) then reverts. Dropdown rows = dim `"label → value"` text.
Reads like a debug overlay; no memory of what's been copied; the required dropdown steps are easy
to miss.

### Target
```
╭──────────────────────────────────────╮
│ ⠿  IIJmio (docomo)        –        ✕ │  drag · collapse · close
│ Tap a chip to copy, then long-press   │  one-line hint
│ the field below and Paste             │
├──────────────────────────────────────┤
│ COPY THESE                            │  teal section label
│ ┌ APN ─────────┐ ┌ Username ───┐      │
│ │ iijmio.jp  ⧉ │ │ mio@iij   ✓ │      │  tap chip = copy; ✓ flashes ~1.5s
│ └──────────────┘ └─────────────┘      │
│ ┌ Pass ┐ ┌ MCC ┐ ┌ MNC ┐              │
│ │ iij ⧉│ │ 440 ⧉│ │ 10 ⧉│              │  short chips pack together
│ └──────┘ └─────┘ └─────┘              │
│ ┌ MVNO value ──────────┐              │
│ │ NTT DOCOMO         ⧉ │              │  long value → its own full row
│ └──────────────────────┘              │
├──────────────────────────────────────┤
│ SET THESE DROPDOWNS                   │  teal label · expanded list
│ Auth type            → PAP or CHAP    │
│ APN protocol         → IPv4/IPv6      │  plain labels, no checkboxes
│ APN roaming protocol → IPv4/IPv6      │
│ MVNO type            → SPN            │
╰──────────────────────────────────────╯
```

### Spec
- **Chips replace label+Button rows.** The whole chip is the tap target (no separate Copy button →
  saves width); a trailing `⧉` glyph signals "tap copies."
- **Flexible flow:** chips wrap — short values pack 2–3 per row, a long value (APN / MMSC URL / MVNO
  value) takes its own full-width row. **No truncation; every value shows in full.**
- **Timed-flash `✓`:** on tap the chip flashes to `✓` with a stronger teal fill, then reverts to
  `⧉` after ~1.5s. (We tried a *persistent* ✓ first; on-device it read as a stuck state — several
  chips left checked at once — so it reverts, a momentary confirmation like the original button flash.)
- **Dropdowns:** the non-copyable fields render as an **expanded `SET THESE DROPDOWNS` list, plain
  labels** (one `label → value` line each, no checkboxes/state).
- **Style — flat M3 buttons.** Chips/copy targets use a **flat** Material 3 look — rounded
  container, teal M3 fill, ripple, **no elevation/shadow** — hand-styled on the plain Views the
  overlay already uses, so **no new dependency**. (Decision: flat, not elevated, in the overlay.)
  The header **collapse / close** buttons are likewise flat — white glyphs with a circular ripple,
  no stock-`Button` gray box.
- **Carried over unchanged:** draggable header + collapse + close, teal accent, one-line hint, and
  the entire clipboard backend (silent proxy write → read-back verify → `ClipboardWriteActivity`
  foreground fallback on MIUI).

### Approach path
- **`ApnOverlay.Row(label, value, copyable)` already splits copyable vs dropdown** — and
  `FloatOverEditorButton` already emits copyable rows first, then checklist rows. So the
  COPY-THESE / SET-THESE split needs **no model or call-site change**; it's purely how
  `OverlayPanel` renders the two groups.
- `OverlayPanel.kt`:
  - Replace `fieldRow()` for copyable rows with a **chip view** styled as a **flat M3 button**
    (rounded `GradientDrawable` bg, teal M3 fill, ripple foreground, **no elevation**; label
    small/dim over value, trailing `⧉`; `setOnClickListener → spec.onCopy(value, chip)`).
  - Swap the single vertical `list` `LinearLayout` for a **wrapping container**. Android has no
    built-in wrapping layout: implement a small custom `FlowLayout` `ViewGroup` (~60 lines, measures
    children and wraps at the 300dp panel width) — preferred over adding the `flexbox-layout` dep
    (FOSS but an extra dependency for one screen). Long chips that don't fit wrap to their own row →
    gives the "flexible flow" behavior for free.
  - Render non-copyable rows as the plain `SET THESE DROPDOWNS` list (today's dim `label → value`
    lines, under a teal section label).
  - Add teal section labels (`COPY THESE` / `SET THESE DROPDOWNS`).
- `ApnOverlay.kt`: `copy()` / `writeViaProxy()` / `clipboardHolds()` / `writeClip()` — **unchanged**
  (the MIUI hybrid is untouched). `onCopy` becomes `(value) -> Unit`; the chip owns its own
  timed-flash feedback (`✓` for ~1.5s via `postDelayed`, then reverts to `⧉`).
- `res/values*/strings.xml`: add `COPY THESE` / `SET THESE DROPDOWNS` section labels, shorten
  `overlay_hint`; **en + ja** both.
- Theme: copied-chip tint from `Teal40`/`Teal80`.
- **Effort: M. Risk: low** — presentation-only; clipboard path untouched.
- **Verify:** ✅ on-device (Redmi K30 5G / MIUI 12.5): chips render + wrap, copy works (chip → paste),
  the `✓` flashes and reverts, flat header buttons, drag / collapse / close — all floating over the
  system APN editor. `just ci` green.

---

## 2. Main screen (preset list)

**Decision: flat list of full-width, clearly-separated preset cards — M3 `ElevatedCard`.**
Started from _carrier cards + drill-in_ but **flattened — no 3 levels, no drill-in**; each card is a
preset (`IIJmio (docomo)`, `IIJmio (softbank)`, `mineo (docomo)`…). Rejected: _search-first grouped
list_, _carrier cards + drill-in_ (extra navigation level), _collapsible accordion_, _hero + tabs_.
Card layout chosen as **full-width** over 2-column variants specifically so the last-applied
footnote fits without truncation (the localized date — e.g. `2026年6月28日` — is too long for a
~165dp grid card; it fits cleanly on its own line in a ~340dp full-width card).

### Current state (`ui/list/PresetListScreen.kt`, `PresetListViewModel.kt`)
Bare `TopAppBar` (app name only) → grouped `LazyColumn`: `★ Favorites` section, then
region header → carrier header → `ListItem` rows (label, carrier + last-applied, favorite heart).
Clean Material 3 but: no search/filter (catalog grows via community PRs), no visual identity, all
groups always expanded.

### Target
```
┌───────────────────────────────────────────┐
│ APN Settings Helper     🔍   Japan ▾   ⋮  │  region selector: compact, top-right
├───────────────────────────────────────────┤
│ ★ FAVORITES                               │
│ ╔═══════════════════════════════════════╗ │
│ ║ (IJ) IIJmio (docomo)            ♥     ║ │  M3 ElevatedCard — pops via shadow
│ ║      Last applied · Jun 28, 2026      ║ │  recorded datetime, own line
│ ╚═══════════════════════════════════════╝ │
│                                           │
│ ALL PRESETS                               │
│ ╔═══════════════════════════════════════╗ │
│ ║ (IJ) IIJmio (docomo)            ♡     ║ │
│ ║      Last applied · Jun 28, 2026      ║ │
│ ╚═══════════════════════════════════════╝ │
│ ╔═══════════════════════════════════════╗ │
│ ║ (IJ) IIJmio (softbank)          ♡     ║ │  flat list — each card is one
│ ╚═══════════════════════════════════════╝ │  preset; no region/carrier headers,
│ ╔═══════════════════════════════════════╗ │  no drill-in
│ ║ (mi) mineo (docomo)             ♡     ║ │
│ ╚═══════════════════════════════════════╝ │
│ ╔═══════════════════════════════════════╗ │
│ ║ (po) povo (au)                  ♡     ║ │
│ ╚═══════════════════════════════════════╝ │
└───────────────────────────────────────────┘
```

### Spec
- **Flat list of preset cards** — no region/carrier header rows and no carrier drill-in. The carrier
  is shown on each card (avatar + `Carrier (line)` title), so headers are redundant.
- **Full-width `ElevatedCard`** — each card pops off the background via M3 elevation/shadow (raise
  the elevation a step so it's clearly obvious), not a border.
- **Card content:** carrier avatar (initials + tint) · `Carrier (line)` title · favorite heart
  (trailing — **muted** `onSurfaceVariant` when empty, **teal** `primary` when favorited) ·
  `Last applied · <date>` footnote on its own line, **only** on the one last-applied preset (full
  localized date via `ApnDateFormat`).
- **Favorites** stay as a `★ FAVORITES` section of the same bordered cards at the top; the rest under
  `ALL PRESETS`.
- **Region selector compact, top-right** (`Japan ▾`) instead of a full-width region header — it's
  rarely changed in a Japan-first app. With only one region today it's effectively a static chip;
  becomes a real dropdown once a 2nd region ships.
- **Search** (`🔍` in the top bar) filters across carrier + preset names; matching presets surface
  directly in the flat list.
- **Ordering:** both the Favorites section and the main list are sorted **A→Z by display name**, so
  positions are predictable. Favoriting still lifts the item into the Favorites section (kept), but
  to a findable alphabetical spot. (Japanese sorts by code point — katakana ソ before ド.)

### Approach path
- `PresetListScreen.kt`:
  - Replace `PresetRow`'s `ListItem` with an **`ElevatedCard`** (`CardDefaults.elevatedCardElevation`
    — raise a step so cards pop): `Row { CarrierAvatar; Column { title; optional footnote }; favorite
    IconButton }`.
  - **Detail screen** adopts the same M3 elevated style: copyable-field rows become **`ElevatedCard`**
    and all action buttons become **`ElevatedButton`** (`Open APN editor`, `Float over the APN
    editor`, `Apply now`, `Record as applied`).
  - Drop `SectionHeader(region)` / `CarrierHeader` — keep only the `★ FAVORITES` / `ALL PRESETS`
    labels.
  - `TopAppBar`: add a **search action** (icon → expanding `SearchBar`/`TextField`) and a **region
    selector** (`DropdownMenu`) as a right-side action; optional `⋮` overflow (About / language) is
    separate/optional.
  - New `CarrierAvatar(name)` composable: `Box` (circle, bg = deterministic color from the carrier
    name hash mapped into the teal palette in `ui/theme/Color.kt`) + initials `Text`. **No logos**
    (trademark-safe, FOSS).
- `PresetListViewModel.kt`:
  - Flatten `regions → carriers → presets` into a single `List<PresetRowUi>` (favorites still split
    out). Add a `region` field to `PresetRowUi` to back the top-right filter.
  - Add `searchQuery` + `selectedRegion` state and expose a filtered flat list.
  - `PresetRowUi` already carries `label`, `carrier`, `isFavorite`, `lastAppliedLabel`
    (`ApnDateFormat`-formatted) — reuse as-is for the footnote.
- **Effort: M. Risk: low–med** (VM flatten + search/filter wiring).
- **Verify:** search filter, region selector, favorite toggle, last-applied footnote rendering
  (en + ja, incl. the long JA date), bordered cards; `just ci` green.

---

## Cross-cutting
- **Component styling (Material 3):** Compose screens (list + detail) use **`ElevatedCard` +
  `ElevatedButton`**; the Views overlay uses **flat** M3-style buttons/chips (rounded, teal fill,
  ripple, **no elevation**), hand-styled on plain Views — **no new dependency**.
- **Teal brand accent** — `ui/theme/Color.kt` already has `Teal40/80`, `TealGrey40/80`, `Sand40/80`.
  Used for the overlay header + copied-chip tint, and the main-screen avatars + button/card accents.
- **Generated carrier avatars** (initials + deterministic tint) — shared idea; **never trademark
  logos** (FOSS / Play / F-Droid safe).
- **Localized last-applied date** via existing `ui/common/ApnDateFormat`.

## Shipped refinements (v1.2.0 — on-device tweaks that superseded parts of the design above)
- **Overlay `✓` is a timed flash** (~1.5s revert), not persistent — a stuck row of checks read as broken on MIUI.
- **Overlay header** collapse/close are flat (white glyph + circular ripple), not stock gray `Button`s.
- **List cards: carrier is the title, network is the subtitle** (e.g. `IIJmio` / `Docomo`) — the old "Carrier (Network)" title repeated the carrier subtitle. Both sections sorted **A→Z**. Empty hearts **muted** (`onSurfaceVariant`), favorited **teal** (`primary`).
- **Preset labels split**: the list shows the clean network only (`Type`/`plan`/`line` dropped); the full designation (`Type D / Docomo`) is a **caption above "Open system APN editor"** on the detail screen (carriers with no Type — e.g. HIS — show none). Backed by a new optional `Preset.line` field (+ `PresetDto`), derived subtitle in `PresetListViewModel`.
- **Detail copy buttons** match the overlay: a `⧉` icon that flips to `✓` for ~1.5s, then reverts.

## Out of scope / deferred
- **Preset label rewording** (`Type D` → `docomo`, `Type A` → `au`, etc.) in
  `app/src/main/assets/presets.json` — the mockups assume the human-friendly line name. Data-only;
  a separate change/PR, not part of this UI work.
- **About / language overflow menu** — mentioned for the top bar, optional, separate.
- `flexbox-layout` dependency — avoided in favor of a small custom `FlowLayout`.
- **Material Components for Android** (`com.google.android.material`) — considered for the overlay
  (authentic M3 `MaterialButton`/`Chip`/`ChipGroup`), **not adopted**: a flat look needs no library
  and avoids `Theme.Material3` `ContextThemeWrapper` plumbing on the overlay context.
- No change to apply tiers, the clipboard hybrid, or favorites/last-applied semantics.

## Further refinements (post-v1.2.0, on-device iteration)
Behaviour/wording polish after the v1.2.0 redesign; **data model unchanged** (favorites set + a
single last-applied slot — still a passive, unverifiable note):
- **Favoriting no longer pops the card out of the list.** A hearted preset stays in its A→Z spot
  under *All Presets* **and** mirrors into ★ Favorites (was: removed from the list) — kills the
  confusing "jump to top". (`PresetListViewModel`: dropped the `!isFavorite` filter.)
- **Whole field card copies.** On the detail screen the entire `CopyableField` card is the tap
  target (was: a small trailing icon button); the `⧉` glyph still flips to `✓` for ~1.5s.
- **Detail screen reordered.** *Notes* moved to the **top** and now **merges the line/plan
  designation** (e.g. `Type D / Docomo`) with the freeform notes into one area
  (`ui.common.PresetNotes`, replacing `PresetLineCaption`). The manual record button moved **up to
  just below "Float over the APN editor."**
- **"Record this as applied" → "Mark as in use."** Emphasises a user-asserted mark over "record".
  Footnote `Last applied <date>` → `In use · <date>` (home card + detail); toast → `Marked as in
  use`. en + ja.
