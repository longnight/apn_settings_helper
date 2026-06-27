# APN Settings Helper — Architecture & Maintenance Reference

How the app is organized and the non-obvious choices behind it. Product rationale: `AGENTS.md`. Dev
environment: `plan_implement_steps.md`. Root-apply internals: `plan_review_M-E.md`.

## System overview & data flow
- `PresetRepository` loads + validates `assets/presets.json` → grouped `region → carrier → preset`.
- ViewModels `combine` the repo with DataStore flows (`favorites`, `lastApplied`) into immutable UI
  state; Compose renders it (single-activity, Compose Navigation `list → detail/{presetId}`).
- "Applying" goes through the `ApplyStrategy` seam (manual / root / overlay). There is **no silent
  write** — the manual path only guides the user and opens the system APN editor.

## Package map (`app/src/main/kotlin/io/github/ln/apnsettingshelper/`)
- `domain.model/` — `Preset`, `Carrier`, `Region`, enums (`AuthType`, `ApnProtocol`, `MvnoType`),
  `LocalizedText` (`resolve(tag)`), `LastApplied`. Pure Kotlin.
- `domain.apply/` — `ApplyStrategy` (seam) + `ApplyTier`/`ApplyOutcome`; `ManualCopyStrategy`,
  `RootStrategy`, `OverlayStrategy` (stub), `ApplyStrategyResolver`, `ShellRunner` (seam).
- `data.preset/` — `PresetDto` (+ `toDomain`), `PresetSerialization` (pure parse/validate/group),
  `AssetPresetRepository` (Android assets).
- `data.store/` — `SettingsStore` (interface) + `DataStoreSettingsStore` (Preferences DataStore).
- `data.root/` — `LibsuShellRunner` (the only libsu touchpoint).
- `ui.list/`, `ui.detail/` — stateless `*Content` composables + `*ViewModel` + UI-state classes;
  `ui.detail.DetailFields` (copy/checklist field builders). `ui.common/` — `CopyableField`,
  `ChecklistItem`, `ApnEditor` (open-editor intent), `ApnFieldDisplay` (enum→label), `ApnDateFormat`.
  `ui.nav/` — `AppNavHost`, `Routes`. `ui.theme/`.
- `AppGraph` — manual DI (no Hilt): parses presets once, builds `SettingsStore` + `ApplyStrategyResolver`;
  held by `ApnApplication`, read by ViewModel factories via `APPLICATION_KEY`.

## Data contract — `assets/presets.json`
- `{ schemaVersion: 1, regions: [{ code, name{en,ja}, carriers: [{ id, name{en,ja}, presets: [{…}] }] }] }`.
- Required per preset: `id` (globally unique), `label{en,ja}`, `apn`, `mcc`, `mnc`. Other fields
  default to `""`/enum default. Full field table + rules: `CONTRIBUTING.md`.
- **Validation on load** (`PresetSerialization`): `schemaVersion == 1`; unique preset/carrier/region
  ids; `apn` non-blank; `mcc` 3 digits, `mnc` 2–3 digits; enums in range (else parse fails).
  `Json { ignoreUnknownKeys = true }` so additive fields don't break old builds.
- **Dropdown fields** (`authType`, `protocol`, `roamingProtocol`, `mvnoType`) render as "set to X"
  checklist items; every other non-blank field renders as a copy button.

## Persistence (DataStore Preferences)
- `favorites: Set<String>` (any number); `lastApplied: {presetId, epochMillis}?` (single slot).
- Exposed as `Flow`s; a corrupt file yields `emptyPreferences()` (no crash). Clock is injected for tests.

## Apply strategy
- `ApplyStrategyResolver`: root shell present → `RootStrategy`; overlay permitted (v1: never) →
  `OverlayStrategy`; else `ManualCopyStrategy`.
