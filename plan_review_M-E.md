# Root Apply — Design Notes & Gotchas

Reference for the root (`libsu`) apply path. Code: `domain.apply.RootStrategy` (pure logic over the
`ShellRunner` seam) + `data.root.LibsuShellRunner` (the only libsu touchpoint).

## What it does (in order)
1. **Delete** any existing row matching `apn`+`mcc`+`mnc` — re-applying after a silent APN loss (the
   core use case) must not pile up duplicate rows.
2. **Insert** the preset into `content://telephony/carriers`.
3. **Verify** by reading the row back (`content query … _id`) — `content insert` can exit 0 **without
   writing** (it may print a SecurityException / unknown-column to stderr yet exit 0). No `_id` ⇒ fail.
4. **Select** it as preferred (`content insert … /preferapn --bind apn_id:i:<id>`).
- **Outcomes:** `Applied` (written **and** selected) · `WrittenNotSelected` (written, selection failed
  — its own toast) · `Failed`. The "write **and** set active" invariant is why a failed selection is
  surfaced, not swallowed.

## Provider mapping (camelCase preset → `carriers` columns)
- `numeric` = `mcc` + `mnc`; `current:i:1`.
- `authtype` (int): NONE/PAP/CHAP/PAP_OR_CHAP → 0/1/2/3.
- `protocol` / `roaming_protocol`: IPV4/IPV6/IPV4V6 → `IP` / `IPV6` / `IPV4V6`.
- `mvno_type`: spn/imsi/gid (with `mvno_match_data`; omitted entirely when NONE).
- `name` = `label.resolve(languageTag)` (localized, so a JP user sees the JP name in system Settings).
- Blank optional fields are omitted. **Adding a column?** wire it into `buildInsertCommand` — the test
  `insert includes every populated optional field` guards against silent divergence.

## Invariants & fragile bits
- **Opt-in only:** probe `su` (which may pop the superuser dialog) **only** when the user enables the
  root toggle (`setRootApplyEnabled` → `probeRoot`), never in `init` — preserves "invisible until
  opened". The probe is wrapped in try/catch and degrades to "no root" on any throw.
- **Destructive delete** ⇒ the WHERE clause is SQL-escaped (`matchWhere` / `sqlLiteral`).
- **UI safety:** synchronous `applying.compareAndSet(false, true)` blocks double-tap; `applyNow` uses
  try/catch/finally so a throw can't wedge the button; apply events use a buffered `Channel` so a toast
  survives a rotation.
- **`LibsuShellRunner` is not unit-tested** (it's the thin libsu binding; manually verified below).
  `Shell.cmd(cmd).to(out, err)` captures stderr so failures report a real reason.

## Manual verification on a `google_apis` emulator (no app-level `su`)
The in-app button won't show (libsu `isRoot` = false); verify the command shape via `adb root`:
```sh
adb root
adb shell content insert --uri content://telephony/carriers \
  --bind name:s:… --bind apn:s:… --bind numeric:s:44010 \
  --bind authtype:i:3 --bind protocol:s:IPV4V6 --bind current:i:1 …
adb shell content query  --uri content://telephony/carriers --projection _id --where "apn='…'"
adb shell content insert --uri content://telephony/carriers/preferapn --bind apn_id:i:<id>
```
- **Caveat:** `preferapn` only takes effect when the APN's MCC/MNC matches the active SIM, so selecting
  a JP (440/xx) APN on the emulator's T-Mobile (310/260) SIM is correctly ignored — the row write
  itself still succeeds.
