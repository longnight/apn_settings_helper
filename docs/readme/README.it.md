# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | Italiano | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper è una piccola app Android open source per ripristinare le impostazioni APN dei dati mobili da preset verificati. APN significa Access Point Name: l'impostazione usata dal telefono per dati mobili e MMS della SIM.

È pensata per utenti MVNO / SIM economiche, viaggiatori e telefoni sbloccati con SIM prepagata locale. Il Giappone è supportato per primo; altre regioni possono essere aggiunte.

## Per utenti

### Perché esiste

A volte i dati mobili smettono di funzionare perché il telefono ha perso, modificato o non ha mai avuto l'APN corretto. Inserirlo a mano è scomodo: valori lunghi, campi simili e istruzioni diverse per operatore.

L'app aiuta a scegliere un preset, copiare i valori, aprire l'editor APN Android e incollarli. Android non permette a un'app normale di cambiare APN in silenzio, quindi il flusso principale è manuale guidato. I telefoni rootati possono usare l'applicazione diretta opzionale.

### Può fare

- Mostrare preset APN verificati.
- Copiare valori APN con un tocco.
- Indicare i valori dei menu a tendina.
- Aprire le impostazioni APN di sistema quando Android lo consente.
- Mostrare un pannello flottante sopra l'editor APN.
- Applicare direttamente solo su telefono rootato dopo opt-in.

### Non può fare

- Modificare automaticamente APN su un telefono normale non rootato.
- Sapere con certezza quale APN il telefono usa davvero.
- Risolvere limitazioni di SIM, piano, operatore o dispositivo.

"Mark as in use" è solo una nota nell'app, non una verifica dell'APN reale.

### Download e uso

Scarica l'APK da [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Richiede Android 8.0 o successivo. Screenshot e breve video saranno aggiunti più avanti.

1. Apri l'app.
2. Cerca o scegli il tuo operatore.
3. Apri il preset corretto.
4. Copia i campi APN.
5. Tocca "Open system APN editor".
6. Crea o modifica l'APN in Android Settings.
7. Incolla i valori, imposta i menu a tendina, salva e seleziona l'APN.
8. Facoltativamente tocca "Mark as in use".

"Float over the APN editor" tiene visibili valori e pulsanti di copia; non compila nulla automaticamente.

### Privacy e segnalazioni

Nessun accesso rete, account, pubblicità, tracciamento o servizio in background. Overlay e root sono opzionali.

Per operatori mancanti o errati, apri una issue: [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues). Includi paese, operatore, linea/piano/rete, link APN ufficiale e valore mancante o errato. Non pubblicare dati personali.

Gli sviluppatori possono aggiungere preset con [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Per sviluppatori e contributori

`v1.3.0` è rilasciato. Lista preset, dettaglio, copia manuale, root opzionale, overlay opzionale e localizzazione inglese/giapponese sono implementati. App MIT, orientata FOSS, senza GMS.

### Android e tecnologie

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` per root
- Modulo unico `:app`

Versioni in [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Sviluppo

L'ambiente è un flake pure-Nix per Apple Silicon (`aarch64-darwin`), CLI-only, con JDK 17, Android SDK 35, emulatore, Gradle, ktlint, detekt e `just`. Per soli docs o preset non serve installarlo.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| Comando | Scopo | Emulatore |
|---|---|---|
| `just test` | JVM tests + lint non fatale | No |
| `just ci` | Gate CI rigoroso | No |
| `just fmt` | Formatta Kotlin | No |
| `just emu-test` | Instrumented Android tests | Sì |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

### Preset, PR e release

Preset: `app/src/main/assets/presets.json`. Verifica con la pagina APN ufficiale e aggiungi `source` / `lastVerified` se possibile. Schema e checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Prima di una PR esegui `just ci` se possibile. Non dichiarare applicazione APN silenziosa senza root, non aggiungere auto-fill AccessibilityService, non sondare root prima dell'opt-in, non presentare "in use" come verifica reale, tieni sincronizzate le stringhe inglesi/giapponesi.

Versione in [app/build.gradle.kts](../../app/build.gradle.kts). Firma release opzionale via `keystore.properties`. F-Droid deve costruire `libsu` da sorgente.

### Licenza

[MIT](../../LICENSE).
