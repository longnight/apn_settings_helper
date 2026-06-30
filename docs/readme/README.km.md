# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | ភាសាខ្មែរ | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper គឺជា​កម្មវិធី Android ប្រភពបើកចំហតូចមួយ សម្រាប់ស្ដារការកំណត់ APN ទិន្នន័យចល័តពី preset ដែលបានផ្ទៀងផ្ទាត់។ APN គឺការកំណត់ដែលទូរស័ព្ទប្រើសម្រាប់ទិន្នន័យចល័ត និង MMS របស់ SIM។

## សម្រាប់អ្នកប្រើ

កម្មវិធីនេះសម្រាប់អ្នកប្រើ MVNO / SIM តម្លៃទាប អ្នកធ្វើដំណើរ និងទូរស័ព្ទ unlocked ដែលប្រើ SIM prepaid មូលដ្ឋាន។ ជប៉ុនត្រូវបានគាំទ្រមុន ហើយតំបន់ផ្សេងអាចបន្ថែមបាន។

បើទិន្នន័យចល័តឈប់ដំណើរការ ទូរស័ព្ទអាចបាត់ APN ត្រឹមត្រូវ។ កម្មវិធីជួយឱ្យជ្រើស preset ចម្លងតម្លៃ ហើយបិទភ្ជាប់ទៅក្នុង APN editor របស់ Android។ Android មិនអនុញ្ញាតឱ្យកម្មវិធីធម្មតាប្តូរ APN ដោយស្ងាត់ៗទេ ដូច្នេះលំហូរចម្បងគឺធ្វើដោយដៃ។ ទូរស័ព្ទ root អាចប្រើការអនុវត្តផ្ទាល់ជាជម្រើស។

### អាចធ្វើបាន និងមិនអាចធ្វើបាន

- បង្ហាញ preset APN ដែលបានផ្ទៀងផ្ទាត់។
- ចម្លងតម្លៃ APN ដោយចុចម្តង។
- ផ្តល់តម្លៃសម្រាប់ dropdown fields។
- បើកការកំណត់ APN របស់ប្រព័ន្ធពេល Android អនុញ្ញាត។
- អាចបង្ហាញ panel លើ APN editor។
- root apply ដំណើរការតែបន្ទាប់ពី opt-in នៅលើឧបករណ៍ root។
- មិនប្តូរ APN ដោយស្វ័យប្រវត្តិដោយគ្មាន root។
- មិនដឹងប្រាកដថា APN ណាកំពុងសកម្មពិតប្រាកដ។

"Mark as in use" គ្រាន់តែជា note ក្នុងកម្មវិធី។

### ទាញយក និងប្រើ

ទាញយក APK ពី [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases)។ ត្រូវការ Android 8.0+។ រូបថតអេក្រង់ និងវីដេអូខ្លីនឹងបន្ថែមពេលក្រោយ។

1. បើកកម្មវិធី។
2. ជ្រើស carrier និង preset។
3. ចម្លង APN fields។
4. ចុច "Open system APN editor"។
5. បង្កើត ឬកែ APN ក្នុង Android Settings។
6. បិទភ្ជាប់តម្លៃ កំណត់ dropdowns រក្សាទុក ហើយជ្រើស APN។
7. ប្រើ "Mark as in use" ប្រសិនបើចង់បាន។

"Float over the APN editor" បង្ហាញតម្លៃ និងប៊ូតុងចម្លង ប៉ុន្តែមិនបំពេញដោយស្វ័យប្រវត្តិទេ។

### ភាពឯកជន និងការរាយការណ៍

គ្មាន network, accounts, ads, tracking ឬ background service។ Overlay និង root ជាជម្រើស។

បើមាន carrier ខ្វះ ឬខុស សូមបើក issue: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues)។ សូមផ្តល់ប្រទេស carrier SIM/plan/network official APN link និងតម្លៃខុស។ កុំបង្ហាញទិន្នន័យផ្ទាល់ខ្លួន។

អាចបន្ថែម preset តាម [CONTRIBUTING.md](../../CONTRIBUTING.md)។

## សម្រាប់អ្នកអភិវឌ្ឍន៍

`v1.3.0` ត្រូវបានចេញផ្សាយ។ មាន preset list, detail, manual copy, optional root, optional overlay និង EN/JA localization។ អាជ្ញាបណ្ណ MIT, FOSS, គ្មាន GMS។

### Platform និង stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` សម្រាប់ root
- module `:app`

Dependency versions: [gradle/libs.versions.toml](../../gradle/libs.versions.toml)

### Development

Environment គឺ pure-Nix flake សម្រាប់ Apple Silicon (`aarch64-darwin`) មាន JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt និង `just`។ Docs និង presets អាចកែដោយមិនចាំបាច់ដំឡើង។

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Command | Purpose | Emulator |
|---|---|---|
| `just test` | JVM tests + non-fatal lint | No |
| `just ci` | strict CI check | No |
| `just fmt` | Kotlin format | No |
| `just emu-test` | Instrumented Android tests | Yes |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml)

Presets នៅ `app/src/main/assets/presets.json`; ពិនិត្យជាមួយ official APN page ហើយបន្ថែម `source` / `lastVerified`។ Schema និង checklist នៅ [CONTRIBUTING.md](../../CONTRIBUTING.md)។

មុន PR សូមរត់ `just ci` ប្រសិនបើអាច។ កុំអះអាង silent APN ដោយគ្មាន root, កុំបន្ថែម AccessibilityService auto-fill, កុំ probe root មុន opt-in និង sync EN/JA strings។

Version: [app/build.gradle.kts](../../app/build.gradle.kts)។ F-Droid ត្រូវ build `libsu` ពី source។

### License

[MIT](../../LICENSE).
