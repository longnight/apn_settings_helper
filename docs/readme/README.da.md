# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | Dansk | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper er en lille open source Android-app til at gendanne telefonens APN-indstillinger for mobildata fra verificerede presets. APN er den indstilling, telefonen bruger til mobildata og MMS for dit SIM-kort.

Appen er til MVNO-/budget-SIM-brugere, rejsende og ulåste telefoner med lokal prepaid SIM. Japan dækkes først, og flere regioner kan tilføjes.

## For brugere

Mobildata kan stoppe, hvis telefonen mister eller mangler korrekt APN. Manuel indtastning er besværlig, fordi værdierne er lange og udbydernes vejledninger varierer. Appen hjælper dig med at vælge et preset, kopiere værdierne, åbne Androids APN-editor og indsætte dem. Android tillader ikke normal apps at ændre APN i baggrunden; standardflowet er derfor guidet manuel opsætning. Rootede telefoner kan valgfrit anvende direkte.

### Kan

- Vise verificerede APN-presets.
- Kopiere APN-værdier med et tryk.
- Vise hvilke dropdown-værdier der skal vælges.
- Åbne systemets APN-indstillinger, når Android tillader det.
- Vise et flydende hjælpepanel over APN-editoren.
- Anvende APN direkte på rootede telefoner efter opt-in.

### Kan ikke

- Automatisk ændre APN på en normal telefon uden root.
- Sikkert vide hvilken APN telefonen faktisk bruger.
- Løse SIM-, abonnement-, operatør- eller enhedsblokeringer.

"Mark as in use" er kun en note i appen.

### Download, brug og privatliv

Download APK fra [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Android 8.0+ understøttes. Screenshots og kort video kommer senere.

1. Åbn appen.
2. Vælg udbyder og preset.
3. Kopiér APN-felterne.
4. Tryk "Open system APN editor".
5. Opret/rediger APN i Android Settings.
6. Indsæt værdier, vælg dropdowns, gem og vælg APN.
7. Brug eventuelt "Mark as in use".

"Float over the APN editor" viser værdier og kopiknapper over editoren, men udfylder ikke automatisk.

Ingen netværksadgang, konti, annoncer, sporing eller baggrundstjeneste. Overlay og root er valgfrie.

Mangler eller fejl i udbyderdata: åbn issue på [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Angiv land, udbyder, SIM-linje/abonnement/netværk, officiel APN-link og forkert/manglende værdi. Undgå personlige data.

Se [CONTRIBUTING.md](../../CONTRIBUTING.md) for at tilføje presets.

## For udviklere og bidragydere

`v1.3.0` er udgivet. Preset-liste, detaljeskærm, manuel kopiering, valgfri root, valgfrit overlay og engelsk/japansk lokalisering er implementeret. Appen er MIT-licenseret, FOSS-orienteret og uden GMS.

### Platform og stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` til root
- Modul: `:app`

Dependency-versioner: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Udvikling

Dev-miljøet er en pure-Nix flake til Apple Silicon (`aarch64-darwin`) med JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt og `just`. Docs/presets kan redigeres uden miljøet.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Kommando | Formål | Emulator |
|---|---|---|
| `just test` | JVM tests + ikke-fatal lint | Nej |
| `just ci` | Strengt CI-check | Nej |
| `just fmt` | Kotlin-formattering | Nej |
| `just emu-test` | Instrumented Android tests | Ja |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Presets ligger i `app/src/main/assets/presets.json`; tjek mod officiel APN-side og tilføj `source` / `lastVerified`. Schema og PR-checkliste: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Kør `just ci` før PR hvis muligt. Påstå ikke stille APN-anvendelse uden root, tilføj ikke AccessibilityService auto-fill, probe ikke root før opt-in, og hold engelske/japanske UI-strenge synkroniseret.

Version: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid skal bygge `libsu` fra source.

### Licens

[MIT](../../LICENSE).
