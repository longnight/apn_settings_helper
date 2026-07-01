# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | Français | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper est une petite application Android open source qui aide à restaurer les paramètres APN de données mobiles à partir de préréglages vérifiés. APN signifie Access Point Name : le réglage utilisé par le téléphone pour les données mobiles et les MMS de votre SIM.

Elle vise les utilisateurs de MVNO / SIM économiques, les voyageurs et les téléphones déverrouillés avec SIM prépayée locale. Le Japon est couvert en premier, d'autres régions peuvent être ajoutées.

## Pour les utilisateurs

### Pourquoi cette app existe

Les données mobiles peuvent cesser de fonctionner si le téléphone perd, modifie ou n'a jamais eu le bon APN. Le refaire à la main est pénible : valeurs longues, champs faciles à confondre et instructions différentes selon l'opérateur.

L'app permet de choisir un préréglage, copier les valeurs, ouvrir l'éditeur APN Android et les coller. Android n'autorise pas une app normale à modifier l'APN silencieusement ; le flux principal est donc manuel guidé. Les téléphones rootés peuvent utiliser une application directe optionnelle.

### Ce qu'elle peut faire

- Afficher des préréglages APN vérifiés.
- Copier les valeurs APN en un toucher.
- Indiquer les valeurs des listes déroulantes.
- Ouvrir les paramètres APN système quand Android le permet.
- Afficher un panneau flottant au-dessus de l'éditeur APN.
- Appliquer directement uniquement sur téléphone rooté après opt-in.

### Ce qu'elle ne peut pas faire

- Modifier automatiquement l'APN sur un téléphone normal non rooté.
- Savoir avec certitude quel APN le téléphone utilise réellement.
- Corriger une limitation SIM, forfait, opérateur ou appareil.

"Mark as in use" est seulement une note dans l'app, pas une vérification du vrai APN.

### Télécharger et utiliser

Téléchargez l'APK depuis [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Android 8.0 ou plus récent est requis. Captures d'écran et courte vidéo seront ajoutées plus tard.

1. Ouvrez l'app.
2. Recherchez ou choisissez votre opérateur.
3. Ouvrez le bon préréglage.
4. Copiez les champs APN.
5. Appuyez sur "Open system APN editor".
6. Créez ou modifiez l'APN dans Android Settings.
7. Collez les valeurs, réglez les listes déroulantes, enregistrez et sélectionnez l'APN.
8. Optionnellement, appuyez sur "Mark as in use".

"Float over the APN editor" garde seulement les valeurs et boutons de copie visibles ; il ne remplit rien automatiquement.

### Confidentialité et signalements

Pas d'accès réseau, pas de compte, pas de publicité, pas de suivi, pas de service en arrière-plan. Overlay et root sont optionnels.

Pour un opérateur manquant ou incorrect, ouvrez une issue : [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues). Indiquez pays, opérateur, ligne/forfait/réseau, lien APN officiel et valeur manquante ou incorrecte. Ne publiez pas d'informations personnelles.

Les développeurs peuvent ajouter des préréglages via [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Pour les développeurs et contributeurs

`v1.4.0` est publié. Liste des préréglages, détail, copie manuelle, root optionnel et overlay optionnel sont en place, et l'interface est traduite en **20 langues**, avec un sélecteur de langue intégré (une icône de traduction dans la barre d'outils ouvre un menu de langues). App MIT, orientée FOSS, sans GMS.

### Android et technologies

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` pour root
- Module unique `:app`

Versions dans [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Développement

L'environnement est un flake pure-Nix pour Apple Silicon (`aarch64-darwin`), CLI-only, avec JDK 17, Android SDK 35, émulateur, Gradle, ktlint, detekt et `just`. Pour docs ou presets seulement, il n'est pas nécessaire.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| Commande | Rôle | Émulateur |
|---|---|---|
| `just test` | JVM tests + lint non fatal | Non |
| `just ci` | Gate CI strict | Non |
| `just fmt` | Formatage Kotlin | Non |
| `just emu-test` | Instrumented Android tests | Oui |

CI : [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

### Presets, PR et release

Presets : `app/src/main/assets/presets.json`. Vérifiez avec la page APN officielle et ajoutez `source` / `lastVerified` si possible. Schema et checklist : [CONTRIBUTING.md](../../CONTRIBUTING.md).

Avant une PR, lancez `just ci` si possible. Ne pas promettre d'application APN silencieuse sans root, ne pas ajouter d'auto-fill AccessibilityService, ne pas sonder root avant opt-in, ne pas présenter "in use" comme vérification réelle, garder les chaînes anglais/japonais synchronisées.

Version dans [app/build.gradle.kts](../../app/build.gradle.kts). Signature release optionnelle via `keystore.properties`. F-Droid doit construire `libsu` depuis les sources.

### Licence

[MIT](../../LICENSE).
