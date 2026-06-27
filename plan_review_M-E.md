# M-E code-review follow-ups (root apply)

> Source: `/code-review max` on the M-E diff (`2e626d0`, the commit local `main` is ahead of `origin`),
> 2026-06-27. 10 finder angles → 46 candidates → 15 verified defects (6 refuted as pure style).
> The milestone's core works (root telephony write verified via `adb root`); these are hardening
> items. **Recommended: an "M-E.1 hardening" pass before release** for P1/P2; fold i18n items into M-F,
> cosmetics into M-H. Tick as done.
>
> **Status (2026-06-27): M-E.1 DONE** — all **P1 (3)** + **P2 (6)** fixed and tested (52 JVM tests,
> ktlint/detekt/android-lint clean); **P4.1** pulled forward (the WHERE now drives a destructive
> delete). **P3 (2)** folds into M-F; remaining **P4 (3)** → M-H.

## P1 — correctness / "false success" (a user can be told data is fixed when it isn't)
- [x] **Apply reports success even when activation failed** — `RootStrategy.apply()` returns `Applied`
      on INSERT success alone; `selectPreferredApn()` swallows a failed id-query / unparseable `_id`
      (`RootStrategy.kt:27,69-78`). Caption/AGENTS invariant is "write the APN **and** set it active".
      → distinguish outcomes (e.g. `Applied` vs a "written but not selected" result) and surface it.
      → DONE (M-E.1, 2026-06-27): added `ApplyOutcome.WrittenNotSelected`; `selectPreferredApn(rowId)`
      now returns Boolean → `Applied` only when selection succeeds, else `WrittenNotSelected`; VM maps
      it to a distinct `ApplyEvent.WrittenNotSelected` toast ("APN saved, but couldn't select it…").
- [x] **`content insert` exit 0 ≠ provider accepted** — on some OS versions `content insert` prints the
      SecurityException/unknown-column to stderr but still exits 0, so `insert.success` is true with no
      row written (`RootStrategy.kt:26`). → verify with a follow-up `content query` (count/`_id`) before
      reporting `Applied`.
      → DONE (M-E.1): `queryInsertedRowId()` reads the row back after insert; no `_id` ⇒ `Failed`
      (not `Applied`). Test `returns failed when the inserted row cannot be verified`.
- [x] **Eager root probe pops a `su` dialog on every detail-screen open** — `PresetDetailViewModel.init`
      → `applyResolver.resolve()` → `Shell.getShell()` (`PresetDetailViewModel.kt:88-92`,
      `ApplyStrategyResolver.kt:14`, `LibsuShellRunner.kt:17`). Violates the locked opt-in /
      "invisible until opened" design. → probe lazily (on first apply intent / behind a user toggle),
      not in `init`.
      → DONE (M-E.1): removed the `init` probe; added an opt-in **"One-tap apply (root)"** toggle —
      only flipping it on calls `setRootApplyEnabled(true)` → `probeRoot()`. Manual users never
      trigger `su`. Test `canApplyRoot is false until root apply is enabled`.

