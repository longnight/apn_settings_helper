# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

<p align="center">
  日本語 | <a href="docs/readme/README.en.md">English</a> | <a href="docs/readme/README.vi.md">Tiếng Việt</a> | <a href="docs/readme/README.zh-CN.md">简体中文</a> | <a href="docs/readme/README.zh-TW.md">繁體中文</a> | <a href="docs/readme/README.ko.md">한국어</a> | <a href="docs/readme/README.de.md">Deutsch</a> | <a href="docs/readme/README.es.md">Español</a> | <a href="docs/readme/README.fr.md">Français</a> | <a href="docs/readme/README.it.md">Italiano</a> | <a href="docs/readme/README.da.md">Dansk</a> | <a href="docs/readme/README.pl.md">Polski</a> | <a href="docs/readme/README.bs.md">Bosanski</a> | <a href="docs/readme/README.ar.md">العربية</a> | <a href="docs/readme/README.no.md">Norsk</a> | <a href="docs/readme/README.pt-BR.md">Português (Brasil)</a> | <a href="docs/readme/README.th.md">ไทย</a> | <a href="docs/readme/README.tr.md">Türkçe</a> | <a href="docs/readme/README.km.md">ភាសាខ្មែរ</a> | <a href="docs/readme/README.uk.md">Українська</a>
</p>

APN Settings Helper は、検証済みプリセットからスマートフォンのモバイルデータ用 APN 設定を復元しやすくする、小さなオープンソース Android アプリです。APN は Access Point Name の略で、SIM でモバイルデータや MMS に接続するために端末が使う設定です。

MVNO や格安 SIM ユーザー、旅行者、現地プリペイド SIM を SIM フリー端末で使う人向けに作っています。まずは日本を中心にしつつ、ほかの地域も追加できる設計です。

## アプリを使う方向け

### 何を解決するアプリか

モバイルデータが急に使えなくなる原因のひとつに、端末が正しい APN 設定を失った、変更してしまった、または最初から持っていなかった、というケースがあります。手作業で直すには値が長く、欄も多く、通信会社ごとの説明も少しずつ違います。

このアプリでは、プリセットを選び、必要な APN 値をコピーし、Android の APN 編集画面に貼り付けて保存できます。現在の Android では普通のアプリが APN を裏側で勝手に変更できないため、通常の流れは「手動設定をわかりやすく案内する」ものです。root 済み端末では、任意でワンタップ適用も使えます。

### できること

- 同梱された検証済み APN プリセットを表示する。
- ツールバーの翻訳アイコンから、UI の表示言語を 20 言語から選んで切り替える。
- APN、ユーザー名、MCC/MNC などの値をタップでコピーする。
- 認証タイプや APN プロトコルなど、ドロップダウンで選ぶ値を案内する。
- Android が許可する場合、システムの APN 設定画面を開く。
- 任意で、APN 編集画面の上に小さなヘルパーパネルを表示する。
- root 済み端末では、ユーザーが有効化した場合だけ APN を直接適用する。

### できないこと

- 通常の非 root 端末で APN を自動的に変更することはできません。
- 端末が現在どの APN を実際に使っているかを確実に読むことはできません。
- SIM、契約、キャリアロック、端末側の制限で APN 編集がブロックされている場合、それ自体は直せません。

「使用中としてマーク」はアプリ内の自分用メモです。端末の実際のネットワーク状態を検証するものではありません。

### ダウンロードとインストール