- `RootStrategy` is **pure over `ShellRunner`** (command-building + outcomes are unit-tested without
  real root; `LibsuShellRunner` is the thin, un-unit-tested impl). Internals/mappings/gotchas:
  `plan_review_M-E.md`.
- Open editor (`ui.common.ApnEditor`): `Intent(Settings.ACTION_APN_SETTINGS)`, fallback
  `ACTION_WIRELESS_SETTINGS`, wrapped in try/catch `ActivityNotFoundException` (some OEMs block it).

## i18n
- `res/values/strings.xml` (en) + `res/values-ja/strings.xml` (ja) — every UI string localized; keep
  both in sync (Android lint `MissingTranslation` gates `just ci`).
- `ApnDateFormat`: en `yyyy-MM-dd HH:mm`, ja `yyyy年M月d日 HH:mm` (formatters cached; zone re-bound).
- Preset `label`/`notes` + region/carrier names resolve via `LocalizedText.resolve(tag)`.

## Gotchas & fragile areas
- **API-35 pinning:** AndroidX/Compose are held at the API-35 line (devShell ships platform/build-tools
  35; v1 locks compileSdk 35). Do **not** "upgrade to latest" without first bumping `flake.nix` to
  compileSdk/build-tools 36. Lint "newer version available" warnings are expected.
- **`libsu` via JitPack** (scoped repo in `settings.gradle.kts`) — F-Droid must build it from source.
- **Root apply:** opt-in probe only; verifies the write; deletes the matching row before re-insert;
  SQL-escaped WHERE (the delete is destructive). Full detail: `plan_review_M-E.md`.
- **Checklist checkboxes are ephemeral** (not persisted) — the app can't verify the device APN.
- **`displayName()` enum labels are intentionally English/acronym** (mirror the system APN-editor
  spinners) — the only non-`stringResource` user-facing strings.
- **Lint policy:** single line length 140 across ktlint (`.editorconfig`) + detekt
  (`config/detekt/detekt.yml`, which relaxes naming/length/`TooManyFunctions` for `@Composable`).
  `just test` linters are non-fatal (fast iteration); **`just ci` is fatal** — the real gate.

## Tech stack (pinned via `gradle/libs.versions.toml`)
- AGP 8.13.2 · Kotlin 2.1.21 (+ compose + serialization plugins) · Gradle 8.14.4 (committed wrapper).
- Compose BOM → material3, ui, activity/navigation/lifecycle-viewmodel-compose, material-icons-**core**.
- datastore-preferences · kotlinx-serialization-json · kotlinx-coroutines-android · `libsu:core`.
- Tests: junit, turbine, kotlinx-coroutines-test (JVM); androidx.test ext, espresso-intents,
  compose ui-test (instrumented).

## Testing & coverage
- `just ci` (`./gradlew test` + ktlint + detekt + `lint`, fatal) — 56 JVM tests.
- `just emu-test` — instrumented (nav, copy, heart toggle, APN-editor intent) on `apnhelper`.
- Every layer has tests. **Intentionally not unit-tested:** `LibsuShellRunner` (manually verified —
  `plan_review_M-E.md`), thin `AssetPresetRepository`.

## Tech debt / TODO (deferred, post-v1)
- **Self-healing watcher** — additive behind `ApplyStrategy` (the reason that seam exists).
- **Overlay** full implementation.
- **More regions/carriers** via data-only PRs (no restructuring).
- **UI:** favoriting prepends the ★ section above the current scroll offset — consider auto-scroll-to-top
  on first favorite.
- **i18n:** localize `displayName()` (`None`→`なし`, `PAP or CHAP`→`PAP または CHAP`) for exact JP
  spinner parity (only these differ from the acronyms).
- **CI/release:** add Nix-store caching (FlakeHub/Cachix) to skip the SDK re-fetch; optionally port CI
  to `ubuntu` by extending `flake.nix` to `x86_64-linux`; consider v3 APK signing; F-Droid `fdroiddata`
  recipe (build `libsu` from source); back up the signing keystore.
