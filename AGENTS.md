# APN Settings Helper — Project Notes

> Name: **APN Settings Helper** (casing flexible). Android app · MIT · public, open-source.
> Note: name is close to the existing "APN Settings" app — package id is unique
> (`io.github.ln.apnsettingshelper`); differentiate via icon + store listing.
> Status: **dev env ready (`plan_implement_steps.md`); v1 decisions locked; coding plan in `plan_coding_steps.md`. M-A (scaffold) done & verified on emulator (2026-06-27); next: M-B (preset model + data).**

## Problem
Phones not sold for the Japanese market, used in Japan on budget MVNO SIMs (e.g. HIS Mobile),
require manual APN setup. In weak-signal areas the phone can silently lose its APN config;
restoring it by hand is tedious and error-prone.

## Goal
A generic, public, MIT-licensed app for anyone with APN pain — works across most devices,
localized at least **English + Japanese**. Curate accurate Japan-MVNO APN presets; let the user
pick one and apply it fast.

## Positioning & scope
- The problem is **global**, not Japan-only: MVNO users (US/EU/etc.), and especially
  **travelers / expats** putting local prepaid SIMs in unlocked phones. The mechanism is
  country-agnostic; only the **preset data** is local and extensible.
- **Japan is the launch wedge, not the boundary:** v1 ships deep, accurate Japan-MVNO presets first.
- **Neutral, global app name** (no "Japan/JP" in it) — emphasize Japan via a Japan-tuned
  **per-locale store listing** (格安SIM / APN設定 keywords), which Play and F-Droid both support.
- **Preset data model:** `country/region → carrier → preset`, from day one. Other countries added
  by us or via community PRs without restructuring.
- Wording: use **"settings"** (not "configs") in user-facing UI/store copy — matches Android and
  carrier (HIS = APN設定) usage; "config" is fine in dev/code contexts only.

## Core platform constraint (the "why" behind everything)
Modern Android blocks third-party apps from writing APN settings:
- `WRITE_APN_SETTINGS` is `signature|privileged` — never granted to normal apps.
- Writing `content://telephony/carriers` needs that permission **or** carrier privileges.
- ⇒ **No silent "apply" is possible for an ordinary app.** Every strategy below works around this.

## Strategy analysis (4 privilege tiers)
| Tier | Mechanism | Verdict |
|---|---|---|
| **A. Deep-link + overlay** | Open system APN editor; assist manual entry | ✅ **Universal default** |
| **B. Device Owner** | `DevicePolicyManager.addOverrideApn()` (API 28+) | ❌ Rejected — needs factory-reset / no-accounts provisioning |
| **C. AccessibilityService** | Auto-fill the APN form by label text | ❌ Rejected — fragile across OEM/locale, Play-banned |
| **D. Root** | `su` → write telephony provider directly | ✅ **Opt-in bonus** for already-rooted users |
| Carrier privileges | App cert whitelisted on the SIM | ❌ Needs carrier cooperation — infeasible |

## Decisions (v1)
**Normal users (zero permissions)**
- Preset list → detail → **per-field copy buttons**; user pastes into the system APN editor field by field.
- Dropdown-type fields (auth type, protocol, MVNO type) shown as **"set to X" checklist items**, not copy buttons.

**Optional overlay (opt-in, `SYSTEM_ALERT_WINDOW`)**
- Floats the preset values over the APN editor to remove app-switching. **Off by default.**

**Rooted users (su granted)**
- One-tap **"Apply now"** — writes the preset via the telephony provider and sets it active (lib: `libsu`). Nothing persistent.

**Philosophy: fire-and-forget**
- No notification, no Quick-Settings tile, no background service, no watcher. **Invisible until opened.**

**Self-healing (auto-detect APN loss + re-apply): NOT in v1**
- Deliberately deferred (danger + complexity). Keep the apply logic behind a single interface so a
  future **opt-in** watcher is an additive change — do **not** architect it out.

## Preset list UI
- **♥ Favorites (multiple)** — heart toggle per row; user may favorite any number; faved rows float to
  a "★ FAVORITES" section at top. (Chosen over a single "current" marker, which would pressure the user
  to keep the app in sync with the device's real APN — which the app can't verify anyway.)
- **"last applied YYYY-MM-DD HH:MM"** muted line on the last-applied row — single slot
  `{presetId, timestamp}`, overwritten each apply. A passive history note, **not** a "current" claim.
- Favorites (many) and last-applied (one) are independent — may coincide or differ.
- Last-applied set: root = auto on Apply; normal users = explicit **"Record this as applied"** button in the detail screen.
- i18n: localize labels + date format (En `2026-06-26 14:30`, Ja `2026年6月26日 14:30`).

## Persisted local state (all local, no network)
- `favorites`: set of presetIds (multiple)
- `lastApplied`: `{presetId, timestamp}` | null
- presets data (bundled; `country/region → carrier → preset`; Japan first; community-extendable)

## Permissions
- Core flow: **none**.
- Overlay: `SYSTEM_ALERT_WINDOW` (optional, user-granted).
- Root: no manifest permission — uses `su` if present.

## Tech (proposed, not final)
- Kotlin + Jetpack Compose · `libsu` for root · DataStore for persistence · Min SDK TBD.

## Decided (v1) — 2026-06-26
- **applicationId:** `io.github.ln.apnsettingshelper` · **minSdk 26** · compileSdk/targetSdk 35.
- **Preset coverage breadth:** **broad Japan** (10+ MVNOs) for v1.
- **Distribution:** **F-Droid + GitHub APK first** (FOSS-only deps; no GMS), keep Play-compatible.
- **Stack confirmed:** Kotlin + Compose/Material3 + DataStore + kotlinx.serialization + `libsu` (root). Single `:app` module.
- Full implementation roadmap: **`plan_coding_steps.md`**.

## Deferred / open
- Opt-in self-healing watcher (post-v1; keep `ApplyStrategy` seam).
- Overlay strategy full implementation (stub in v1).
- More regions/carriers via community PRs (data-only).

## License
MIT~~
