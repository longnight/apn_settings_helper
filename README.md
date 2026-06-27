# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A small, open-source Android app that makes restoring a phone's **APN (Access Point
Name)** settings fast and reliable — for MVNO users and travellers/expats putting a
local prepaid SIM in an unlocked phone. Curated, verified APN presets (Japan first,
extensible to any region); pick one and apply it.

> **Status:** v1 in development. The core flows (preset browse → detail, manual copy,
> opt-in root apply) and English + Japanese localization are done; remaining work is
> test/CI hardening (M-G) and release polish (M-H). See
> [`plan_coding_steps.md`](plan_coding_steps.md) for the milestone tracker.

## Why this exists

Modern Android blocks ordinary apps from writing APN settings: `WRITE_APN_SETTINGS`
is `signature|privileged`, so there is **no silent "apply" for a normal app**. This
app works *with* that constraint:

- **Everyone (zero permissions):** per-field **copy** buttons + a "set this dropdown
  to X" checklist, plus a button that opens the system APN editor. You paste the
  values in field by field.
- **Rooted devices (opt-in):** a **One-tap apply** toggle that, only when you turn it
  on, uses `su` to write the preset into the telephony provider and select it. Nothing
  runs in the background; the app is invisible until you open it.

Favorites (any number) and a passive "last applied" note round out the list UI.
Fully localized in **English** and **Japanese** (labels + date formats).

## Tech

Kotlin · Jetpack Compose + Material 3 · DataStore · kotlinx.serialization ·
[`libsu`](https://github.com/topjohnwu/libsu) (root) · single `:app` module ·
min SDK 26 / compile+target SDK 35 · FOSS-only deps (no GMS) — targets F-Droid +
GitHub APK, kept Play-compatible. MIT licensed.

## Development

The dev environment is a **pure-Nix flake** (Apple Silicon / `aarch64-darwin`,
CLI-only — no Android Studio). It pins JDK 17, the Android SDK (platform/build-tools
35, platform-tools), Gradle, an arm64 emulator + `apnhelper` AVD, ktlint, detekt, and
`just`. Full setup notes: [`plan_implement_steps.md`](plan_implement_steps.md).

```sh
# Enter the dev shell (direnv auto-loads on `cd`, or run it explicitly):
nix develop

# Build the debug APK:
./gradlew :app:assembleDebug

# Run on the emulator:
just emu-create                 # one-time: create the apnhelper AVD
just emu                        # boot it (windowed)
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

Non-interactive shells don't auto-activate the flake; prefix commands with
`nix develop --command ...` (see `plan_coding_steps.md` → "How to start").

## Testing

One command runs the whole non-emulator suite. Recipes are in the [`justfile`](justfile):

| Command | What it runs | Needs an emulator? |
|---|---|---|
| `just test` | JVM unit tests + ktlint + detekt + Android lint (linters **non-fatal** — for fast local iteration) | No |
| `just ci` | Same checks, but **fatal on any violation** — the exact gate CI runs | No |
| `just fmt` | `ktlint --format` (auto-fix) | No |
| `just emu-test` | Instrumented Compose/espresso tests (`connectedAndroidTest`) | Yes (`apnhelper`) |

**Coverage** — every layer has tests:

- `domain.apply` — Manual / Root / Overlay strategies + the resolver (`RootStrategy`
  is pure over a `ShellRunner` seam, so the `content insert/query/delete` command
  shapes and outcomes are unit-tested without real root).
- `data.preset` — JSON parse/validate/group + a test that loads the bundled
  `presets.json` and spot-checks verified APNs.
- `data.store` — DataStore favorites + last-applied round-trips.
- `ui.*` — list/detail ViewModels (Turbine) + the locale-aware date formatter;
  instrumented tests cover navigation, clipboard copy, the favorite toggle, and that
  the APN-editor intent fires.

Intentionally not unit-tested: `data.root.LibsuShellRunner` (the thin libsu binding —
verified manually on a rooted emulator via `adb root`; steps in `plan_review_M-E.md`)
and thin Android wrappers like `AssetPresetRepository`.

## Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs `just ci` on every push
and PR to `main`. It uses an **arm64 macOS runner** with Nix so CI executes the same
flake-pinned toolchain as local development (the declarative SDK means no license
acceptance step). Instrumented tests are **not** run in CI (the AVD is arm64 and
emulator runs on hosted runners are flaky) — run `just emu-test` locally instead.

## Contributing

Adding or correcting a preset is a **data-only** change to
`app/src/main/assets/presets.json` (region → carrier → preset; include `source` +
`lastVerified`). A full contributor guide and the add-a-preset walkthrough land with
the v1 release (M-H).

## License

[MIT](LICENSE).
