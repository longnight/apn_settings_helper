# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | Bosanski | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper je mala Android aplikacija otvorenog koda za vraćanje APN postavki mobilnih podataka iz provjerenih preseta. APN je postavka koju telefon koristi za mobilne podatke i MMS preko SIM kartice.

## Za korisnike

Aplikacija je za MVNO / jeftine SIM korisnike, putnike i otključane telefone s lokalnom prepaid SIM karticom. Japan je prvi podržan.

Ako mobilni podaci prestanu raditi, telefon je možda izgubio ispravan APN. Aplikacija pomaže da izaberete preset, kopirate vrijednosti i zalijepite ih u Android APN editor. Android ne dozvoljava običnim aplikacijama da tiho mijenjaju APN, pa je osnovni tok ručni. Root telefoni mogu opciono primijeniti direktno.

### Može i ne može

- Prikazuje provjerene APN presete.
- Kopira APN vrijednosti jednim dodirom.
- Daje vrijednosti za dropdown polja.
- Otvara sistemske APN postavke kada Android dozvoli.
- Može prikazati panel iznad APN editora.
- Root apply radi samo nakon opt-in na root uređaju.
- Ne mijenja APN automatski bez root-a.
- Ne zna sigurno koji APN je stvarno aktivan.

"Mark as in use" je samo bilješka u aplikaciji.

### Preuzimanje i korištenje

Preuzmite APK sa [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Potreban je Android 8.0+. Screenshotovi i kratki video biće dodani kasnije.

1. Otvorite aplikaciju.
2. Izaberite operatera i preset.
3. Kopirajte APN polja.
4. Dodirnite "Open system APN editor".
5. Kreirajte ili uredite APN u Android Settings.
6. Zalijepite vrijednosti, podesite dropdown polja, sačuvajte i izaberite APN.
7. Po želji koristite "Mark as in use".

"Float over the APN editor" prikazuje vrijednosti i dugmad za kopiranje, ali ne popunjava automatski.

### Privatnost i prijave

Nema mreže, računa, reklama, praćenja ili pozadinskog servisa. Overlay i root su opcionalni.

Greške ili nedostajući operateri: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Navedite državu, operatera, SIM/paket/mrežu, službeni APN link i pogrešnu vrijednost. Ne objavljujte lične podatke.

Presete možete dodati preko [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Za developere

`v1.3.0` je objavljen. Implementirani su lista preseta, detalji, ručno kopiranje, optional root, optional overlay i EN/JA lokalizacija. MIT, FOSS, bez GMS.

### Platforma i stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` za root
- Modul `:app`

Dependency verzije: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Razvoj

Okruženje je pure-Nix flake za Apple Silicon (`aarch64-darwin`) sa JDK 17, Android SDK 35, emulatorom, Gradle, ktlint, detekt i `just`. Dokumentaciju i presete možete uređivati bez instalacije.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Komanda | Svrha | Emulator |
|---|---|---|
| `just test` | JVM tests + non-fatal lint | Ne |
| `just ci` | Stroga CI provjera | Ne |
| `just fmt` | Kotlin formatiranje | Ne |
| `just emu-test` | Instrumented Android tests | Da |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Preseti: `app/src/main/assets/presets.json`; provjerite sa službenom APN stranicom i dodajte `source` / `lastVerified`. Schema i checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Prije PR-a pokrenite `just ci` ako možete. Ne obećavajte tihi APN bez root-a, ne dodajte AccessibilityService auto-fill, ne provjeravajte root prije opt-in i sinhronizujte EN/JA stringove.

Verzija: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid mora graditi `libsu` iz sourcea.

### Licenca

[MIT](../../LICENSE).
