# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | Tiếng Việt | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper là một ứng dụng Android mã nguồn mở nhỏ, giúp khôi phục cài đặt APN dữ liệu di động từ các preset đã được kiểm chứng. APN là Access Point Name: cài đặt điện thoại dùng để kết nối dữ liệu di động và MMS cho SIM.

Ứng dụng dành cho người dùng MVNO / SIM giá rẻ, khách du lịch, và người dùng SIM trả trước địa phương trên điện thoại đã mở khóa. Nhật Bản được hỗ trợ trước, các khu vực khác có thể được thêm dần.

## Dành cho người dùng

### Vì sao ứng dụng này tồn tại

Đôi khi dữ liệu di động ngừng hoạt động vì điện thoại mất, đổi, hoặc chưa từng có APN đúng cho SIM. Nhập lại bằng tay rất phiền: giá trị dài, nhiều trường dễ nhầm, và mỗi nhà mạng viết hướng dẫn hơi khác nhau.

Ứng dụng giúp bạn chọn preset, sao chép từng giá trị APN, mở trình chỉnh APN của Android, dán giá trị và lưu. Android hiện đại không cho ứng dụng thông thường âm thầm đổi APN, nên luồng chính là hướng dẫn thủ công. Điện thoại đã root có thể dùng chế độ áp dụng một chạm tùy chọn.

### Ứng dụng có thể làm gì

- Hiển thị preset APN đã kiểm chứng.
- Sao chép giá trị APN bằng một lần chạm.
- Hướng dẫn giá trị cần chọn trong menu thả xuống.
- Mở màn hình APN hệ thống khi Android cho phép.
- Hiển thị bảng trợ giúp nổi trên trình chỉnh APN nếu bạn cấp quyền overlay.
- Chỉ trên máy đã root, áp dụng APN trực tiếp sau khi bật tùy chọn root.

### Ứng dụng không thể làm gì

- Không thể tự đổi APN trên điện thoại thường chưa root.
- Không thể biết chắc điện thoại đang thật sự dùng APN nào.
- Không thể sửa SIM, gói cước, khóa nhà mạng hoặc thiết bị chặn chỉnh APN.

"Mark as in use" chỉ là ghi chú trong ứng dụng, không phải xác minh APN đang hoạt động trên điện thoại.

### Tải xuống và cài đặt

Tải APK từ [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). Ứng dụng hỗ trợ Android 8.0 trở lên. Nhiều điện thoại sẽ yêu cầu cho phép trình duyệt hoặc trình quản lý tệp cài APK.

Ảnh chụp màn hình và video hướng dẫn ngắn sẽ được thêm sau.

### Cách sử dụng

1. Mở APN Settings Helper.
2. Tìm hoặc chọn nhà mạng từ danh sách preset.
3. Mở preset phù hợp với SIM hoặc mạng của bạn.
4. Sao chép các trường APN được hiển thị.
5. Chạm "Open system APN editor".
6. Trong Android Settings, tạo APN mới hoặc sửa APN phù hợp.
7. Dán từng giá trị vào đúng trường.
8. Chọn các giá trị dropdown theo hướng dẫn.
9. Lưu APN và chọn APN đó.
10. Quay lại ứng dụng và chạm "Mark as in use" nếu muốn lưu ghi chú.

Nếu việc chuyển qua lại giữa ứng dụng và Android Settings bất tiện, hãy dùng "Float over the APN editor". Bảng nổi không tự điền; nó chỉ giữ giá trị và nút sao chép trên màn hình.

### Quyền riêng tư

- Không truy cập mạng.
- Không tài khoản.
- Không quảng cáo.
- Không theo dõi.
- Không dịch vụ nền.

Luồng thủ công không cần quyền đặc biệt. Overlay và root đều là tùy chọn.

### Thiếu hoặc sai cài đặt nhà mạng?

Hãy mở issue: [github.com/longnight/apn_settings_helper/issues](https://github.com/longnight/apn_settings_helper/issues)

Thông tin hữu ích: quốc gia/khu vực, tên nhà mạng, dòng SIM/gói cước/loại mạng, liên kết hướng dẫn APN chính thức, và giá trị bị thiếu hoặc sai. Đừng gửi thông tin tài khoản, số điện thoại, số serial SIM hoặc ảnh có thông tin cá nhân.

Nhà phát triển có thể thêm preset trực tiếp. Xem [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Dành cho nhà phát triển và người đóng góp

`v1.3.0` đã được phát hành. Các luồng chính gồm danh sách preset, màn hình chi tiết, sao chép thủ công, root apply tùy chọn, overlay helper tùy chọn, và bản địa hóa tiếng Anh/Nhật. Ứng dụng dùng giấy phép MIT, hướng tới FOSS và không phụ thuộc GMS.

### Hỗ trợ Android và công nghệ

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` cho root apply
- Một module Android: `:app`

Phiên bản dependency nằm trong [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### Môi trường, build và kiểm tra

Môi trường phát triển là pure-Nix flake cho Apple Silicon (`aarch64-darwin`), CLI-only, gồm JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt và `just`. Có thể sửa docs hoặc preset mà không cần cài môi trường này.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
```

| Lệnh | Nội dung | Cần emulator |
|---|---|---|
| `just test` | JVM tests + ktlint/detekt/lint không fatal | Không |
| `just ci` | Cổng CI nghiêm ngặt | Không |
| `just fmt` | Tự định dạng Kotlin | Không |
| `just emu-test` | Instrumented Android tests | Có |

[.github/workflows/ci.yml](../../.github/workflows/ci.yml) chạy `just ci` cho push và pull request.

### Preset, PR và phát hành

Preset nằm trong `app/src/main/assets/presets.json`. Hãy kiểm tra với trang APN chính thức của nhà mạng và thêm `source` / `lastVerified` khi có thể. Schema và checklist ở [CONTRIBUTING.md](../../CONTRIBUTING.md).

Trước PR, chạy `just ci` nếu có thể. Nếu không chạy được Android toolchain, nói rõ trong PR và mô tả bạn đã kiểm tra gì.

Không tuyên bố apply APN im lặng trên máy không root, không thêm AccessibilityService auto-fill, không probe root trước opt-in, không nói "in use" là xác minh APN thật, và giữ chuỗi tiếng Anh/Nhật đồng bộ.

Version nằm trong [app/build.gradle.kts](../../app/build.gradle.kts). Release signing dùng `keystore.properties` nếu có. F-Droid build phải xử lý `libsu` từ source vì JitPack không được phép.

### Giấy phép

[MIT](../../LICENSE).
