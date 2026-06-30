# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | Norsk | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper er en liten Android-app med åpen kildekode for å gjenopprette APN-innstillinger for mobildata fra verifiserte forhåndsvalg. APN er innstillingen telefonen bruker for mobildata og MMS med SIM-kortet.

## For brukere

Appen er for MVNO-/billig-SIM-brukere, reisende og ulåste telefoner med lokalt kontantkort-SIM. Japan støttes først.

Hvis mobildata slutter å virke, kan telefonen ha mistet riktig APN. Appen hjelper deg å velge forhåndsvalg, kopiere verdier og lime dem inn i Androids APN-redigerer. Android lar ikke vanlige apper endre APN stille, så hovedflyten er manuell. Rootede telefoner kan bruke valgfri direkte anvendelse.

### Kan og kan ikke

- Viser verifiserte APN-forhåndsvalg.
- Kopierer APN-verdier med ett trykk.
- Gir verdier for dropdown-felt.
- Åpner systemets APN-innstillinger når Android tillater det.
- Kan vise et panel over APN-redigereren.
- Root apply virker bare etter opt-in på root-enhet.
- Endrer ikke APN automatisk uten root.
- Vet ikke sikkert hvilken APN som faktisk er aktiv.

"Mark as in use" er bare en notis i appen.

### Last ned og bruk

Last ned APK fra [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Krever Android 8.0+. Skjermbilder og kort video kommer senere.

1. Åpne appen.
2. Velg operatør og forhåndsvalg.
3. Kopier APN-feltene.
4. Trykk "Open system APN editor".
5. Opprett eller rediger APN i Android Settings.
6. Lim inn verdier, sett dropdowns, lagre og velg APN.
7. Bruk eventuelt "Mark as in use".

"Float over the APN editor" viser verdier og kopiknapper, men fyller ikke inn automatisk.

### Personvern og rapporter

Ingen nettverk, kontoer, annonser, sporing eller bakgrunnstjeneste. Overlay og root er valgfrie.

Feil eller manglende operatører: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Oppgi land, operatør, SIM/abonnement/nettverk, offisiell APN-lenke og feil verdi. Ikke publiser personlige data.

Forhåndsvalg kan legges til via [CONTRIBUTING.md](../../CONTRIBUTING.md).

## For utviklere

`v1.3.0` er utgitt. Forhåndsvalgsliste, detaljer, manuell kopiering, optional root, optional overlay og EN/JA-lokalisering er implementert. MIT, FOSS, ingen GMS.

### Plattform og stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` for root
- Modul `:app`

Dependency-versjoner: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Utvikling

Miljøet er en pure-Nix flake for Apple Silicon (`aarch64-darwin`) med JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt og `just`. Dokumentasjon og forhåndsvalg kan redigeres uten installasjon.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Kommando | Formål | Emulator |
|---|---|---|
| `just test` | JVM tests + non-fatal lint | Nei |
| `just ci` | Streng CI-sjekk | Nei |
| `just fmt` | Kotlin-formattering | Nei |
| `just emu-test` | Instrumented Android tests | Ja |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Presets: `app/src/main/assets/presets.json`; sjekk med offisiell APN-side og legg til `source` / `lastVerified`. Schema og checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Før PR, kjør `just ci` hvis mulig. Ikke lov stille APN uten root, ikke legg til AccessibilityService auto-fill, ikke sjekk root før opt-in, og synkroniser EN/JA-strenger.

Versjon: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid må bygge `libsu` fra source.

### Lisens

[MIT](../../LICENSE).
