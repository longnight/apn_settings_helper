# Contributing

Thanks for helping! The single most useful contribution is **adding or correcting an
APN preset** — that's a data-only change and needs no Android knowledge.

## Dev setup & checks

The toolchain is a pure-Nix flake (Apple Silicon / `aarch64-darwin`); see
[README](README.md) → *Development*. Before opening a PR, run the same gate CI runs:

```sh
just ci        # JVM tests + ktlint + detekt + Android lint (fatal on any issue)
just fmt       # auto-fix ktlint formatting
```

Preset edits are validated by `just ci` too: `BundledPresetsTest` loads the real
`presets.json` and the serializer validates it, so a malformed entry fails the build.

## Adding or fixing a preset

All presets live in **`app/src/main/assets/presets.json`**, grouped
`region → carrier → preset`:

```jsonc
{
  "schemaVersion": 1,
  "regions": [
    {
      "code": "JP",
      "name": { "en": "Japan", "ja": "日本" },
      "carriers": [
        {
          "id": "example-mobile",
          "name": { "en": "Example Mobile", "ja": "エグザンプルモバイル" },
          "presets": [
            {
              "id": "example-mobile-docomo",         // globally unique
              "label": { "en": "Example (Docomo line)", "ja": "エグザンプル（ドコモ回線）" },
              "apn": "example.ne.jp",
              "mcc": "440",
              "mnc": "10",
              "username": "user@example",             // omit if not needed
              "password": "example",
              "authType": "PAP_OR_CHAP",
              "protocol": "IPV4V6",
              "roamingProtocol": "IPV4V6",
              "mvnoType": "NONE",
              "apnType": "default,supl,mms",
              "notes": { "en": "", "ja": "" },
              "source": "https://example.com/apn",    // official page you verified against
              "lastVerified": "2026-06-27"            // YYYY-MM-DD
            }
          ]
        }
      ]
    }
  ]
}
```

Add your preset to the existing carrier if it's already listed; otherwise add a new
carrier object (new `id` + `name`), or a new region object (new `code` + `name`) for a
new country.

### Fields

| Field | Required | Notes |
|---|---|---|
| `id` | ✅ | Globally unique across all presets (kebab-case, e.g. `carrier-line`). |
| `label` | ✅ | `{ "en": …, "ja": … }` shown in the app. |
| `apn` | ✅ | Must be non-blank. |
| `mcc` | ✅ | Exactly **3 digits**. |
| `mnc` | ✅ | **2–3 digits**. |
| `username`, `password` | — | Omit (or `""`) if the carrier doesn't use them. |
| `authType` | — | `NONE` \| `PAP` \| `CHAP` \| `PAP_OR_CHAP` (default `NONE`). |
| `protocol`, `roamingProtocol` | — | `IPV4` \| `IPV6` \| `IPV4V6` (default `IPV4V6`). |
| `mvnoType` | — | `NONE` \| `SPN` \| `IMSI` \| `GID` (default `NONE`). `mvnoValue` pairs with it. |
| `apnType`, `proxy`, `port`, `mmsc`, `mmsProxy`, `mmsPort`, `server` | — | Optional; omit when empty. |
| `notes` | — | `{ "en": …, "ja": … }`; tips shown under the preset. |
| `source` | — | **Link the official carrier APN page you verified against.** |
| `lastVerified` | — | `YYYY-MM-DD` you checked it. |

Region `code` + `name`, and carrier `id` + `name`, are required for new
regions/carriers.

### Validation rules (enforced by the build)

- `schemaVersion` must be `1`.
- `id` (presets), `id` (carriers), and `code` (regions) must each be unique.
- `apn` non-blank; `mcc` 3 digits; `mnc` 2–3 digits.
- Enum fields must use a value from the lists above (otherwise JSON parsing fails).

### PR checklist

- [ ] Verified the values against the carrier's **official** APN page and set `source` + `lastVerified`.
- [ ] Provided both `en` and `ja` for `label` (and `notes` if used).
- [ ] `just ci` passes locally.

## Code & translations

- Code: keep `just ci` green; tests live alongside the code (see README → *Testing*).
- New UI language: add `app/src/main/res/values-<lang>/strings.xml` (translate every key
  in `values/strings.xml`) and, optionally, a `fastlane/metadata/android/<locale>/`
  store listing.

By contributing you agree your changes are licensed under the project's [MIT](LICENSE) license.
