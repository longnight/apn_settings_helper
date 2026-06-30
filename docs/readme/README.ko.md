# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | 한국어 | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper는 검증된 프리셋으로 휴대폰의 모바일 데이터 APN 설정을 복구하기 쉽게 해 주는 작은 오픈소스 Android 앱입니다. APN은 Access Point Name으로, SIM에서 모바일 데이터와 MMS를 연결할 때 쓰는 설정입니다.

MVNO / 알뜰 SIM 사용자, 여행자, 현지 선불 SIM을 언락폰에 넣어 쓰는 사람을 위해 만들었습니다. 일본을 먼저 지원하며 다른 지역도 추가할 수 있습니다.

## 앱 사용자용

### 왜 필요한가

모바일 데이터가 갑자기 작동하지 않는 이유 중 하나는 휴대폰이 SIM에 맞는 APN 설정을 잃었거나, 변경했거나, 처음부터 갖고 있지 않았기 때문입니다. 직접 입력하기에는 값이 길고 필드가 헷갈리며 통신사 안내도 제각각입니다.

이 앱은 통신사 프리셋을 고르고, APN 값을 복사하고, Android APN 편집기에 붙여 넣어 저장하도록 돕습니다. 현대 Android는 일반 앱이 APN을 조용히 바꾸는 것을 허용하지 않으므로 기본 흐름은 안내식 수동 설정입니다. root된 휴대폰은 선택적으로 원탭 적용을 사용할 수 있습니다.

### 할 수 있는 일

- 검증된 APN 프리셋을 보여줍니다.
- APN 값을 탭으로 복사합니다.
- 드롭다운에서 선택할 값을 안내합니다.
- Android가 허용하면 시스템 APN 설정 화면을 엽니다.
- 권한을 주면 APN 편집기 위에 도움 패널을 띄웁니다.
- root된 휴대폰에서만 root 옵션을 켠 뒤 APN을 직접 적용합니다.

### 할 수 없는 일

- 일반 비 root 휴대폰에서 APN을 자동 변경할 수 없습니다.
- 현재 휴대폰이 실제로 어떤 APN을 쓰는지 확실히 알 수 없습니다.
- SIM, 요금제, 통신사 잠금, 기기 제한으로 APN 편집이 막힌 문제는 해결할 수 없습니다.

"Mark as in use"는 앱 안의 개인 메모일 뿐이며 실제 APN 검증이 아닙니다.

### 다운로드와 설치

APK는 [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases)에서 받을 수 있습니다. Android 8.0 이상을 지원합니다. 많은 기기에서 APK 설치 전 브라우저나 파일 관리자 설치 권한을 요구합니다.

스크린샷과 짧은 사용 영상은 나중에 추가됩니다.

### 사용 방법

1. APN Settings Helper를 엽니다.
2. 통신사를 검색하거나 목록에서 선택합니다.
3. SIM 또는 네트워크에 맞는 프리셋을 엽니다.
4. 표시된 APN 필드를 복사합니다.
5. "Open system APN editor"를 탭합니다.
6. Android Settings에서 새 APN을 만들거나 알맞은 APN을 편집합니다.
7. 값을 각 필드에 붙여 넣습니다.
8. 앱이 안내하는 드롭다운 값을 선택합니다.
9. APN을 저장하고 선택합니다.
10. 필요하면 앱으로 돌아와 "Mark as in use"를 누릅니다.

"Float over the APN editor"는 자동 입력이 아니라 값과 복사 버튼을 APN 편집기 위에 보여 주는 기능입니다.

### 개인정보 보호

- 네트워크 접근 없음.
- 계정 없음.
- 광고 없음.
- 추적 없음.
- 백그라운드 서비스 없음.

기본 수동 흐름에는 특별한 권한이 필요하지 않습니다. Overlay와 root는 선택 사항입니다.

### 통신사 설정이 없거나 틀렸나요?

issue를 열어 주세요: [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues)

국가/지역, 통신사, SIM 회선/요금제/네트워크 유형, 공식 APN 안내 링크, 누락되었거나 잘못된 값을 알려 주세요. 개인 정보, 전화번호, SIM 일련번호, 개인정보가 보이는 스크린샷은 올리지 마세요.

개발자는 [CONTRIBUTING.md](../../CONTRIBUTING.md)를 보고 프리셋을 직접 추가할 수 있습니다.

## 개발자 및 기여자용

`v1.3.0`이 출시되었습니다. 프리셋 목록, 상세 화면, 수동 복사, 선택적 root 적용, 선택적 플로팅 도우미, 영어/일본어 로컬라이즈가 구현되어 있습니다. MIT 라이선스의 FOSS 지향 앱이며 GMS 의존성이 없습니다.

### Android 지원과 기술

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- root 적용용 `libsu`
- 단일 Android 모듈: `:app`

의존성 버전은 [gradle/libs.versions.toml](../../gradle/libs.versions.toml)에 있습니다.

### 개발, 빌드, 검사

개발 환경은 Apple Silicon (`aarch64-darwin`)용 pure-Nix flake이며 CLI-only입니다. JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt, `just`를 제공합니다. 문서나 프리셋만 수정할 때는 설치하지 않아도 됩니다.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| 명령 | 내용 | 에뮬레이터 |
|---|---|---|
| `just test` | JVM tests + non-fatal lint | 아니요 |
| `just ci` | 엄격한 CI 검사 | 아니요 |
| `just fmt` | Kotlin 자동 포맷 | 아니요 |
| `just emu-test` | Instrumented Android tests | 예 |

[.github/workflows/ci.yml](../../.github/workflows/ci.yml)은 push와 PR에서 `just ci`를 실행합니다.

### 프리셋, PR, 릴리스

프리셋은 `app/src/main/assets/presets.json`에 있습니다. 통신사 공식 APN 페이지와 대조하고 가능하면 `source`와 `lastVerified`를 포함하세요. 스키마와 checklist는 [CONTRIBUTING.md](../../CONTRIBUTING.md)에 있습니다.

PR 전 가능하면 `just ci`를 실행하세요. Android toolchain을 실행할 수 없다면 PR에 확인 내용을 적어 주세요.

비 root 자동 APN 적용을 주장하지 말고, AccessibilityService 자동 입력을 추가하지 말고, opt-in 전 root probe를 하지 말고, "in use"를 실제 APN 검증처럼 보이게 하지 말고, 영어/일본어 UI 문자열을 동기화하세요.

버전은 [app/build.gradle.kts](../../app/build.gradle.kts)에 있습니다. Release signing은 `keystore.properties`로 제어됩니다. F-Droid build는 `libsu`를 source에서 처리해야 합니다.

### 라이선스

[MIT](../../LICENSE).
