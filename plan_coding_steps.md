# APN Settings Helper — Coding Plan (app implementation)

> **READ THIS FIRST in a fresh session.** This is the self-contained implementation plan for the
> Android app. Pair it with `AGENTS.md` (product design + decisions) and `plan_implement_steps.md`
> (the dev-environment setup, already DONE). Tick the checkboxes as you go so progress survives
> session restarts.
>
> **Prerequisite (already complete):** a pure-Nix Android devShell exists. Enter it before any work:
> `cd` into the repo (direnv auto-loads) **or** `nix develop`. It provides JDK 17, Android SDK
> (platform 35, build-tools 35, platform-tools), a native arm64 emulator + `apnhelper` AVD
> (`google_apis/arm64-v8a`, `adb root` works), Gradle 8.14.4, ktlint, detekt, kotlin-language-server,
> just. See `plan_implement_steps.md`. **No Android Studio — CLI only.**

## Conventions
- `[ ]` todo · `[x]` done · `[~]` in progress · `[!]` blocked (note why).
- After finishing a step, tick it and append `→ note (date)`.
- Resume = first unchecked box. Milestones are ordered; within a milestone, write tests alongside code.
- Run everything inside the devShell. Use `just` recipes (defined in `justfile`).

## Locked decisions (v1)
- **applicationId / package root:** `io.github.ln.apnsettingshelper`
- **minSdk 26** (Android 8.0) · **compileSdk 35** · **targetSdk 35**
- **Preset breadth:** broad Japan coverage — 10+ MVNOs (see M-B list)
- **Module layout:** single `:app` module, clean package layering (no multi-module for v1)
- **Language/UI:** Kotlin 2.x · Jetpack Compose + Material3 · Compose Navigation
- **Data:** presets bundled as a JSON asset, parsed with kotlinx.serialization
- **Persistence:** Jetpack DataStore (Preferences)
- **Root:** `libsu` (Apache-2.0), opt-in, behind an interface
- **Distribution:** F-Droid + GitHub APK first (FOSS-only deps; no GMS/Firebase), keep Play-compatible
- **License:** MIT
- **Testing:** JUnit · Robolectric · Turbine · kotlinx-coroutines-test · Compose UI Test · espresso-intents · ktlint · detekt

## Tech stack (starting versions — verify latest stable in the devShell, pin via version catalog)
- Kotlin `2.1.x` (+ `org.jetbrains.kotlin.plugin.compose`, `kotlin-plugin-serialization`)
- AGP `8.9.x` (must be compatible with the devShell Gradle 8.14.x — bump AGP if Gradle complains)
- Compose BOM `2025.x` → material3, ui, ui-tooling(-preview), activity-compose, navigation-compose, lifecycle-viewmodel-compose
- `androidx.datastore:datastore-preferences`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- Root: `com.github.topjohnwu.libsu:core` + `:service` (jitpack; F-Droid builds from source — flag in metadata)
- Tests: `junit:junit`, `org.robolectric:robolectric`, `app.cash.turbine:turbine`,
  `org.jetbrains.kotlinx:kotlinx-coroutines-test`, `androidx.compose.ui:ui-test-junit4`,
  `androidx.compose.ui:ui-test-manifest` (debug), `androidx.test.ext:junit`,
  `androidx.test.espresso:espresso-intents`
- Use **Gradle Kotlin DSL** + **`gradle/libs.versions.toml`** version catalog. Commit the `./gradlew` wrapper.

