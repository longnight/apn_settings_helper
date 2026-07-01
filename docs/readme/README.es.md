# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | Español | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper es una pequeña app Android de código abierto para restaurar los APN de datos móviles desde presets verificados. APN significa Access Point Name: el ajuste que usa el teléfono para conectar datos móviles y MMS con tu SIM.

Está pensada para usuarios de MVNO / SIM económicas, viajeros y teléfonos desbloqueados con una SIM prepago local. Japón está cubierto primero y se pueden añadir más regiones.

## Para usuarios

### Por qué existe

A veces los datos móviles dejan de funcionar porque el teléfono perdió, cambió o nunca tuvo el APN correcto. Configurarlo a mano es molesto: valores largos, campos parecidos e instrucciones distintas por operador.

La app ayuda a elegir un preset, copiar valores, abrir el editor APN de Android y pegarlos. Android no permite que una app normal cambie APN en silencio, así que el flujo principal es manual guiado. En teléfonos rooteados hay aplicación directa opcional.

### Puede hacer

- Mostrar presets APN verificados.
- Copiar valores APN con un toque.
- Indicar qué elegir en desplegables.
- Abrir ajustes APN del sistema cuando Android lo permite.
- Mostrar un panel flotante sobre el editor APN.
- Aplicar directamente solo en teléfonos rooteados y con permiso del usuario.

### No puede hacer

- Cambiar APN automáticamente en un teléfono normal sin root.
- Saber con certeza qué APN usa realmente el teléfono.
- Arreglar bloqueos de SIM, plan, operador o dispositivo.

"Mark as in use" es solo una nota dentro de la app, no una verificación del APN real.

### Descargar y usar

Descarga el APK desde [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Soporta Android 8.0 o superior. Capturas y video corto se añadirán después.

1. Abre la app.
2. Busca o elige tu operador.
3. Abre el preset correcto.
4. Copia los campos APN.
5. Toca "Open system APN editor".
6. Crea o edita el APN en Android Settings.
7. Pega valores, ajusta desplegables, guarda y selecciona el APN.
8. Opcionalmente toca "Mark as in use".

"Float over the APN editor" solo mantiene valores y botones de copia visibles; no rellena nada automáticamente.

### Privacidad y reportes

Sin red, cuentas, anuncios, rastreo ni servicio en segundo plano. Overlay y root son opcionales.

Si faltan datos de un operador, abre un issue: [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues). Incluye país, operador, línea/plan/red, enlace oficial APN y el valor incorrecto o faltante. No incluyas datos personales.

Los desarrolladores pueden añadir presets con [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Para desarrolladores y contribuidores

`v1.4.0` está publicado. La lista de presets, el detalle, la copia manual, el root opcional y el overlay opcional están implementados, y la interfaz está traducida a **20 idiomas**, con un selector de idioma integrado (un icono de traducción en la barra de herramientas abre un menú de idiomas). App MIT, orientada a FOSS y sin GMS.

### Android y tecnología

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` para root
- Módulo único `:app`

Versiones en [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Desarrollo

El entorno es un flake pure-Nix para Apple Silicon (`aarch64-darwin`), solo CLI, con JDK 17, Android SDK 35, emulador, Gradle, ktlint, detekt y `just`. Para docs o presets no hace falta instalarlo.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| Comando | Qué hace | Emulador |
|---|---|---|
| `just test` | JVM tests + lint no fatal | No |
| `just ci` | Gate CI estricto | No |
| `just fmt` | Formateo Kotlin | No |
| `just emu-test` | Instrumented Android tests | Sí |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

### Presets, PR y release

Presets: `app/src/main/assets/presets.json`. Verifica contra la página APN oficial y añade `source` / `lastVerified` si es posible. Schema y checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Antes de PR, ejecuta `just ci` si puedes. No afirmar aplicación APN silenciosa sin root, no añadir auto-fill con AccessibilityService, no sondear root antes del opt-in, no presentar "in use" como verificación real, y mantener strings inglés/japonés sincronizados.

Versión en [app/build.gradle.kts](../../app/build.gradle.kts). Firma release opcional con `keystore.properties`. F-Droid debe construir `libsu` desde source.

### Licencia

[MIT](../../LICENSE).