## P2 — robustness (crashes / stuck UI / lost feedback / dup data)
- [x] **Duplicate carrier rows on re-apply** — `buildInsertCommand` always INSERTs; re-applying the same
      preset (the app's core use case after silent APN loss) piles up rows, and `_id DESC` can select a
      stale dup (`RootStrategy.kt:14,35-66`). → delete/update matching `apn`+`mcc`+`mnc` before insert.
      → DONE (M-E.1): `buildDeleteCommand` deletes matching `apn`+`mcc`+`mnc` before the insert (and
      since dups are gone, `_id DESC` now reads back the row we just wrote). Test
      `deletes any prior copy of the preset before inserting`.
- [x] **`ShellResult.err` is always empty** — libsu `Shell.cmd(cmd).exec()` doesn't separate stderr
      (no `FLAG_REDIRECT_STDERR` / `.to(out,err)`), so every root failure shows the generic
      "Failed to write the APN" (`LibsuShellRunner.kt:22-23`). → configure the shell to capture stderr.
      → DONE (M-E.1): `Shell.cmd(cmd).to(out, err).exec()` captures stderr into `ShellResult.err`
      (compile-checked; libsu impl not unit-tested by design).
- [x] **`applyNow()` has no try/finally** — if `apply()` throws, `applying` never resets and the button
      is stuck disabled+spinner until VM recreation (`PresetDetailViewModel.kt:108-110`). → try/finally.
      → DONE (M-E.1): `try { … } catch { emit Failed } finally { applying.value = false }`.
- [x] **`init` root probe has no try/catch** — if `Shell.getShell()` throws, opening *any* detail screen
      crashes — including for manual users who never touch root (`PresetDetailViewModel.kt:88`).
      → catch → degrade to "no root".
      → DONE (M-E.1): probe moved out of `init` into `probeRoot()` with try/catch → degrades to
      "no root" (available = false) on any throw.
- [x] **Double-tap re-entrancy** — `applyNow()`'s only guard is async (`applying` set inside the launched
      coroutine; `enabled=!applying` lags a frame), so a fast double-tap fires two concurrent applies =
      two rows + two toasts (`PresetDetailViewModel.kt:104`). → synchronous guard.
      → DONE (M-E.1): synchronous `if (!applying.compareAndSet(false, true)) return` before the launch.
- [x] **Apply toast lost on rotation** — `applyEvents` is a `replay=0` SharedFlow; if the collector is
      torn down at emit time the toast is dropped (`PresetDetailScreen.kt:60`, `PresetDetailViewModel.kt:70`).
      → buffered/STATE channel or `Channel`-based events.
      → DONE (M-E.1): `applyEvents` is now a `Channel(BUFFERED).receiveAsFlow()` — a one-shot event
      is buffered until the collector re-subscribes.

## P3 — i18n (fold into M-F)
- [ ] **Failure toast not localized** — shows `event.message` (English/stderr) verbatim
      (`PresetDetailScreen.kt:65`, `RootStrategy.kt:23,30`). → map outcomes to string resources.
- [ ] **APN `name` column written English-only** — `preset.label.en` vs the localized name shown in-app;
      a JP user sees the English name in system Settings (`RootStrategy.kt:38`). → `label.resolve(tag)`.

## P4 — minor / cosmetic (M-H)
- [x] **SQL WHERE not escaped** — `apn`/`mcc`/`mnc` interpolated unescaped into the preferapn query;
      an apostrophe breaks it (low trigger — APNs are hostnames) (`RootStrategy.kt:70`).
      → DONE early (M-E.1): pulled forward from M-H because the WHERE now also drives a **destructive
      delete** (P2 dup-fix). `matchWhere()` builds the clause via `sqlLiteral()` (single-quoted,
      embedded quotes doubled). Remaining P4 items (RecordApplied dup control, field-knowledge
      dedup, formatter hoist) stay for M-H.
- [ ] **`RecordApplied` button also shown to root tier** — root auto-records on apply, so root users see
      two last-applied controls; spec splits auto (root) vs manual (`PresetDetailScreen.kt:229`).
- [ ] **`buildInsertCommand` duplicates per-field knowledge** — a 5th place that enumerates optional
      Preset fields (alongside `copyableFields`, `Preset`, `PresetDto`, `toDomain`); silent divergence
      risk when adding a column (`RootStrategy.kt:36`).
- [ ] **`buildState` rebuilds a `DateTimeFormatter` every emission** — title/notes re-resolved and a new
      formatter allocated on each of the 4 combined flows' emits (`PresetDetailViewModel.kt:74,139-145`).
      → hoist title/notes; cache the en/ja formatters in `ApnDateFormat`.

## Refuted (no action — pure style)
FakeShellRunner file placement · `strategy`/`rootAvailable` duplication · `CURRENT_TRUE` inline ·
`buildState` param shadow · `{ Unit }` branch verbosity · `apply()` re-checking `isRootAvailable()`.
