# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | Українська
</p>

APN Settings Helper — невеликий Android-застосунок з відкритим кодом для відновлення APN-налаштувань мобільних даних із перевірених пресетів. APN — це налаштування, яке телефон використовує для мобільних даних і MMS через SIM.

## Для користувачів

Застосунок створено для користувачів MVNO / бюджетних SIM, мандрівників і розблокованих телефонів із місцевою prepaid SIM. Спершу підтримується Японія.

Якщо мобільні дані перестали працювати, телефон міг втратити правильний APN. Застосунок допомагає вибрати пресет, скопіювати значення і вставити їх у редактор APN Android. Android не дозволяє звичайним застосункам тихо змінювати APN, тому основний шлях ручний. На root-пристроях доступне опційне пряме застосування.

### Може і не може

- Показує перевірені APN-пресети.
- Копіює APN-значення одним дотиком.
- Дає значення для dropdown-полів.
- Відкриває системні APN-налаштування, якщо Android дозволяє.
- Може показати панель над APN-редактором.
- Root apply працює лише після opt-in на root-пристрої.
- Не змінює APN автоматично без root.
- Не знає точно, який APN справді активний.

"Mark as in use" — лише нотатка в застосунку.

### Завантаження і використання

Завантажте APK з [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Потрібен Android 8.0+. Скріншоти і коротке відео буде додано пізніше.

1. Відкрийте застосунок.
2. Виберіть оператора і пресет.
3. Скопіюйте APN-поля.
4. Натисніть "Open system APN editor".
5. Створіть або відредагуйте APN в Android Settings.
6. Вставте значення, налаштуйте dropdowns, збережіть і виберіть APN.
7. За бажанням використайте "Mark as in use".

"Float over the APN editor" показує значення і кнопки копіювання, але не заповнює автоматично.

### Приватність і звіти

Немає мережі, акаунтів, реклами, трекінгу чи фонового сервісу. Overlay і root необов'язкові.

Помилки або відсутні оператори: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Вкажіть країну, оператора, SIM/тариф/мережу, офіційне APN-посилання і неправильне значення. Не публікуйте особисті дані.

Пресети можна додати через [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Для розробників

`v1.3.0` випущено. Реалізовано список пресетів, деталі, ручне копіювання, optional root, optional overlay і EN/JA localization. MIT, FOSS, без GMS.

### Платформа і стек

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` для root
- Модуль `:app`

Версії залежностей: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Розробка

Середовище — pure-Nix flake для Apple Silicon (`aarch64-darwin`) з JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt і `just`. Документацію і пресети можна редагувати без встановлення.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Команда | Призначення | Emulator |
|---|---|---|
| `just test` | JVM tests + non-fatal lint | Ні |
| `just ci` | Сувора CI-перевірка | Ні |
| `just fmt` | Форматування Kotlin | Ні |
| `just emu-test` | Instrumented Android tests | Так |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Пресети: `app/src/main/assets/presets.json`; звіряйте з офіційною APN-сторінкою і додавайте `source` / `lastVerified`. Schema і checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Перед PR запускайте `just ci`, якщо можете. Не обіцяйте тихий APN без root, не додавайте AccessibilityService auto-fill, не перевіряйте root до opt-in і синхронізуйте EN/JA рядки.

Версія: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid має збирати `libsu` з source.

### Ліцензія

[MIT](../../LICENSE).
