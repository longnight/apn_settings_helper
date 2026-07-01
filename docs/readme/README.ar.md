# APN Settings Helper

[![CI](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml/badge.svg)](https://github.com/longnight/apn_settings_helper/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../../LICENSE)

<p align="center" dir="rtl">
  <a href="../../README.md">日本語</a> | <a href="README.en.md">English</a> | <a href="README.vi.md">Tiếng Việt</a> | <a href="README.zh-CN.md">简体中文</a> | <a href="README.zh-TW.md">繁體中文</a> | <a href="README.ko.md">한국어</a> | <a href="README.de.md">Deutsch</a> | <a href="README.es.md">Español</a> | <a href="README.fr.md">Français</a> | <a href="README.it.md">Italiano</a> | <a href="README.da.md">Dansk</a> | <a href="README.pl.md">Polski</a> | <a href="README.bs.md">Bosanski</a> | العربية | <a href="README.no.md">Norsk</a> | <a href="README.pt-BR.md">Português (Brasil)</a> | <a href="README.th.md">ไทย</a> | <a href="README.tr.md">Türkçe</a> | <a href="README.km.md">ភាសាខ្មែរ</a> | <a href="README.uk.md">Українська</a>
</p>

<div dir="rtl">

APN Settings Helper هو تطبيق Android صغير ومفتوح المصدر لاستعادة إعدادات APN لبيانات الهاتف من إعدادات مسبقة تم التحقق منها. APN هو الإعداد الذي يستخدمه الهاتف لتشغيل بيانات الهاتف وMMS عبر شريحة SIM.

## للمستخدمين

التطبيق موجه لمستخدمي MVNO / شرائح SIM الاقتصادية، والمسافرين، والهواتف غير المقفلة مع شريحة محلية مسبقة الدفع. الدعم يبدأ باليابان ويمكن إضافة مناطق أخرى.

إذا توقفت بيانات الهاتف، فقد يكون الهاتف فقد إعداد APN الصحيح. يساعدك التطبيق على اختيار إعداد مسبق، نسخ القيم، ولصقها في محرر APN في Android. Android لا يسمح للتطبيقات العادية بتغيير APN بصمت، لذلك المسار الأساسي يدوي. أجهزة root يمكنها استخدام تطبيق مباشر اختياري.

### ما يمكنه وما لا يمكنه

- يعرض إعدادات APN مسبقة ومتحقق منها.
- ينسخ قيم APN بنقرة واحدة.
- يعطي قيم القوائم المنسدلة.
- يفتح إعدادات APN في النظام عندما يسمح Android.
- يمكنه إظهار لوحة فوق محرر APN.
- root apply يعمل فقط بعد موافقة المستخدم على جهاز root.
- لا يغيّر APN تلقائيا بدون root.
- لا يعرف بشكل مؤكد أي APN نشط فعلا.

"Mark as in use" مجرد ملاحظة داخل التطبيق.

### التنزيل والاستخدام

نزّل APK من [GitHub Releases](https://github.com/longnight/apn_settings_helper/releases). يتطلب Android 8.0 أو أحدث. ستضاف لقطات شاشة وفيديو قصير لاحقا.

1. افتح التطبيق.
2. اختر شركة الاتصال والإعداد.
3. انسخ حقول APN.
4. اضغط "Open system APN editor".
5. أنشئ أو عدّل APN في Android Settings.
6. الصق القيم، اضبط القوائم المنسدلة، احفظ واختر APN.
7. استخدم "Mark as in use" إذا أردت.

"Float over the APN editor" يعرض القيم وأزرار النسخ فقط، ولا يملأ الحقول تلقائيا.

### الخصوصية والتبليغ

لا شبكة، لا حسابات، لا إعلانات، لا تتبع، ولا خدمة خلفية. Overlay وroot اختياريان.

للإبلاغ عن إعدادات ناقصة أو خاطئة: [GitHub Issues](https://github.com/longnight/apn_settings_helper/issues). اذكر البلد، الشركة، SIM/الخطة/الشبكة، رابط APN الرسمي، والقيمة الخاطئة. لا تنشر معلومات شخصية.

يمكن إضافة الإعدادات عبر [CONTRIBUTING.md](../../CONTRIBUTING.md).

## للمطورين

تم إصدار `v1.4.0`. تم تنفيذ قائمة الإعدادات، التفاصيل، النسخ اليدوي، root اختياري، وoverlay اختياري، وتُرجمت الواجهة إلى **20 لغة** مع مبدّل لغة داخل التطبيق (أيقونة ترجمة في شريط الأدوات تفتح قائمة اللغات). الترخيص MIT، موجه FOSS، بدون GMS.

### المنصة والتقنيات

- Application ID: `io.github.ln.apnsettingshelper`
- Minimum SDK: 26
- Compile/Target SDK: 35
- Kotlin, Jetpack Compose, Material 3, Compose Navigation
- DataStore Preferences, kotlinx.serialization, coroutines
- `libsu` للـ root
- الوحدة `:app`

إصدارات الاعتمادات: [gradle/libs.versions.toml](../../gradle/libs.versions.toml).

### التطوير

بيئة التطوير pure-Nix flake لـ Apple Silicon (`aarch64-darwin`) مع JDK 17 وAndroid SDK 35 ومحاكي وGradle وktlint وdetekt و`just`. يمكن تعديل الوثائق والإعدادات بدون تثبيتها.

```sh
nix develop
./gradlew :app:assembleDebug
just emu-create
just emu
```

| الأمر | الغرض | محاكي |
|---|---|---|
| `just test` | JVM tests + lint غير قاتل | لا |
| `just ci` | فحص CI صارم | لا |
| `just fmt` | تنسيق Kotlin | لا |
| `just emu-test` | Instrumented Android tests | نعم |

CI: [.github/workflows/ci.yml](../../.github/workflows/ci.yml).

الإعدادات في `app/src/main/assets/presets.json`; تحقق من صفحة APN الرسمية وأضف `source` / `lastVerified`. Schema وchecklist في [CONTRIBUTING.md](../../CONTRIBUTING.md).

قبل PR شغّل `just ci` إن أمكن. لا تدّع APN صامت بدون root، لا تضف AccessibilityService auto-fill، لا تفحص root قبل opt-in، وحافظ على مزامنة نصوص EN/JA.

الإصدار: [app/build.gradle.kts](../../app/build.gradle.kts). يجب أن يبني F-Droid `libsu` من المصدر.

### الرخصة

[MIT](../../LICENSE).

</div>