## Architecture & package layout (`io.github.ln.apnsettingshelper`)
```
.data
  preset/     PresetDto, PresetSerialization, PresetRepository (loads assets/presets.json, groups)
  store/      SettingsStore (DataStore: favorites:Set<String>, lastApplied:{id,epochMillis})
.domain
  model/      Preset, Carrier, Region, ApnField (+ which fields are dropdown vs copyable)
  apply/      ApplyStrategy (interface), ManualCopyStrategy, RootStrategy, OverlayStrategy(stub),
              ApplyStrategyResolver, ApplyOutcome
.ui
  list/       PresetListScreen + PresetListViewModel  (favorites section, heart toggle, last-applied note)
  detail/     PresetDetailScreen + PresetDetailViewModel (copy fields, "set to X" checklist,
              open-APN-editor button, record-applied, apply-now-if-root)
  theme/      Material3 theme (light/dark), Type, Color
  nav/        NavHost (list -> detail/{presetId})
  common/     CopyableField, ChecklistField composables, date formatting
MainActivity (single-activity, Compose)
```
**Keep `ApplyStrategy` as the single seam** so a future opt-in self-healing watcher is additive (AGENTS.md).

## Preset data model (`assets/presets.json`)
Top level grouped by region → carrier → presets. Per-preset fields mirror the system APN editor /
telephony `carriers` columns. Schema (camelCase in JSON; map to Android columns in RootStrategy):
```jsonc
{
  "schemaVersion": 1,
  "regions": [{
    "code": "JP", "name": {"en": "Japan", "ja": "日本"},
    "carriers": [{
      "id": "his-mobile", "name": {"en": "HIS Mobile", "ja": "HISモバイル"},
      "presets": [{
        "id": "his-mobile-default",
        "label": {"en": "HIS Mobile (data+voice)", "ja": "HISモバイル"},
        "apn": "vmobile.jp",
        "username": "vmobile@uqmobile", "password": "0000",
        "mcc": "440", "mnc": "10",
        "authType": "CHAP",                 // dropdown: NONE|PAP|CHAP|PAP_OR_CHAP
        "protocol": "IPV4V6",               // dropdown: IPV4|IPV6|IPV4V6
        "roamingProtocol": "IPV4V6",        // dropdown
        "mvnoType": "NONE",                 // dropdown: NONE|SPN|IMSI|GID
        "mvnoValue": "",
        "apnType": "default,supl,mms",
        "proxy": "", "port": "",
        "mmsc": "", "mmsProxy": "", "mmsPort": "",
        "server": "",
        "notes": {"en": "", "ja": ""},
        "source": "carrier site", "lastVerified": "2026-06-26"
      }]
    }]
  }]
}
```
- **Dropdown fields** (`authType`, `protocol`, `roamingProtocol`, `mvnoType`) render as **"set to X" checklist items** in the UI, NOT copy buttons. All other non-empty fields render as **per-field copy buttons**.
- Validate on load (required: `id`, `apn`, `mcc`, `mnc`; enums in range; ids unique).

## Persistence (DataStore Preferences)
- `favorites`: `Set<String>` of presetIds (multiple allowed).
- `lastApplied`: `presetId: String?` + `lastAppliedAt: Long?` (epoch millis). Single slot, overwritten each apply.
- Expose as `Flow`s; toggling a favorite / recording applied updates the store.

## Apply strategy
```kotlin
enum class ApplyTier { MANUAL, OVERLAY, ROOT }
sealed interface ApplyOutcome { object ManualGuidance: ApplyOutcome; data class Applied(...): ApplyOutcome; data class Failed(val msg:String): ApplyOutcome }
interface ApplyStrategy { val tier: ApplyTier; suspend fun apply(preset: Preset): ApplyOutcome }
```
- **ManualCopyStrategy** (default, zero perms): returns `ManualGuidance`; UI drives copy + checklist + "open system APN editor".
- **RootStrategy** (`libsu`): inserts a row into `content://telephony/carriers` and sets it current
  (preferapn). Map camelCase → carriers columns carefully. Guard heavily; opt-in. On success records lastApplied.
- **OverlayStrategy**: `SYSTEM_ALERT_WINDOW`, floats values over the editor. **v1 stub/deferred** (off by default per AGENTS.md).
- **ApplyStrategyResolver**: root available? (`Shell.getShell().isRoot`) → ROOT offered; `Settings.canDrawOverlays` → OVERLAY; else MANUAL.

