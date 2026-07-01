# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | Português (Brasil) | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper é um pequeno app Android de código aberto para restaurar configurações APN de dados móveis a partir de presets verificados. APN é a configuração usada pelo telefone para dados móveis e MMS do SIM.

## Para usuários

O app é para usuários de MVNO / SIM econômico, viajantes e telefones desbloqueados com SIM pré-pago local. O Japão é suportado primeiro.

Se os dados móveis pararem, o telefone pode ter perdido o APN correto. O app ajuda a escolher um preset, copiar valores e colá-los no editor APN do Android. Android não deixa apps comuns mudarem APN silenciosamente, então o fluxo principal é manual. Telefones com root podem usar aplicação direta opcional.

### Pode e não pode

- Mostra presets APN verificados.
- Copia valores APN com um toque.
- Indica valores de campos suspensos.
- Abre as configurações APN do sistema quando Android permite.
- Pode mostrar painel sobre o editor APN.
- Root apply só funciona após opt-in em dispositivo root.
- Não muda APN automaticamente sem root.
- Não sabe com certeza qual APN está realmente ativo.

"Mark as in use" é apenas uma nota no app.

### Baixar e usar

Baixe o APK em [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Requer Android 8.0+. Capturas e vídeo curto virão depois.

1. Abra o app.
2. Escolha operadora e preset.
3. Copie os campos APN.
4. Toque em "Open system APN editor".
5. Crie ou edite APN em Android Settings.
6. Cole valores, ajuste dropdowns, salve e selecione o APN.
7. Use "Mark as in use" se quiser.

"Float over the APN editor" mostra valores e botões de cópia, mas não preenche automaticamente.

### Privacidade e reports

Sem rede, contas, anúncios, rastreamento ou serviço em segundo plano. Overlay e root são opcionais.

Erros ou operadoras ausentes: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). Informe país, operadora, SIM/plano/rede, link APN oficial e valor errado. Não publique dados pessoais.

Presets podem ser adicionados via [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Para desenvolvedores

`v1.4.0` foi lançado. Lista de presets, detalhes, cópia manual, optional root e optional overlay estão implementados, e a interface está traduzida em **20 idiomas**, com um seletor de idioma integrado (um ícone de tradução na barra de ferramentas abre um menu de idiomas). MIT, FOSS, sem GMS.

### Plataforma e stack

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` para root
- Módulo `:app`

Versões: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Desenvolvimento

O ambiente é um pure-Nix flake para Apple Silicon (`aarch64-darwin`) com JDK 17, Android SDK 35, emulador, Gradle, ktlint, detekt e `just`. Docs e presets podem ser editados sem instalar.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| Comando | Finalidade | Emulador |
|---|---|---|
| `just test` | JVM tests + lint não fatal | Não |
| `just ci` | Checagem CI estrita | Não |
| `just fmt` | Formatação Kotlin | Não |
| `just emu-test` | Instrumented Android tests | Sim |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

Presets: `app/src/main/assets/presets.json`; confira com página APN oficial e adicione `source` / `lastVerified`. Schema e checklist: [CONTRIBUTING.md](../../CONTRIBUTING.md).

Antes do PR, rode `just ci` se possível. Não prometa APN silencioso sem root, não adicione AccessibilityService auto-fill, não cheque root antes de opt-in e sincronize strings EN/JA.

Versão: [app/build.gradle.kts](../../app/build.gradle.kts). F-Droid deve construir `libsu` do source.

### Licença

[MIT](../../LICENSE).
