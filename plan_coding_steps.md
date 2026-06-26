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
- [ ] `SettingsStore` (DataStore): favorites `Set<String>`, lastApplied `{id, epochMillis}`; Flows + mutators
- [ ] **Tests:** favorite add/remove/toggle, lastApplied overwrite (JVM/Robolectric)
- **Acceptance:** state round-trips; `./gradlew test` green.

### M-D — UI: list + detail
- [ ] `PresetListScreen` + VM: grouping, ★ FAVORITES section, heart toggle, last-applied muted line
- [ ] `PresetDetailScreen` + VM: copyable fields, "set to X" checklist for dropdowns, open-APN-editor button, "Record this as applied"
- [ ] Navigation list→detail/{presetId}
- [ ] **Tests:** Compose UI (nav, copy button copies, favorite toggle); espresso-intents asserts `ACTION_APN_SETTINGS` fired
- **Acceptance:** `just emu-test` green; manual UI check on emulator (incl. ja locale).

### M-E — Apply strategies
- [ ] `ApplyStrategy` interface + `ManualCopyStrategy` + `ApplyStrategyResolver`
- [ ] `RootStrategy` via libsu (write carriers + set current); "Apply now" wired when root present; auto-record lastApplied
- [ ] `OverlayStrategy` left as documented stub (v1 off)
- [ ] **Tests:** resolver logic (unit); root path manually verified on emulator with `adb root` (document steps)
- **Acceptance:** manual users get copy/checklist flow; on rooted emulator, "Apply now" writes APN.

### M-F — i18n
- [ ] en + ja `strings.xml`; localize all UI; locale-aware date formatter util
- [ ] **Tests:** formatter (en/ja), no hardcoded user strings
- **Acceptance:** app fully localized; date formats match spec.

### M-G — Test harness + CI
- [ ] Confirm `just test` (unit+robolectric+lint) and `just emu-test` (instrumented) cover all layers
- [ ] (optional) GitHub Actions: unit+lint on PR (emulator job optional/manual)
- **Acceptance:** one command runs the suite; documented in README.

### M-H — Polish & release prep
- [ ] App icon differentiated from the existing "APN Settings" app
- [ ] Per-locale store listing copy (en + **ja**: 格安SIM / APN設定 keywords); F-Droid fastlane metadata structure
- [ ] `README.md` (build/run/contribute, add-a-preset guide) + `LICENSE` (MIT)
- [ ] Versioning (versionCode/versionName); F-Droid build recipe notes (jitpack libsu caveat)
- **Acceptance:** installable APK + complete store/F-Droid metadata; contributors can add presets via PR.

---

## How to start (fresh session)
> **Resume point (2026-06-27): M-A + M-B are DONE, tested, and committed to `main`**
> (`f668aab` scaffold, `abda11b` preset data — local commits, not pushed).
> **Resume at the first unchecked box → M-C (persistence: favorites + last-applied).**
> Read `AGENTS.md` (product) + this file first. App layout already exists under
> `app/src/main/kotlin/io/github/ln/apnsettingshelper/` (`domain.model`, `data.preset`, `ui.*`, `MainActivity`).

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