## Open the system APN editor
- `Intent(android.provider.Settings.ACTION_APN_SETTINGS)` (constant `"android.settings.APN_SETTINGS"`).
- Wrap in try/catch `ActivityNotFoundException`; some OEMs block it for non-system apps — fall back to
  `ACTION_WIRELESS_SETTINGS` and show guidance. Cover this with an espresso-intents test (intent fired).

## UI behaviors (from AGENTS.md)
- **PresetList:** grouped region→carrier→preset; a top **★ FAVORITES** section (faved rows float up);
  a **heart** toggle per row (any number of favorites); on the last-applied row, a muted
  **"last applied YYYY-MM-DD HH:MM"** line (localized). Favorites and last-applied are independent.
- **PresetDetail:** per-field **copy buttons**; dropdown fields as **"set to X" checklist** items;
  **"Open system APN editor"** button; **"Record this as applied"** button (manual users);
  **"Apply now"** button only when root is available (auto-records lastApplied).
- Material3 light/dark. Wording: use **"settings"** not "configs" in UI copy.

## i18n
- `values/strings.xml` (en) + `values-ja/strings.xml` (ja). All user text localized.
- Date format localized: en `2026-06-26 14:30`, ja `2026年6月26日 14:30` (a `DateTimeFormatter` util keyed off locale). Unit-test the formatter.

---

## Milestones (tick as you go)

