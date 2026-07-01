# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | 简体中文 | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper 是一款小型开源 Android 应用，用于通过已验证的预设恢复手机的移动数据 APN 设置。APN 是 Access Point Name，也就是手机为 SIM 连接移动数据和 MMS 所用的设置。

它面向 MVNO / 低价 SIM 用户、旅行者，以及在解锁手机中使用本地预付费 SIM 的用户。项目优先覆盖日本，也支持后续扩展到更多地区。

## 面向用户

### 为什么需要它

移动数据有时会因为手机丢失、改动或没有正确 APN 而停止工作。手动输入很麻烦：值很长，字段容易混淆，不同运营商的说明也不完全一致。

这个应用让你选择运营商预设，逐项复制 APN 值，打开 Android APN 编辑器，粘贴并保存。现代 Android 不允许普通应用静默修改 APN，因此普通流程是引导式手动设置。已 root 手机可选择一键应用。

### 能做什么

- 显示内置的已验证 APN 预设。
- 点击复制 APN 值。
- 提示下拉字段应选择的值。
- 在 Android 允许时打开系统 APN 设置。
- 授权后在 APN 编辑器上方显示辅助浮窗。
- 仅在 root 手机上，开启 root 选项后直接应用 APN。

### 不能做什么

- 不能在普通非 root 手机上自动修改 APN。
- 不能确定手机当前实际使用哪个 APN。
- 不能解决 SIM、套餐、运营商锁或设备限制导致的 APN 编辑阻止。

"Mark as in use" 只是应用内备注，不表示应用读取或验证了手机当前 APN。

### 下载和安装

从 [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases) 下载 APK。应用支持 Android 8.0 及以上版本。许多手机会要求允许浏览器或文件管理器安装 APK。

截图和简短视频稍后添加。

### 使用方法

1. 打开 APN Settings Helper。
2. 搜索或选择你的运营商。
3. 打开与 SIM 或网络匹配的预设。
4. 复制应用中显示的 APN 字段。
5. 点击 "Open system APN editor"。
6. 在 Android Settings 中新建或编辑对应 APN。
7. 将值粘贴到对应字段。
8. 按提示设置下拉字段。
9. 保存并选中该 APN。
10. 如需记录，返回应用点击 "Mark as in use"。

如果来回切换不方便，可使用 "Float over the APN editor"。浮窗不会自动填写，只会让值和复制按钮保持可见。

### 隐私

- 不访问网络。
- 不需要账号。
- 没有广告。
- 不跟踪。
- 没有后台服务。

基础手动流程不需要特殊权限。Overlay 和 root 都是可选功能。

### 缺少或错误的运营商设置

请打开 issue：[github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues)

请尽量提供国家/地区、运营商、SIM 线路/套餐/网络类型、官方 APN 页面链接，以及缺失或错误的值。不要提交账号、电话号码、SIM 序列号或含个人信息的截图。

开发者可直接添加预设。见 [CONTRIBUTING.md](../../CONTRIBUTING.md)。

## 面向开发者和贡献者

`v1.4.0` 已发布。预设列表、详情页、手动复制、可选 root 应用、可选浮窗辅助均已实现，界面已本地化为 **20 种语言**，并支持在应用内切换语言（工具栏的翻译图标可打开语言菜单）。本项目使用 MIT 许可证，面向 FOSS，无 GMS 依赖。

### Android 支持和技术栈

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- root 应用使用 `libsu`
- 单 Android 模块：`:app`

依赖版本见 [gradle/libs.versions.toml](../../gradle/libs.versions.toml)。

### 开发、构建和检查

开发环境是面向 Apple Silicon (`aarch64-darwin`) 的 pure-Nix flake，CLI-only，包含 JDK 17、Android SDK 35、模拟器、Gradle、ktlint、detekt 和 `just`。只改文档或预设时可以不安装。

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| 命令 | 内容 | 需要模拟器 |
|---|---|---|
| `just test` | JVM tests + 非 fatal lint | 否 |
| `just ci` | 严格 CI 检查 | 否 |
| `just fmt` | 自动格式化 Kotlin | 否 |
| `just emu-test` | Instrumented Android tests | 是 |

[.github/workflows/ci.yml](../../.github/workflows/ci.yml) 会在 push 和 PR 时运行 `just ci`。

### 预设、PR 和发布

预设在 `app/src/main/assets/presets.json`。请对照运营商官方 APN 页面，并尽量添加 `source` 和 `lastVerified`。Schema 和 checklist 见 [CONTRIBUTING.md](../../CONTRIBUTING.md)。

提交 PR 前尽量运行 `just ci`。如果本地无法运行 Android 工具链，请在 PR 中说明检查方式。

不要宣称非 root 可静默应用 APN，不要添加 AccessibilityService 自动填写，不要提前探测 root，不要暗示 "in use" 是实时 APN 验证，并保持英文/日文 UI 字符串同步。

版本在 [app/build.gradle.kts](../../app/build.gradle.kts)。Release signing 由 `keystore.properties` 控制。F-Droid build 需要从源码处理 `libsu`。

### 许可证

[MIT](../../LICENSE).
