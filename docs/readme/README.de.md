# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | Deutsch | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper ist eine kleine Open-Source-Android-App, mit der du die APN-Einstellungen für mobile Daten aus geprüften Presets wiederherstellen kannst. APN steht für Access Point Name, also die Einstellung, mit der dein Telefon mobile Daten und MMS für deine SIM verbindet.

Die App ist für MVNO-/Budget-SIM-Nutzer, Reisende und entsperrte Telefone mit lokaler Prepaid-SIM gedacht. Japan wird zuerst unterstützt; weitere Regionen können später ergänzt werden.

## Für Nutzer

### Warum diese App existiert

Mobile Daten fallen manchmal aus, weil das Telefon den richtigen APN verloren, geändert oder nie erhalten hat. Manuelles Eintragen ist mühsam: Werte sind lang, Felder leicht zu verwechseln, und jeder Anbieter beschreibt sie anders.

Die App hilft dir, ein Preset zu wählen, APN-Werte zu kopieren, den Android-APN-Editor zu öffnen und alles einzufügen. Android erlaubt normalen Apps keine stille APN-Änderung; der Standardweg ist daher geführte manuelle Einrichtung. Root-Telefone können optional direkt anwenden.

### Kann

- Geprüfte APN-Presets anzeigen.
- APN-Werte per Tipp kopieren.
- Dropdown-Werte wie Authentifizierung und Protokoll erklären.
- Die System-APN-Einstellungen öffnen, wenn Android es erlaubt.
- Optional ein Hilfepanel über dem APN-Editor anzeigen.
- Auf gerooteten Geräten nach Opt-in APN direkt anwenden.

### Kann nicht

- APN auf normalen Nicht-Root-Geräten automatisch ändern.
- Sicher erkennen, welcher APN gerade wirklich aktiv ist.
- SIM-, Tarif-, Betreiber- oder Gerätebeschränkungen reparieren.

"Mark as in use" ist nur eine Notiz in der App, keine Prüfung des echten Telefon-APN.

### Download und Nutzung

Lade das APK von [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Unterstützt wird Android 8.0 und neuer. Screenshots und ein kurzes Video folgen später.

1. App öffnen.
2. Anbieter suchen oder aus der Liste wählen.
3. Passendes Preset öffnen.
4. APN-Felder kopieren.
5. "Open system APN editor" tippen.
6. In Android Settings einen APN erstellen oder bearbeiten.
7. Werte einfügen, Dropdowns setzen, speichern und APN auswählen.
8. Optional in der App "Mark as in use" tippen.

"Float over the APN editor" zeigt Werte und Kopierknöpfe über dem APN-Editor. Es füllt nichts automatisch aus.

### Datenschutz und Rückmeldungen

Kein Netzwerkzugriff, keine Konten, keine Werbung, kein Tracking, kein Hintergrunddienst. Overlay und Root sind optional.

Fehlende oder falsche Anbieterwerte bitte als Issue melden: [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues). Hilfreich sind Land, Anbieter, SIM-Linie/Tarif/Netztyp, offizieller APN-Link und der falsche oder fehlende Wert. Keine privaten Daten posten.

Entwickler können Presets direkt ergänzen: [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Für Entwickler und Beitragende

`v1.3.0` ist veröffentlicht. Preset-Liste, Detailansicht, manuelles Kopieren, optionales Root-Anwenden, optionaler Overlay-Helfer sowie englische/japanische Lokalisierung sind implementiert. Die App ist MIT-lizenziert, FOSS-orientiert und ohne GMS-Abhängigkeit.

### Android und Tech-Stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` für Root-Anwenden
- Einzelnes Android-Modul: `:app`

Dependency-Versionen stehen in [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Entwicklung

Die Entwicklungsumgebung ist eine pure-Nix-Flake für Apple Silicon (`aarch64-darwin`), CLI-only, mit JDK 17, Android SDK 35, Emulator, Gradle, ktlint, detekt und `just`. Für reine Dokumentations- oder Preset-Änderungen musst du sie nicht installieren.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| Befehl | Zweck | Emulator |
|---|---|---|
| `just test` | JVM-Tests + nicht-fatale Lints | Nein |
| `just ci` | Striktes CI-Gate | Nein |
| `just fmt` | Kotlin formatieren | Nein |
| `just emu-test` | Instrumented Android tests | Ja |

CI läuft über [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

### Presets, PRs und Release

Presets liegen in `app/src/main/assets/presets.json`. Werte bitte gegen die offizielle APN-Seite prüfen und möglichst `source` / `lastVerified` ergänzen. Schema und Checkliste: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Vor PRs nach Möglichkeit `just ci` ausführen. Keine stille APN-Anwendung ohne Root behaupten, kein AccessibilityService-Auto-Fill hinzufügen, Root nicht vor Opt-in prüfen, "in use" nicht als Live-Prüfung darstellen, englische und japanische UI-Strings synchron halten.

Versionierung steht in [app/build.gradle.kts](../../app/build.gradle.kts). Release-Signing nutzt optional `keystore.properties`. F-Droid muss `libsu` aus Source bauen.

### Lizenz

[MIT](../../LICENSE).
