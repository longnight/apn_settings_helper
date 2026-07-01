# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | English | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper is a small open-source Android app for restoring your phone's mobile-data APN settings from verified presets. APN means Access Point Name: it is the setting your phone uses to connect mobile data and MMS for your SIM.

It is made for MVNO / budget-SIM users, travelers, and anyone putting a local prepaid SIM into an unlocked phone. Japan is covered first, and more regions can be added over time.

## For People Using the App

### Why this app exists

Sometimes mobile data stops working because the phone lost, changed, or never had the right APN setting for your SIM. Fixing that by hand is annoying: the values are long, the fields are easy to mix up, and every carrier writes the instructions a little differently.

This app gives you a cleaner way to do it: pick your carrier preset, copy each APN field, open Android's APN editor, paste the values, and save. Modern Android does not let normal apps silently change APN settings, so the normal flow is guided manual setup. Rooted phones can optionally use one-tap apply.

### What it can do

- Show verified APN presets bundled with the app.
- Switch the app's display language from a toolbar language menu (20 languages).
- Copy APN values with a tap.
- Tell you which dropdown values to choose, such as authentication type and APN protocol.
- Open the system APN settings screen when Android allows it.
- Float a small helper panel over the APN editor, if you grant "Display over other apps".
- On rooted phones only, apply an APN directly after you turn on the root option.

### What it cannot do

- It cannot silently change APN settings on a normal, non-rooted phone.
- It cannot know for sure which APN your phone is currently using.
- It cannot fix a SIM, plan, carrier lock, or phone model that blocks APN editing.

The "Mark as in use" button is only your own note inside the app. It does not read the phone's live network settings.

### Download and install

Download the APK from the [GitHub Releases page](https://github.com/longnight/apn_settings_helper/releases).

The app supports Android 8.0 and newer. On many phones, Android will ask you to allow installing apps from your browser or file manager before it installs the APK.

Screenshots and a short usage video will be added later.

### How to use it

1. Open APN Settings Helper.
2. Search for your carrier or choose it from the preset list.
3. Open the preset that matches your SIM line or network.
4. Copy the APN fields shown in the app.
5. Tap "Open system APN editor".
6. In Android Settings, create a new APN or edit the matching APN.
7. Paste each copied value into the matching field.
8. Set the dropdown fields shown by the app.
9. Save the APN in Android Settings and select it.
10. Return to the app and tap "Mark as in use" if you want a note for later.

If switching between the app and Android Settings is awkward, try "Float over the APN editor". The floating panel does not auto-fill anything; it keeps the values and copy buttons visible while you work.

### Privacy

- No network access.
- No accounts.
- No ads.
- No tracking.
- No background service.

The basic manual flow needs no special permission. Overlay and root are optional and only used after you choose them.

### Missing or wrong carrier settings?

Please open an issue: [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues)

Helpful reports include:

- Country or region.
- Carrier name.
- SIM line, plan, or network type.
- A link to the carrier's official APN instructions.
- Which value looks missing or wrong.

Please do not include private account details, phone numbers, SIM serial numbers, or screenshots that show personal information.

Developers can add carrier presets directly. See [CONTRIBUTING.md](../../CONTRIBUTING.md).

## For Developers and Contributors

### Project status

`v1.4.0` is released. The preset list, detail screen, manual copy flow, optional root apply, and optional floating helper are implemented, and the UI is localized into **20 languages** with an in-app language picker (a toolbar translate icon opens a right-side language drawer).

The app is MIT licensed, FOSS-oriented, and has no GMS dependency.

### Android support

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile SDK: 35
- Target SDK: 35

Normal Android phones use the manual flow. Root apply appears only after the user opts in and root access is detected. The overlay helper requires `SYSTEM_ALERT_WINDOW`.

### Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Compose Navigation
- DataStore Preferences
- kotlinx.serialization
- Kotlin coroutines
- `libsu` for root apply
- Single Android module: `:app`

Dependency versions are in [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Development environment

The development environment is a pure-Nix flake for Apple Silicon (`aarch64-darwin`). It is CLI-only and provides JDK 17, Android SDK 35, platform tools, an arm64 emulator, Gradle, ktlint, detekt, and `just`.

You can edit docs or preset data without installing this environment, but you will not be able to run the full Android build and checks locally.

### Build and run

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

### Tests and checks

| Command | What it does | Emulator required |
|---|---|---|
| `just test` | JVM tests plus ktlint, detekt, and Android lint with linters non-fatal | No |
| `just ci` | The strict CI gate | No |
| `just fmt` | Auto-format Kotlin with ktlint | No |
| `just emu-test` | Instrumented Android tests | Yes |

[.github/workflows/ci.yml](../../.github/workflows/ci.yml) runs `just ci` on every push and pull request.

### Adding or fixing presets

Preset changes are usually data-only. Edit `app/src/main/assets/presets.json`.

Each preset should be checked against the carrier's official APN page and should include `source` and `lastVerified` when possible.

The schema, field list, validation rules, and PR checklist are in [CONTRIBUTING.md](../../CONTRIBUTING.md).

### Pull requests

Before opening a PR, run `just ci` if possible. If you cannot run the Android toolchain locally, say that clearly in the PR and describe what you changed and checked.

Keep these product rules intact:

- Do not add a silent non-root APN apply claim.
- Do not add AccessibilityService-based auto-fill.
- Do not eagerly probe root.
- Do not imply "in use" means the app verified the live phone APN.
- Keep the canonical English (`values/`) and Japanese (`values-ja/`) UI strings in sync (the other 18 locales are translations).

### Release and packaging

- Versioning is in [app/build.gradle.kts](../../app/build.gradle.kts).
- Fastlane metadata lives under `fastlane/metadata/android/`.
- Release signing is optional and controlled by a gitignored `keystore.properties`.
- F-Droid builds must handle `libsu` from source because JitPack is not allowed there.

```properties
storeFile=keystore/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

```sh
./gradlew :app:assembleRelease
```

Without `keystore.properties`, the release APK is unsigned. That is expected for CI and F-Droid. Back up the signing key; losing it means you cannot ship updates under the same signing identity.

### License

[MIT](../../LICENSE).
