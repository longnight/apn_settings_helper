# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | 繁體中文 | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper 是一款小型開源 Android 應用程式，用於透過已驗證的預設恢復手機的行動數據 APN 設定。APN 是 Access Point Name，也就是手機為 SIM 連接行動數據和 MMS 所用的設定。

它面向 MVNO / 平價 SIM 使用者、旅客，以及在解鎖手機中使用當地預付卡 SIM 的人。專案優先支援日本，也能逐步擴充到更多地區。

## 給使用者

### 為什麼需要它

行動數據有時會因為手機遺失、變更或沒有正確 APN 而停止運作。手動輸入很麻煩：值很長、欄位容易混淆，不同電信商的說明也不完全一樣。

這個應用程式讓你選擇電信商預設，逐項複製 APN 值，開啟 Android APN 編輯器，貼上並儲存。現代 Android 不允許一般應用程式靜默修改 APN，因此一般流程是引導式手動設定。已 root 手機可選擇一鍵套用。

### 可以做什麼

- 顯示內建的已驗證 APN 預設。
- 點一下複製 APN 值。
- 提示下拉欄位應選的值。
- 在 Android 允許時開啟系統 APN 設定。
- 授權後在 APN 編輯器上方顯示輔助浮窗。
- 僅在 root 手機上，開啟 root 選項後直接套用 APN。

### 不能做什麼

- 不能在一般非 root 手機上自動修改 APN。
- 不能確定手機目前實際使用哪個 APN。
- 不能解決 SIM、方案、電信商鎖或裝置限制導致的 APN 編輯阻擋。

"Mark as in use" 只是應用程式內備註，不表示應用程式讀取或驗證了手機目前 APN。

### 下載和安裝

從 [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases) 下載 APK。應用程式支援 Android 8.0 以上版本。許多手機會要求允許瀏覽器或檔案管理器安裝 APK。

截圖和簡短影片稍後加入。

### 使用方式

1. 開啟 APN Settings Helper。
2. 搜尋或選擇你的電信商。
3. 開啟與 SIM 或網路相符的預設。
4. 複製應用程式中顯示的 APN 欄位。
5. 點選 "Open system APN editor"。
6. 在 Android Settings 中新增或編輯對應 APN。
7. 將值貼到對應欄位。
8. 依提示設定下拉欄位。
9. 儲存並選取該 APN。
10. 如需記錄，回到應用程式點選 "Mark as in use"。

如果來回切換不方便，可使用 "Float over the APN editor"。浮窗不會自動填寫，只會讓值和複製按鈕保持可見。

### 隱私

- 不存取網路。
- 不需要帳號。
- 沒有廣告。
- 不追蹤。
- 沒有背景服務。

基本手動流程不需要特殊權限。Overlay 和 root 都是選用功能。

### 缺少或錯誤的電信商設定

請開 issue：[github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues)

請盡量提供國家/地區、電信商、SIM 線路/方案/網路類型、官方 APN 頁面連結，以及缺少或錯誤的值。不要提交帳號、電話號碼、SIM 序號或含個資的截圖。

開發者可直接加入預設。見 [CONTRIBUTING.md](../../CONTRIBUTING.md)。

## 給開發者和貢獻者

`v1.4.0` 已發布。預設列表、詳細頁、手動複製、選用 root 套用、選用浮窗輔助均已實作，介面已在地化為 **20 種語言**，並支援在應用程式內切換語言（工具列的翻譯圖示可開啟語言選單）。本專案使用 MIT 授權，面向 FOSS，無 GMS 依賴。

### Android 支援和技術棧

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- root 套用使用 `libsu`
- 單 Android 模組：`:app`

依賴版本見 [gradle/libs.versions.toml](../../gradle/libs.versions.toml)。

### 開發、建置與檢查

開發環境是針對 Apple Silicon (`aarch64-darwin`) 的 pure-Nix flake，CLI-only，包含 JDK 17、Android SDK 35、模擬器、Gradle、ktlint、detekt 和 `just`。只改文件或預設時可以不安裝。

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| 命令 | 內容 | 需要模擬器 |
|---|---|---|
| `just test` | JVM tests + 非 fatal lint | 否 |
| `just ci` | 嚴格 CI 檢查 | 否 |
| `just fmt` | 自動格式化 Kotlin | 否 |
| `just emu-test` | Instrumented Android tests | 是 |

[.github/workflows/ci.yml](../../.github/workflows/ci.yml) 會在 push 和 PR 時執行 `just ci`。

### 預設、PR 和發布

預設在 `app/src/main/assets/presets.json`。請對照電信商官方 APN 頁面，並盡量加入 `source` 和 `lastVerified`。Schema 和 checklist 見 [CONTRIBUTING.md](../../CONTRIBUTING.md)。

提交 PR 前盡量執行 `just ci`。如果本機無法執行 Android 工具鏈，請在 PR 中說明檢查方式。

不要宣稱非 root 可靜默套用 APN，不要加入 AccessibilityService 自動填寫，不要提前探測 root，不要暗示 "in use" 是即時 APN 驗證，並保持英文/日文 UI 字串同步。

版本在 [app/build.gradle.kts](../../app/build.gradle.kts)。Release signing 由 `keystore.properties` 控制。F-Droid build 需要從 source 處理 `libsu`。

### 授權

[MIT](../../LICENSE).
