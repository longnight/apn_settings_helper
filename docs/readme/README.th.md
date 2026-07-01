# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | <a href="README.ar.md">العربية</a> | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | ไทย | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

APN Settings Helper เป็นแอป Android โอเพนซอร์สขนาดเล็กสำหรับกู้คืนการตั้งค่า APN ของข้อมูลมือถือจาก preset ที่ตรวจสอบแล้ว APN คือการตั้งค่าที่โทรศัพท์ใช้เชื่อมต่อข้อมูลมือถือและ MMS สำหรับ SIM ของคุณ

## สำหรับผู้ใช้

แอปนี้เหมาะกับผู้ใช้ MVNO / SIM ราคาประหยัด นักเดินทาง และโทรศัพท์ปลดล็อกที่ใช้ SIM เติมเงินท้องถิ่น รองรับญี่ปุ่นก่อน และสามารถเพิ่มภูมิภาคอื่นได้ในอนาคต

ถ้าข้อมูลมือถือหยุดทำงาน โทรศัพท์อาจสูญเสีย APN ที่ถูกต้อง แอปช่วยให้เลือก preset คัดลอกค่า และวางในตัวแก้ไข APN ของ Android ได้ง่ายขึ้น Android ไม่อนุญาตให้แอปทั่วไปเปลี่ยน APN แบบเงียบ ๆ ดังนั้นขั้นตอนหลักคือการตั้งค่าด้วยตนเองแบบมีคำแนะนำ เครื่องที่ root แล้วสามารถใช้การใช้ค่าโดยตรงแบบเลือกได้

### ทำได้และทำไม่ได้

- แสดง preset APN ที่ตรวจสอบแล้ว
- คัดลอกค่า APN ด้วยการแตะ
- แนะนำค่าของช่อง dropdown
- เปิดหน้าตั้งค่า APN ของระบบเมื่อ Android อนุญาต
- แสดงแผงช่วยเหลือเหนือหน้าจอแก้ไข APN ได้
- root apply ใช้ได้เฉพาะหลัง opt-in บนอุปกรณ์ที่ root
- ไม่เปลี่ยน APN อัตโนมัติบนเครื่องที่ไม่ root
- ไม่สามารถรู้แน่นอนว่า APN ใดใช้งานจริงอยู่

"Mark as in use" เป็นเพียงบันทึกในแอปเท่านั้น

### ดาวน์โหลดและใช้งาน

ดาวน์โหลด APK จาก [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases) ต้องใช้ Android 8.0 ขึ้นไป ภาพหน้าจอและวิดีโอสั้นจะเพิ่มภายหลัง

1. เปิดแอป
2. เลือกผู้ให้บริการและ preset
3. คัดลอกช่อง APN
4. แตะ "Open system APN editor"
5. สร้างหรือแก้ไข APN ใน Android Settings
6. วางค่า ตั้งค่า dropdown บันทึก และเลือก APN
7. ใช้ "Mark as in use" หากต้องการ

"Float over the APN editor" แสดงค่าและปุ่มคัดลอก แต่ไม่กรอกให้อัตโนมัติ

### ความเป็นส่วนตัวและการรายงาน

ไม่มีการเข้าถึงเครือข่าย ไม่มีบัญชี ไม่มีโฆษณา ไม่มีการติดตาม และไม่มีบริการเบื้องหลัง Overlay และ root เป็นตัวเลือก

หากข้อมูลผู้ให้บริการขาดหรือผิด ให้เปิด issue: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues) โปรดระบุประเทศ ผู้ให้บริการ SIM/แพ็กเกจ/เครือข่าย ลิงก์ APN ทางการ และค่าที่ผิด อย่าเผยแพร่ข้อมูลส่วนตัว

เพิ่ม preset ได้ผ่าน [CONTRIBUTING.md](../../CONTRIBUTING.md)

## สำหรับนักพัฒนา

`v1.4.0` เผยแพร่แล้ว มีรายการ preset หน้ารายละเอียด การคัดลอกแบบ manual, optional root และ optional overlay และ UI ได้รับการแปลเป็น **20 ภาษา** พร้อมตัวสลับภาษาในแอป (ไอคอนแปลภาษาบนแถบเครื่องมือเปิดเมนูภาษา) แอปใช้ MIT, เน้น FOSS และไม่มี GMS

### แพลตฟอร์มและเทคโนโลยี

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` สำหรับ root
- โมดูล `:app`

เวอร์ชัน dependency: [gradle/libs.versions.toml](../../gradle/libs.versions.toml)

### การพัฒนา

สภาพแวดล้อมเป็น pure-Nix flake สำหรับ Apple Silicon (`aarch64-darwin`) มี JDK 17, Android SDK 35, emulator, Gradle, ktlint, detekt และ `just` เอกสารและ preset แก้ไขได้โดยไม่ต้องติดตั้ง

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| คำสั่ง | จุดประสงค์ | Emulator |
|---|---|---|
| `just test` | JVM tests + lint ไม่ fatal | ไม่ |
| `just ci` | ตรวจ CI แบบเข้มงวด | ไม่ |
| `just fmt` | format Kotlin | ไม่ |
| `just emu-test` | Instrumented Android tests | ใช่ |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml)

Preset อยู่ที่ `app/src/main/assets/presets.json`; ตรวจสอบกับหน้า APN ทางการและเพิ่ม `source` / `lastVerified` Schema และ checklist อยู่ที่ [CONTRIBUTING.md](../../CONTRIBUTING.md)

ก่อน PR ให้รัน `just ci` หากทำได้ อย่าอ้างว่าสามารถใช้ APN แบบเงียบโดยไม่ root, อย่าเพิ่ม AccessibilityService auto-fill, อย่าตรวจ root ก่อน opt-in และ sync string EN/JA

เวอร์ชันอยู่ที่ [app/build.gradle.kts](../../app/build.gradle.kts) F-Droid ต้อง build `libsu` จาก source

### License

[MIT](../../LICENSE).