APK は [GitHub Releases ページ](https://github.com/longnight/apn_settings_helper/releases) からダウンロードできます。

対応 Android は 8.0 以降です。多くの端末では、APK をインストールする前にブラウザやファイルマネージャーからのインストール許可を求められます。

スクリーンショットや短い使い方動画は、あとで追加する予定です。

### 使い方

1. APN Settings Helper を開きます。
2. 通信会社を検索するか、一覧から選びます。
3. SIM の回線やネットワークに合うプリセットを開きます。
4. 表示された APN 項目をコピーします。
5. 「システムの APN 編集画面を開く」をタップします。
6. Android 設定で新しい APN を作成するか、該当する APN を編集します。
7. コピーした値を対応する欄に貼り付けます。
8. アプリが案内するドロップダウン項目を選びます。
9. Android 設定で APN を保存し、その APN を選択します。
10. 必要ならアプリに戻り、「使用中としてマーク」をタップします。

アプリと Android 設定を行き来するのが面倒な場合は、「APN 編集画面に重ねて表示」を使えます。これは自動入力ではなく、値とコピー用ボタンを APN 編集画面の上に表示するだけです。

### プライバシー

- ネットワークアクセスなし。
- アカウントなし。
- 広告なし。
- トラッキングなし。
- バックグラウンドサービスなし。

基本の手動フローに特別な権限は不要です。オーバーレイと root はどちらも任意で、ユーザーが選んだあとにだけ使われます。

### プリセットがない・間違っている場合

[Issue ページ](https://github.com/longnight/apn_settings_helper/issues) で知らせてください。

あると助かる情報:

- 国または地域。
- 通信会社名。
- SIM の回線、プラン、ネットワーク種別。
- 通信会社の公式 APN 説明ページへのリンク。
- 足りない、または間違っていると思う値。

個人情報、電話番号、SIM シリアル番号、個人情報が写ったスクリーンショットは含めないでください。

開発者の方は [CONTRIBUTING.md](CONTRIBUTING.md) を見て、プリセットを直接追加できます。

## 開発者・コントリビューター向け

### プロジェクトの状態

`v1.4.0` はリリース済みです。プリセット一覧、詳細画面、手動コピー、任意の root 適用、任意のフローティングヘルパーに加え、UI を **20 言語** にローカライズし、アプリ内で表示言語を切り替えられる言語メニュー（ツールバーの翻訳アイコン→右側のドロワー）を実装しています。

MIT ライセンスの FOSS アプリで、GMS には依存しません。

### Android サポート

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile SDK: 35
- Target SDK: 35

通常端末では手動フローを使います。root 適用は、ユーザーが明示的に有効化し、root アクセスが検出された場合にだけ表示されます。オーバーレイヘルパーには `SYSTEM_ALERT_WINDOW` 権限が必要です。

### 技術スタック

- Kotlin
- Jetpack Compose
- Material 3
- Compose Navigation
- DataStore Preferences
- kotlinx.serialization
- Kotlin coroutines
- root 適用用の `libsu`
- 単一 Android モジュール `:app`

依存関係のバージョンは [gradle/libs.versions.toml](gradle/libs.versions.toml) にあります。

### 開発環境

開発環境は Apple Silicon (`aarch64-darwin`) 向けの pure-Nix flake です。CLI のみで、Android Studio は不要です。JDK 17、Android SDK 35、platform tools、arm64 emulator、Gradle、ktlint、detekt、`just` を含みます。

ドキュメントやプリセットデータだけを編集する場合は、この環境をインストールしなくても作業できます。ただし Android のビルドやフルチェックは実行できません。

### ビルドと実行

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

### テストとチェック

| コマンド | 内容 | エミュレーター |
|---|---|---|
| `just test` | JVM テスト、ktlint、detekt、Android lint。リンターは非 fatal | 不要 |
| `just ci` | CI と同じ厳格なチェック | 不要 |
| `just fmt` | ktlint で Kotlin を自動整形 | 不要 |
| `just emu-test` | Android instrumented tests | 必要 |

[.github/workflows/ci.yml](.github/workflows/ci.yml) は push と pull request ごとに `just ci` を実行します。

### プリセットの追加・修正

プリセットは `app/src/main/assets/presets.json` にあります。通信会社の公式 APN ページで値を確認し、可能なら `source` と `lastVerified` を含めてください。

スキーマ、フィールド、検証ルール、PR チェックリストは [CONTRIBUTING.md](CONTRIBUTING.md) を参照してください。

### Pull request

PR 前に可能なら `just ci` を実行してください。ローカルで Android ツールチェーンを使えない場合は、PR にその旨と確認した内容を書いてください。

守るべきルール:

- 非 root で APN を自動適用できると書かない。
- AccessibilityService ベースの自動入力を追加しない。
- ユーザーが有効化する前に root を probe しない。
- 「使用中」が端末の live APN 検証を意味すると示唆しない。
- 正規の英語（`values/`）と日本語（`values-ja/`）の UI 文字列を同期する（他の 18 言語は翻訳）。

### リリースとパッケージング

- バージョンは [app/build.gradle.kts](app/build.gradle.kts) にあります。
- Fastlane metadata は `fastlane/metadata/android/` 配下にあります。
- リリース署名は gitignored の `keystore.properties` がある場合だけ有効です。
- F-Droid build では JitPack が使えないため、`libsu` をソースから扱う必要があります。

```properties
storeFile=keystore/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

```sh
./gradlew :app:assembleRelease
```

`keystore.properties` がない場合、release APK は未署名です。これは CI と F-Droid では想定どおりです。署名鍵を失うと同じ署名 ID で更新できないため、必ずバックアップしてください。

### ライセンス

[MIT](LICENSE).
