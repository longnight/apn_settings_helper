# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | Türkçe | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper, doğrulanmış ön ayarlardan mobil veri APN ayarlarını geri yükleyen küçük açık kaynaklı bir Android uygulamasıdır. APN, SIM için mobil veri ve MMS bağlantısında kullanılan ayardır.

## Kullanıcılar için

Uygulama MVNO / ekonomik SIM kullanıcıları, gezginler ve yerel ön ödemeli SIM kullanan kilidi açık telefonlar içindir. Önce Japonya desteklenir.

Mobil veri durursa telefon doğru APN'yi kaybetmiş olabilir. Uygulama ön ayar seçmeyi, değerleri kopyalamayı ve Android APN düzenleyicisine yapıştırmayı kolaylaştırır. Android normal uygulamaların APN'yi sessizce değiştirmesine izin vermez; ana akış manuel rehberdir. Rootlu telefonlar isteğe bağlı doğrudan uygulama kullanabilir.

### Yapabilir ve yapamaz

- Doğrulanmış APN ön ayarlarını gösterir.
- APN değerlerini tek dokunuşla kopyalar.
- Dropdown alan değerlerini verir.
- Android izin verirse sistem APN ayarlarını açar.
- APN düzenleyici üzerinde panel gösterebilir.
- Root apply yalnızca opt-in sonrası root cihazda çalışır.
- Root olmadan APN'yi otomatik değiştirmez.
- Hangi APN'nin gerçekten aktif olduğunu kesin bilmez.

"Mark as in use" yalnızca uygulama içi nottur.

### İndir ve kullan

APK'yı [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases) üzerinden indirin. Android 8.0+ gerekir. Ekran görüntüleri ve kısa video sonra eklenecek.

1. Uygulamayı açın.
2. Operatör ve ön ayar seçin.
3. APN alanlarını kopyalayın.
4. "Open system APN editor" dokunun.
5. Android Settings içinde APN oluşturun veya düzenleyin.
6. Değerleri yapıştırın, dropdownları ayarlayın, kaydedin ve APN'yi seçin.
7. İsterseniz "Mark as in use" kullanın.

"Float over the APN editor" değerleri ve kopyalama düğmelerini gösterir, otomatik doldurmaz.

### Gizlilik ve bildirim

Ağ, hesap, reklam, izleme veya arka plan servisi yoktur. Overlay ve root isteğe bağlıdır.

Eksik/hatalı operatörler: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Ülke, operatör, SIM/paket/ağ, resmi APN linki ve hatalı değeri yazın. Kişisel veri paylaşmayın.

Ön ayarlar [CONTRIBUTING.md](../../CONTRIBUTING.md) ile eklenebilir.

## Geliştiriciler için

`v1.3.0` yayınlandı. Ön ayar listesi, detaylar, manuel kopyalama, optional root, optional overlay ve EN/JA yerelleştirme tamam. MIT, FOSS, GMS yok.

### Platform ve stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- Root için `libsu`
- Modül `:app`

Sürümler: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Geliştirme

Ortam Apple Silicon (`aarch64-darwin`) için pure-Nix flake'tir; JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt ve `just` içerir. Docs ve presetler kurulum olmadan düzenlenebilir.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Komut | Amaç | Emulator |
|---|---|---|
| `just test` | JVM tests + non-fatal lint | Hayır |
| `just ci` | Sıkı CI kontrolü | Hayır |
| `just fmt` | Kotlin formatlama | Hayır |
| `just emu-test` | Instrumented Android tests | Evet |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Presetler: `app/src/main/assets/presets.json`; resmi APN sayfasıyla kontrol edip `source` / `lastVerified` ekleyin. Schema ve checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

PR öncesi mümkünse `just ci` çalıştırın. Root olmadan sessiz APN vaat etmeyin, AccessibilityService auto-fill eklemeyin, opt-in öncesi root kontrol etmeyin ve EN/JA stringleri senkron tutun.

Sürüm: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid `libsu`yu source'tan kurmalıdır.

### Lisans

[MIT](../../LICENSE).