### M-A — Project scaffold + end-to-end toolchain proof
- [x] Gradle KTS project: `settings.gradle.kts`, root + `:app` `build.gradle.kts`, `gradle/libs.versions.toml`, committed `./gradlew` wrapper → wrapper pinned to Gradle 8.14.4 (2026-06-27)
- [x] `:app` config: applicationId `io.github.ln.apnsettingshelper`, minSdk 26/compileSdk 35/targetSdk 35, Compose enabled, Kotlin compose plugin → AGP 8.13.2 + Kotlin 2.1.21 (2026-06-27)
- [x] `AndroidManifest.xml` (no special perms yet), `MainActivity` (single-activity Compose), Material3 theme, empty NavHost (list→detail) → placeholder list/detail screens; vector adaptive launcher icon; en `strings.xml` (2026-06-27)
- [x] **Build & run end-to-end:** `./gradlew assembleDebug` → `just emu` → `adb install` → launch; app shows on emulator → built via wrapper, installed on `apnhelper`, `MainActivity` confirmed foreground, list→detail nav verified by screenshot (2026-06-27)
- **Acceptance:** APK builds and launches on `apnhelper`. _(This is the real end-to-end env proof the setup didn't do yet.)_ → ✅ **MET (2026-06-27)**
- **Notes (M-A):** AndroidX/Compose deliberately pinned at the **API-35 line** (devShell ships only platform-35/build-tools-35 and v1 locks compileSdk 35; the mid-2026 latest demands compileSdk 36). Build tooling kept current (AGP 8.13.2 + Kotlin 2.1.21, natural pair for Gradle 8.14.4). Android lint's "newer version available" notices on these pins are expected/benign. Added Compose-aware static analysis: `.editorconfig` (ktlint; PascalCase `@Composable` allowed) + `config/detekt/detekt.yml`; updated `just lint`, added `just fmt`. ktlint + detekt + `./gradlew lintDebug` all green (0 errors, 23 benign warnings).

### M-B — Preset model + bundled data
- [x] Domain models (`Preset`, `Carrier`, `Region`, enums) + kotlinx.serialization DTOs → `domain.model` (pure) + `data.preset` DTOs + mapper; enums `AuthType/ApnProtocol/MvnoType`, `LocalizedText` (2026-06-27)
- [x] `assets/presets.json` — **broad JP** (12 carriers / 16 presets): HIS Mobile (Docomo+SoftBank), IIJmio, mineo (D/A/S), OCN モバイル ONE, Rakuten Mobile, LINEMO, povo, NUROモバイル, BIGLOBEモバイル, y.u mobile, イオンモバイル (au+Docomo), J:COM MOBILE → **each verified against the carrier's official APN page** (online research); `source`+`lastVerified` per preset (2026-06-27)
- [x] `PresetRepository` loads + validates + groups → `PresetSerialization` (pure parse/validate/group) + `AssetPresetRepository` (Android assets). Validation: required id/apn/mcc/mnc, mcc/mnc format, unique preset/carrier/region ids, enum range + required-field presence via kotlinx, schemaVersion (2026-06-27)
- [x] **Tests (JVM):** JSON parse, schema validation (unique ids, required fields, enum ranges), grouping → 13 tests incl. a `BundledPresetsTest` that loads the real `presets.json` from the test classpath and spot-checks verified APNs (2026-06-27)
- **Acceptance:** `./gradlew test` green; repository returns grouped presets. → ✅ **MET** — `just test` green (13/13 unit tests, ktlint+detekt+`lint` 0 errors) (2026-06-27)
- **Notes (M-B):** Stale data caught by research — the old HIS example (`vmobile.jp`/`vmobile@uqmobile`) is now `dm.jplat.net`/`his@his`; y.u mobile is `yumobile.jp` (not `ymobile.jp`); NURO is `so-net.jp`. au-line MCC/MNC set to 440/51, Docomo 440/10, SoftBank 440/20, Rakuten 440/11 (editor usually auto-fills; noted per preset). Aligned detekt `MaxLineLength` to `.editorconfig` (140). `Json { ignoreUnknownKeys = true }` so additive schema fields won't break old builds.

### M-C — Persistence (favorites + last-applied)
- [x] `SettingsStore` (DataStore): favorites `Set<String>`, lastApplied `{id, epochMillis}`; Flows + mutators → `data.store.SettingsStore` (interface) + `DataStoreSettingsStore` (Preferences DataStore); `LastApplied` domain model; `setFavorite`/`toggleFavorite`/`recordApplied`; injectable clock; `.from(context)` factory (2026-06-27)
- [x] **Tests:** favorite add/remove/toggle, lastApplied overwrite (JVM/Robolectric) → 9 pure-JVM tests (`kotlinx-coroutines-test` + `TemporaryFolder`, no Robolectric needed): defaults, set add/remove, idempotent set, toggle on/off, multiple independent favorites, lastApplied null/record/overwrite (fixed clock), favorites⊥lastApplied (2026-06-27)
- **Acceptance:** state round-trips; `./gradlew test` green. → ✅ **MET** — `just test` green (22 unit tests, 0 fail; ktlint + detekt + android lint 0 errors) (2026-06-27)
- **Notes (M-C):** Round-trip proven by write→read-back through the store's `Flow`s (DataStore serializes to / re-reads from disk). Clock injected (`now: () -> Long = System::currentTimeMillis`) so `recordApplied` timestamps are deterministic in tests. `data.catch { IOException -> emptyPreferences() }` so a corrupt file yields empty state, not a crash. Added deps: `androidx.datastore.preferences`, `kotlinx.coroutines.android` (impl); `kotlinx.coroutines.test` (test). DataStore tests run with `runTest(UnconfinedTestDispatcher())` + `backgroundScope` as the store's host scope.

### M-D — UI: list + detail
- [x] `PresetListScreen` + VM: grouping, ★ FAVORITES section, heart toggle, last-applied muted line → `PresetListViewModel` combines repo + favorites + lastApplied → `PresetListUiState` (favorited rows float OUT of their carrier group into the ★ Favorites section; rest grouped region→carrier). Stateless `PresetListContent` + VM wrapper (2026-06-27)
- [x] `PresetDetailScreen` + VM: copyable fields, "set to X" checklist for dropdowns, open-APN-editor button, "Record this as applied" → `PresetDetailViewModel` (load by id, favorite toggle, recordApplied, notFound state); copy fields = non-blank non-dropdown; checklist = 4 dropdowns via `displayName()` (e.g. "PAP or CHAP", "IPv4/IPv6"); notes + last-applied line. Verified on emulator incl. ja preset labels + ja date (2026-06-27)
- [x] Navigation list→detail/{presetId} → existing `AppNavHost` unchanged (screens' new `viewModel`/`modifier` args are defaulted); VMs provided via `viewModelFactory{initializer{...}}` reading `APPLICATION_KEY` → `ApnApplication.graph` (manual DI, no Hilt) (2026-06-27)
- [x] **Tests:** Compose UI (nav, copy button copies, favorite toggle); espresso-intents asserts `ACTION_APN_SETTINGS` fired → 7 instrumented (list content + click + heart toggle; detail render + clipboard copy; espresso-intents APN_SETTINGS stubbed; full-app `MainActivity` nav) + 14 new JVM (2 VMs via fakes/Turbine + `ApnDateFormat` en/ja) (2026-06-27)
- **Acceptance:** `just emu-test` green; manual UI check on emulator (incl. ja locale). → ✅ **MET** — 34 JVM + 7 instrumented green; ktlint/detekt/lint clean; emulator screenshots verified list (favorites/groups/heart), detail (copy/checklist/notes/record), last-applied line, and ja resolution (日本/HISモバイル/2026年6月27日) (2026-06-27)
- **Notes (M-D):** Manual DI via `AppGraph` (held by `ApnApplication`, registered in manifest) — `PresetRepository` parsed once, `SettingsStore` from `DataStoreSettingsStore.from`. Added `material-icons-core` (heart/star/back; FOSS, small — NOT the huge `-extended`), `turbine` (test), `espresso-intents` (androidTest). `ApnDateFormat` (locale-aware en `yyyy-MM-dd HH:mm` / ja `yyyy年M月d日 HH:mm`) pulled forward from M-F since the last-applied line needs it; M-F still owns `values-ja/strings.xml` (UI chrome is en-only for now). Checklist checkboxes are ephemeral (not persisted) — app can't verify the device APN. **Known minor polish (defer to M-H):** favoriting prepends the ★ Favorites section above the current scroll offset, so on a scrolled list the new section sits just above the fold (LazyColumn key-stable scroll) — consider auto-scroll-to-top on first favorite.

### M-E — Apply strategies
- [x] `ApplyStrategy` interface + `ManualCopyStrategy` + `ApplyStrategyResolver` → `domain.apply`: `ApplyTier`/`ApplyOutcome`/`ApplyStrategy`; pure `ManualCopyStrategy` (returns `ManualGuidance`); `ApplyStrategyResolver` (root→overlay→manual, overlay off in v1) (2026-06-27)
- [x] `RootStrategy` via libsu (write carriers + set current); "Apply now" wired when root present; auto-record lastApplied → `RootStrategy` is pure logic over a `ShellRunner` seam (`content insert` into `content://telephony/carriers` + best-effort `preferapn`); `data.root.LibsuShellRunner` is the libsu impl; detail VM exposes `canApplyRoot`/`applying`/`applyNow()`/`applyEvents`, shows "Apply now" only when root present, auto-records last-applied on success (2026-06-27)
- [x] `OverlayStrategy` left as documented stub (v1 off) → `OverlayStrategy` returns `Failed("not implemented in v1")`; tier kept so the resolver seam accounts for it (2026-06-27)
- [x] **Tests:** resolver logic (unit); root path manually verified on emulator with `adb root` (document steps) → 13 new JVM tests (Manual/Root/Resolver strategies via `FakeShellRunner` + detail-VM `applyNow`); root provider write verified on emulator (steps below) (2026-06-27)
- **Acceptance:** manual users get copy/checklist flow; on rooted emulator, "Apply now" writes APN. → ✅ **MET** — 47 JVM + 7 instrumented green; ktlint/detekt/lint clean; the `RootStrategy` `content insert` command shape verified against the real telephony provider via `adb root` (row written with `authtype=3`, `protocol=IPV4V6`; `_id` parsed for `preferapn`) (2026-06-27)
- **Notes (M-E):** `RootStrategy` decoupled from libsu via `ShellRunner` so its command-building + outcome logic are fully unit-tested without real root; only the thin `LibsuShellRunner` is un-unit-tested (manually verified). Enum→provider mappings: authtype NONE/PAP/CHAP/PAP_OR_CHAP→0/1/2/3, protocol IPV4/IPV6/IPV4V6→IP/IPV6/IPV4V6, mvno spn/imsi/gid; `numeric`=mcc+mnc; `current:i:1`. Non-destructive (inserts; no delete) → repeated applies can create duplicate rows (note for M-H polish). **libsu via jitpack** (scoped repo in `settings.gradle.kts`); F-Droid must build it from source (flag in M-H metadata). **Root manual-verification steps (google_apis emulator):** `adb root` → run the `content insert --uri content://telephony/carriers --bind …` that `RootStrategy.buildInsertCommand` emits → `content query … --where "apn='…'"` shows the row → `content insert --uri content://telephony/carriers/preferapn --bind apn_id:i:<id>`. **Emulator caveats:** (1) `google_apis` gives `adb root` but NOT app-level `su`, so the in-app "Apply now" button does not appear on the emulator (libsu `isRoot`=false) — verify via `adb root` shell instead, or use a Magisk image for the in-app button; (2) `preferapn` selection only takes effect when the APN's MCC/MNC matches the active SIM, so on the emulator's T-Mobile (310/260) SIM selecting a JP (440/xx) APN is correctly ignored — the row write itself still succeeds.

### M-F — i18n
- [x] en + ja `strings.xml`; localize all UI; locale-aware date formatter util → `values-ja/strings.xml` translates all 44 keys (incl. the M-E.1 root/apply additions); chrome is fully `stringResource`-driven; `ApnDateFormat` (en `yyyy-MM-dd HH:mm` / ja `yyyy年M月d日 HH:mm`) pre-existed from M-D (2026-06-27)
- [x] **Tests:** formatter (en/ja), no hardcoded user strings → `ApnDateFormatTest` (3) covers en/ja; grep confirms the only non-`stringResource` UI literals are the **intentionally technical** enum option labels in `ApnFieldDisplay.displayName()` (None/PAP/CHAP/IPv4/SPN… — mirror the system APN editor spinners, documented "not localized" since M-D) (2026-06-27)
- **Acceptance:** app fully localized; date formats match spec. → ✅ **MET** — 53 JVM tests green; ktlint/detekt/android-lint clean (no MissingTranslation / placeholder mismatch). Also folded in `plan_review_M-E.md` **P3** (failure toast → string resources; APN `name` column written via `label.resolve(languageTag)`). UI-chrome ja was verified at the resource/lint level (not re-run on the emulator this pass; ja preset+date resolution was already screenshot-verified in M-D).
- **Notes (M-F):** `app_name` localized to **APN設定ヘルパー** (launcher + title) — the *brand* stays neutral per AGENTS, but the display label localizes (設定 wording). Acronym field labels (APN/MCC/MNC/MMSC) repeat as-is in `values-ja` rather than `translatable="false"`, so lint sees a complete translation. **Open polish (defer):** `displayName()` values stay English/acronym; only `None`→`なし` would differ from the JP system editor — revisit if we want exact spinner parity.

### M-G — Test harness + CI
- [x] Confirm `just test` (unit+robolectric+lint) and `just emu-test` (instrumented) cover all layers → audited: every layer has tests (`domain.apply` Manual/Root/Overlay/Resolver — added `OverlayStrategyTest` to close the stub gap; `data.preset`, `data.store`, `ui.*` VMs + formatter; instrumented = nav/copy/heart/APN-intent). Intentional non-coverage documented (libsu `LibsuShellRunner`, thin `AssetPresetRepository`) (2026-06-27)
- [x] (optional) GitHub Actions: unit+lint on PR (emulator job optional/manual) → `.github/workflows/ci.yml` runs `just ci` on push/PR to `main`; added a **strict `just ci`** recipe (fatal linters, unlike the lenient `just test`) as the single gate. Instrumented job intentionally omitted (arm64 AVD ⇒ flaky on hosted runners; run `just emu-test` locally) (2026-06-27)
- **Acceptance:** one command runs the suite; documented in README. → ✅ **MET** — `just ci` is the one command (55 JVM tests + ktlint + detekt + Android lint, all green locally); `README.md` documents build/run/test/CI + the coverage map.
- **Notes (M-G):** CI runs on **`macos-14` (arm64)** because `flake.nix` only exposes a devShell for `aarch64-darwin`; it installs Nix and runs `nix develop --command just ci`, so CI = the exact flake-pinned local toolchain (declarative Nix SDK ⇒ **no `sdkmanager --licenses` step**). Gradle Maven cache via `actions/cache`; Nix-store caching deferred (magic-nix-cache is sunset — add FlakeHub/Cachix later to skip the SDK re-fetch). ⚠️ **The workflow could not be executed from this environment** (no way to trigger Actions) — it's YAML-valid and runs the locally-verified command, but **confirm the first run on GitHub** (runner Nix setup / SDK fetch time). Could later be ported to faster `ubuntu` runners by extending the flake to `x86_64-linux`.

### M-H — Polish & release prep
- [x] App icon differentiated from the existing "APN Settings" app → adaptive icon from M-A: white SIM-card-with-chip glyph on a distinctive **teal** (`#0B6E6E`) background (+ monochrome/themed variant). Distinct mark + colour vs the existing app; a designer pass is optional post-v1 (2026-06-27)
- [x] Per-locale store listing copy (en + **ja**: 格安SIM / APN設定 keywords); F-Droid fastlane metadata structure → `fastlane/metadata/android/{en-US,ja}/` with `title` / `short_description` (≤80) / `full_description` / `changelogs/1.txt`; ja copy leads with 格安SIM・APN設定. **Screenshots captured** at **1080×2400** (list + detail, en + ja) → `…/<locale>/images/phoneScreenshots/{1_list,2_detail}.png`, via a throwaway `pixel_6` AVD (project's `apnhelper` AVD untouched) (2026-06-27)
- [x] `README.md` (build/run/contribute, add-a-preset guide) + `LICENSE` (MIT) → `README.md` (overview/build/run/test/CI/release) created in M-G; `CONTRIBUTING.md` added with the full add-a-preset guide (schema, field table, validation rules, PR checklist); `LICENSE` (MIT) already present (2026-06-27)
- [x] Versioning (versionCode/versionName); F-Droid build recipe notes (jitpack libsu caveat) → `versionCode 1` / `versionName 1.0.0`; README "Releasing / packaging" documents the **libsu JitPack → build-from-source** F-Droid caveat, the fastlane layout, and signing (2026-06-27)
- **Acceptance:** installable APK + complete store/F-Droid metadata; contributors can add presets via PR. → ✅ **MET** — `assembleDebug` produces an installable `app-debug.apk` (v1.0.0); en+ja fastlane metadata in place (screenshots are the one remaining binary asset); `CONTRIBUTING.md` lets contributors add a preset via a data-only PR (validated by `BundledPresetsTest`). `just ci` green; APK builds.
- **Notes (M-H):** Closed the last three `plan_review_M-E.md` **P4** items (P4.2 root record-button, P4.3 field-divergence guard test, P4.4 formatter/title caching) — punch-list fully closed. No release signing config in-repo (F-Droid signs its own builds). The teal SIM-card icon was kept rather than re-cut blind; a visual emulator check / designer pass can refine it later.

---

## How to start (fresh session)
> **Resume point (2026-06-27): 🎉 M-A–M-H ALL DONE + M-E.1 hardening DONE — v1 is code-complete.**
> (M-A–M-G pushed to `origin/main`, CI verified green on GitHub; **M-H is committed locally but NOT yet
> pushed** — run `git push origin main` to sync, or ask the user.)
> `plan_review_M-E.md` punch-list is **fully closed** (P1+P2+P3+P4). 56 JVM + 7 instrumented tests;
> `just ci` green; `assembleDebug` produces a v1.0.0 APK.
> **No coding milestones remain.** Pre-store-submission tasks left (not code):
> 1. ✅ **Phone screenshots** — done: en + ja, list + detail, **1080×2400** in `…/images/phoneScreenshots/`.
> 2. **F-Droid:** submit an `fdroiddata` recipe that builds **libsu from source** (JitPack is disallowed;
>    see README → Releasing) — that metadata lives in fdroiddata, not this repo.
> 3. Optional: release signing for GitHub APKs; tag `v1.0.0`.
> Read `AGENTS.md` (product) + this file first. App layout already exists under
> `app/src/main/kotlin/io/github/ln/apnsettingshelper/` (`domain.model`, `domain.apply`, `data.preset`, `data.store`, `data.root`, `ui.list`, `ui.detail`, `ui.common`, `ui.nav`, `ui.theme`, `AppGraph`, `ApnApplication`, `MainActivity`); i18n in `app/src/main/res/values/` (en) + `values-ja/` (ja).

1. **Enter the devShell.**
   - Interactive terminal: `cd` into the repo (direnv auto-loads) or run `nix develop`.
   - ⚠️ **Non-interactive / automation shells** (e.g. one-shot tool calls): direnv does **not** auto-activate and `nix` is **not** on PATH. Run each command as:
     `source /nix/var/nix/profiles/default/etc/profile.d/nix-daemon.sh && cd <repo> && nix develop --command bash -c '<cmd>'`
     (flake eval is cached, ~2 s/call). Confirm `gradle`, `adb`, `$ANDROID_HOME`, JDK 17.
2. **Build / test** via the committed wrapper: `./gradlew :app:assembleDebug` · `just test` (unit + ktlint + detekt + android lint) · `just fmt` (ktlint autofix). Lint is Compose-aware via `.editorconfig` + `config/detekt/detekt.yml` (single line-length policy = 140).
3. **Emulator** (`apnhelper` AVD, google_apis arm64 ⇒ `adb root` works): `just emu` (windowed) or headless `emulator -avd apnhelper -no-window -no-audio -no-snapshot -gpu swiftshader_indirect`. Wait for boot with `adb wait-for-device && adb shell 'while [[ "$(getprop sys.boot_completed)" != "1" ]]; do sleep 1; done'`. Install+launch: `adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity`. Stop: `adb emu kill`.
4. **Version pinning reminder:** AndroidX/Compose are intentionally held at the **API-35 line** (devShell ships platform-35/build-tools-35; v1 locks compileSdk 35). Don't "upgrade to latest" without first bumping to compileSdk 36 in `flake.nix` — newer AndroidX requires it. Android lint's "newer version available" warnings are expected.
5. Keep ticking boxes here; **commit per milestone** (end commit messages with the Co-Authored-By line).

## Deferred (post-v1, keep the seam)
- Opt-in self-healing watcher (auto-detect APN loss + re-apply) — additive behind `ApplyStrategy`.
- OverlayStrategy full implementation.
- More regions/carriers via community PRs (data-only; no restructuring).
