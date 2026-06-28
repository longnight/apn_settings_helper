# APN Settings Helper — Project Notes

MIT, open-source Android app that restores a phone's mobile-data **APN** from curated, verified
presets — for MVNO users and travellers putting a local prepaid SIM in an unlocked phone. Japan-first,
region-extensible. **Status: v1.1.0 released** (`origin/main`; `v1.1.0` tagged + GitHub release — adds the opt-in float-over-editor overlay).

> Brand name stays neutral (no "Japan/JP"). Package id `io.github.ln.apnsettingshelper` is unique vs
> the existing "APN Settings" app — differentiate by icon (teal SIM-card) + per-locale store listing.

## Document
Read documents under /docs

## The core constraint (the "why" behind everything)
Modern Android blocks third-party apps from writing APN settings:
- `WRITE_APN_SETTINGS` is `signature|privileged` — never granted to normal apps.
- Writing `content://telephony/carriers` needs that permission **or** carrier privileges.
- ⇒ **No silent "apply" for an ordinary app.** Every apply tier works around this.

## Apply tiers — decisions a maintainer must not undo
- **Manual (default, zero perms):** per-field copy buttons + "set to X" checklist for dropdowns + a
  button to open the system APN editor. The universal path.
- **Root (opt-in, `libsu`):** one-tap "Apply now" writes the provider row directly. **Probe `su` only
  after the user opts in** (a toggle) — never eagerly (preserves "invisible until opened").
- **Float-over-editor (`SYSTEM_ALERT_WINDOW`, opt-in):** implemented manual-assist helper —
  `ui.overlay.ApnOverlay` floats the preset's values + per-field Copy buttons over the **system APN
  editor** so the user fills it without bouncing back to our app. Does **not** auto-fill (that needs the
  rejected AccessibilityService); wired as a UI helper like `openApnEditor`, **not** through
  `ApplyStrategy`. Copy survives Android-10+/MIUI's background-clipboard block via a **per-copy hybrid**:
  silent write from a fresh focusable overlay window → read the clip back → if it didn't stick, write as the
  foreground app via `ClipboardWriteActivity` (MIUI gates writes on being the foreground app, not focus) —
  detail in `docs/plan_20260628_clipboard_float_overlay_improvements.md`. The `OverlayStrategy` apply-seam
  slot (a future *programmatic* overlay tier) is still a stub.
- **Rejected — do NOT add:** Device Owner (needs factory-reset provisioning), AccessibilityService
  (fragile across OEM/locale, Play-banned), carrier privileges (needs carrier cooperation).
- **Keep `ApplyStrategy` as the single seam** so a future opt-in self-healing watcher stays additive.

## Product decisions a maintainer must not undo
- **Fire-and-forget:** no notification, tile, background service, or watcher. Invisible until opened.
- **Favorites (many) and last-applied (one) are independent.** Favorites were chosen over a single
  "current" marker because the app **cannot verify the device's real APN** — last-applied is a passive
  history note (`{presetId, timestamp}`, overwritten each apply), not a "current" claim.
- Last-applied is set automatically on a root Apply; manual users get an explicit "Record as applied"
  button (hidden for the root tier, which auto-records).
- Wording: **"settings"**, not "configs", in UI/store copy. `app_name` localizes (`APN設定ヘルパー`).
- All state is **local — no network, no accounts, no tracking.**

## Data & permission contracts
- **Presets:** bundled `app/src/main/assets/presets.json`, grouped `region → carrier → preset`
  (schema + add-a-preset guide: `CONTRIBUTING.md`; model details: `plan_coding_steps.md`).
- **Persisted state (DataStore):** `favorites: Set<String>`, `lastApplied: {presetId, epochMillis}?`.
- **Permissions:** core flow none; float-over-editor overlay `SYSTEM_ALERT_WINDOW` (optional,
  user-granted); root uses `su` if present (no manifest permission).

## Locked tech (v1)
- `applicationId` **`io.github.ln.apnsettingshelper`** · **minSdk 26** · compile/target **35**.
- Kotlin + Jetpack Compose + Material 3 + DataStore + kotlinx.serialization + `libsu`; single `:app`.
- FOSS-only deps (no GMS). Distribution: **F-Droid + GitHub APK** first, kept Play-compatible. MIT.
- AndroidX/Compose pinned at the **API-35 line** — read `plan_coding_steps.md` before bumping.

## Where things are (orient a fresh session)
- **Product / why + this restart guide:** `AGENTS.md` (here).
- **Architecture, file map, data contracts, gotchas, tech debt:** `plan_coding_steps.md`.
- **Dev environment (Nix devShell, commands, emulator):** `plan_implement_steps.md`.
- **Root-apply internals (provider mapping, verify flow, manual-verify, emulator caveats):**
  `plan_review_M-E.md`.
- **Build/run/test/release for humans:** `README.md`; contributing: `CONTRIBUTING.md`.
- **Code:** `app/src/main/kotlin/io/github/ln/apnsettingshelper/` — `domain.model`, `domain.apply`,
  `data.preset`, `data.store`, `data.root`, `ui.{list,detail,common,overlay,nav,theme}`, `AppGraph`,
  `ApnApplication`, `MainActivity`. i18n: `res/values/` (en) + `res/values-ja/` (ja).

## Build & verify (inside the Nix devShell)
- `just ci` — strict gate: JVM tests + ktlint + detekt + Android lint. `just test` = same, lenient.
- `just emu-test` — instrumented tests (needs the `apnhelper` emulator).
- `./gradlew :app:assembleDebug` — APK. Non-interactive shells: see `plan_implement_steps.md`.

## Deferred / open (the v1 boundary)
- **Self-healing watcher** (auto-detect APN loss + re-apply) — post-v1, additive behind `ApplyStrategy`.
- **Overlay:** the **float-over-editor** manual-assist helper is **implemented** (`ui.overlay`, opt-in
  `SYSTEM_ALERT_WINDOW`). Still deferred: the `OverlayStrategy` *programmatic* apply tier (stub) +
  overlay polish (non-MIUI clipboard verification; surviving process death without a service).
- **More regions/carriers** via community PRs (data-only; no restructuring).
- **Release:** F-Droid `fdroiddata` recipe must build `libsu` from source (JitPack disallowed); back up
  the gitignored signing keystore. Smaller polish/tech-debt items are listed in `plan_coding_steps.md`.
