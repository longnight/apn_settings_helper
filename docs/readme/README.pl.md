# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | Polski | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper to mała aplikacja Android open source do przywracania ustawień APN danych komórkowych z użyciem zweryfikowanych presetów. APN to ustawienie, którego telefon używa do danych komórkowych i MMS dla karty SIM.

## Dla użytkowników

App jest dla użytkowników MVNO / tanich SIM, podróżnych i odblokowanych telefonów z lokalną kartą prepaid. Japonia jest obsługiwana jako pierwsza.

Gdy dane komórkowe przestaną działać, telefon mógł utracić poprawny APN. App pomaga wybrać preset, skopiować wartości i wkleić je w edytorze APN Androida. Android nie pozwala zwykłym aplikacjom cicho zmieniać APN, więc podstawowy tryb jest ręczny. Rootowane telefony mogą użyć opcjonalnego zastosowania bezpośredniego.

### Funkcje i ograniczenia

- Pokazuje zweryfikowane presety APN.
- Kopiuje wartości APN jednym dotknięciem.
- Podpowiada wartości list rozwijanych.
- Otwiera systemowe ustawienia APN, gdy Android pozwala.
- Może pokazać panel nad edytorem APN.
- Root apply działa tylko po opt-in na telefonie z rootem.
- Nie zmienia automatycznie APN bez root.
- Nie wie na pewno, który APN jest realnie aktywny.

"Mark as in use" to tylko notatka w aplikacji.

### Pobieranie i użycie

Pobierz APK z [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Wymagany Android 8.0+. Screenshoty i krótki film zostaną dodane później.

1. Otwórz app.
2. Wybierz operatora i preset.
3. Skopiuj pola APN.
4. Dotknij "Open system APN editor".
5. Utwórz lub edytuj APN w Android Settings.
6. Wklej wartości, ustaw dropdowny, zapisz i wybierz APN.
7. Opcjonalnie użyj "Mark as in use".

"Float over the APN editor" pokazuje wartości i przyciski kopiowania, ale niczego nie wypełnia automatycznie.

### Prywatność i zgłoszenia

Brak sieci, kont, reklam, śledzenia i usługi w tle. Overlay i root są opcjonalne.

Zgłoszenia braków/błędów: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Podaj kraj, operatora, linię/plan/sieć, oficjalny link APN i brakującą/błędną wartość. Nie publikuj danych osobowych.

Preset można dodać przez [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Dla programistów i kontrybutorów

`v1.3.0` jest wydane. Lista presetów, szczegóły, kopiowanie ręczne, opcjonalny root, opcjonalny overlay i lokalizacja angielska/japońska są gotowe. MIT, FOSS, bez GMS.

### Platforma i stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` dla root
- Moduł `:app`

Wersje zależności: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Development

Środowisko to pure-Nix flake dla Apple Silicon (`aarch64-darwin`) z JDK 17, Android SDK 35, emulatorem, Gradle, ktlint, detekt i `just`. Dokumentację i presety można edytować bez instalacji środowiska.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Komenda | Cel | Emulator |
|---|---|---|
| `just test` | JVM tests + niekrytyczny lint | Nie |
| `just ci` | Ścisły check CI | Nie |
| `just fmt` | Formatowanie Kotlin | Nie |
| `just emu-test` | Instrumented Android tests | Tak |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Presety: `app/src/main/assets/presets.json`; sprawdzaj z oficjalną stroną APN i dodawaj `source` / `lastVerified`. Schema i checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Przed PR uruchom `just ci`, jeśli możesz. Nie obiecuj cichego APN bez root, nie dodawaj AccessibilityService auto-fill, nie sprawdzaj root przed opt-in i synchronizuj stringi EN/JA.

Wersja: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid musi budować `libsu` ze źródeł.

### Licencja

[MIT](../../LICENSE).
