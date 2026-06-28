# Float-over-editor Overlay — Clipboard & UX Improvements (2026-06-28)

> **Living doc. Keep updating the Progress Log + Status as work proceeds.** This is the re-entry
> guide for the floating-overlay work stream — a fresh agent in a new chat should be able to read
> this top-to-bottom and continue without the prior conversation.

## TL;DR / current status
- **Goal (today's main task):** make the **overlay "Float over the APN editor"** feature genuinely
  useful, easy, and functional. User directive: **try Option #2 (salvage Copy via focusable-toggle)
  first, then Option #1 (read-only floating panel)**.
- **Where we are:** ✅ **DONE & verified on the Xiaomi/MIUI device. `just ci` passes** (tests + ktlint +
  detekt + Android lint). The overlay draws over the system APN editor; **multi-field Copy works reliably on
  MIUI** via a per-copy hybrid — silent write + read-back verify + foreground-`ClipboardWriteActivity` fallback
  (see [the clipboard crux](#the-hard-constraint-discovered-on-device-the-crux)); the panel is polished (collapse
  toggle, hint line, dimmed dropdown-hint rows, permission auto-continue). Code: `ApnOverlay` (controller) +
  `OverlayPanel` (view builder) + `ClipboardWriteActivity` (foreground writer).
- **Next action:** see [Remaining work](#remaining-work). Docs reclassified ✅, flash-UX assessed ✅ (the
  foreground flash is imperceptible — no visible frame, ~13ms focus blip, survives an open field-dialog), and
  the work is committed to `feat/float-over-editor-overlay` ✅. Only **non-MIUI verification** (blocked — no
  device) and an optional **PR/merge** of the branch remain. The feature itself is done.

## Remaining work
- [x] **Docs (DONE 2026-06-28):** `AGENTS.md` — overlay reclassified (apply-tiers bullet → implemented
  float-over-editor manual-assist helper w/ `SYSTEM_ALERT_WINDOW`, focusable-toggle, "not via `ApplyStrategy`",
  `OverlayStrategy` still a stub; permissions line; `ui.{…,overlay,…}`; Deferred section). `docs/plan_coding_steps.md`
  — `ui.overlay/` added to the package map, a float-overlay bullet under Apply strategy, and the Tech-debt
  "Overlay full implementation" reclassified. `README.md` — added the opt-in floating-overlay bullet under
  "Why this exists".
- [ ] **Non-MIUI verification (BLOCKED — no device):** confirm Copy works on stock Android / Samsung / Pixel.
  There the *silent* path should usually win (no flash); the foreground fallback is just the safety net. User
  has no non-MIUI device available (2026-06-28), so this stays open until one is.
- [x] **Foreground-flash UX (DONE 2026-06-28, cont. 5 — verified on the Xiaomi/MIUI):** the
  `ClipboardWriteActivity` flash is **imperceptible**. Measured via the `events` logcat buffer: first Copy after
  the app was foregrounded is **silent** (no `ClipboardWriteActivity` at all — proxy write stuck, focus never
  left Settings); each later Copy launches `ClipboardWriteActivity` which the WM logs as
  `translucent=true visible=false visibleRequested=false` (it **never requests a visible frame**), holds focus
  only **~13ms** (create→finish ~45ms, create→destroy ~120ms incl. teardown), and `mCurrentFocus` returns
  cleanly to the Settings editor. No visible flash frame is drawn.
- [x] **Mechanism docs (DONE, cont. 4):** updated `AGENTS.md` + `docs/plan_coding_steps.md` clipboard-write
  wording from the (wrong) "focusable-toggle" to the hybrid (silent proxy → read-back → foreground fallback).
- [x] **Edge case — copy while a field-dialog is open (DONE 2026-06-28, cont. 5 — the dialog SURVIVES):**
  tested on the Xiaomi with the MIUI APN editor's MMSC field-edit dialog open (keyboard up): tapping an overlay
  COPY fired the flash, yet the dialog stayed open (Cancel/OK + IME intact, `mInputMethodWindow` unchanged),
  the copied value reached the clipboard (IME clip-chip showed it), and paste (`keyevent 279`) inserted it into
  the field. The flash neither dismisses the dialog nor breaks the copy/paste. **Still open:** the overlay
  surviving process death without a service (host option #1 — escalate to a transient FGS only if it proves
  unreliable in real use).
- [ ] **Tests:** the overlay is Views/Android (not JVM-unit-testable) — treat like `LibsuShellRunner`
  (manually verified). Consider an instrumented test for the `FloatOverEditorButton` permission branch only.
- [x] **Commit (DONE 2026-06-28, cont. 5):** committed the work stream to branch `feat/float-over-editor-overlay`
  (off `main`, not pushed). Open: whether to PR/merge it. ComposeView-vs-Views for the panel (Views chosen for
  robustness — revisit only if theme parity matters) stays a non-blocking maybe.

## Files changed (this work stream)
- NEW `app/src/main/kotlin/io/github/ln/apnsettingshelper/ui/overlay/ApnOverlay.kt` — controller (window
  add/remove + the per-copy hybrid clipboard write: silent proxy-window write → read-back verify → foreground
  fallback + `Row`/`PanelSpec`).
- NEW `app/src/main/kotlin/io/github/ln/apnsettingshelper/ui/overlay/OverlayPanel.kt` — `buildOverlayPanel`
  view builder (draggable header w/ collapse + close, hint line, field rows).
- NEW `app/src/main/kotlin/io/github/ln/apnsettingshelper/ui/overlay/ClipboardWriteActivity.kt` — invisible
  one-shot Activity that writes the clipboard as the **foreground** app (the hybrid's reliable fallback).
- `app/src/main/AndroidManifest.xml` — `SYSTEM_ALERT_WINDOW` permission + `ClipboardWriteActivity`
  (`taskAffinity=""`, `noHistory`, `excludeFromRecents`, the invisible theme).
- `app/src/main/res/values/themes.xml` — `Theme.ApnSettingsHelper.Invisible` (translucent, no-animation) for
  the writer Activity.
- `app/src/main/res/values/strings.xml` + `values-ja/strings.xml` — `float_over_settings`,
  `overlay_permission_needed`, `overlay_close`, `overlay_collapse`, `overlay_expand`, `overlay_hint`.
- `app/src/main/kotlin/.../ui/detail/PresetDetailScreen.kt` — `FloatOverEditorButton` (entry point, permission
  auto-continue via `rememberLauncherForActivityResult`, inline row mapping) + `title` threaded into `PresetDetailBody`.
- NEW `docs/plan_20260628_clipboard_float_overlay_improvements.md` — this doc.

## Background / why (read these first)
- Product + tier rationale: `AGENTS.md`. Architecture/file map: `docs/plan_coding_steps.md`.
  Dev env: `docs/plan_implement_steps.md`.
- The app restores a phone's mobile-data **APN** from curated presets. Modern Android blocks 3rd-party
  APN writes (`WRITE_APN_SETTINGS` is `signature|privileged`), so apply happens in tiers:
  **Manual** (copy buttons + open system APN editor), **Root** (one-tap, opt-in), **Overlay** (was a v1 stub).
- This work stream **implements the Overlay tier** as a panel that floats the preset's values over the
  **system APN editor** so the user fills it without bouncing back to our app. Originally framed as the
  deferred "overlay full implementation" in `AGENTS.md`.
- **Overlay cannot auto-fill the editor** (would need AccessibilityService, which `AGENTS.md` rejects).
  The most it can do is *display* values and *copy to clipboard* for manual paste.

## The hard constraint discovered on-device (the crux)
**On MIUI (Xiaomi), `ClipboardManager.setPrimaryClip()` called from the overlay is silently dropped.**
- No exception is thrown — the write just doesn't take; the clipboard keeps its previous content.
- Root cause: Android 10+ restricts clipboard access for non-foreground apps; MIUI enforces it hard.
  Our overlay window is **not** the focused/foreground app (the Settings editor is), so the write is blocked.
- Corroborating: MIUI also **suppresses our toasts** (`NotificationService: Suppressing toast from
  package io.github.ln.apnsettingshelper by user request`) — it treats the overlay app as background.
- **Audience impact:** MIUI/Xiaomi is a large share of this app's exact users (budget unlocked phones,
  travelers, MVNO SIMs). "Breaks on MIUI" is a core-audience problem, not an edge case.
- **Unknown:** whether stock Android / Samsung / Pixel also drop the write (only Xiaomi tested so far).
  The WRITE restriction may be MIUI-aggressive; stock may allow it. Not yet verified.
- **RESOLVED (2026-06-28) — via a hybrid; the focus theory was WRONG.** On-device logging proved the write
  is called **while our window genuinely holds focus** (`hasWindowFocus()==true`) yet still only the **first**
  clipboard *change* after the app was last foreground sticks — every later Copy is silently dropped, even with
  a fresh focusable window and even ~4s later (a 6-field timing sweep: only the 1st stuck). So MIUI gates writes
  on **being the foreground app**, not window focus; no purely-background trick (focus edge, focus poll, fresh
  proxy window, waiting) survives repeated copies. **The fix is a per-copy hybrid** (`ApnOverlay.copy` →
  `writeViaProxy`): (1) try a *silent* write from a fresh 1×1 invisible focusable overlay window; (2) **read the
  clip back** while still focused to check it stuck; (3) if not, write as the genuine **foreground app** via a
  transient invisible `ClipboardWriteActivity` (translucent, no-animation, finishes in `onCreate` — a brief,
  near-invisible flash; allowed from the background because we hold `SYSTEM_ALERT_WINDOW`). Net: silent on
  lenient devices (and MIUI's first copy), reliable everywhere else. **Verified on the Xiaomi:** 6 fields copied
  with 7–12s gaps all paste correctly (1 silent + 5 via the foreground fallback); read-back reliably tells
  stuck from dropped.
- **Earlier dead ends (don't retry):** writing on the `onWindowFocusChanged(true)` edge, polling
  `hasWindowFocus()` then writing, and a fresh focusable proxy window per copy — **all** appeared to work
  (focus confirmed) but the clipboard kept the old value for every copy after the first. The only reliable
  background-write detector is reading the clip back; the only reliable *write* is the foreground Activity.

## What's implemented so far (the spike)
Host approach = **Option #1 from the design: no service** (AGENTS.md host decision — user OK'd
"no-service first, escalate to a transient foreground service only if the panel gets killed").
Overlay UI = **classic Android Views** (not Compose) to avoid `ComposeView`-in-WindowManager lifecycle
plumbing for the spike.

Files added/changed:
- **`app/src/main/kotlin/io/github/ln/apnsettingshelper/ui/overlay/ApnOverlay.kt`** (NEW) — `object ApnOverlay`:
  - `canDraw(context)` → `Settings.canDrawOverlays`. `permissionIntent(context)` → `ACTION_MANAGE_OVERLAY_PERMISSION`.
  - `show(context, title, rows)` adds a Views panel to the **application** `WindowManager` with
    `TYPE_APPLICATION_OVERLAY` + `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL`, `hide(context)` removes it.
  - Panel: dark rounded card, draggable by header, ✕ close, one row per field. `Row(label, value, copyable)` —
    copyable rows get a COPY button (`setPrimaryClip` + toast); dropdown fields render as `label → value` hints.
- **`AndroidManifest.xml`** — added `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`.
- **`res/values/strings.xml`** + **`res/values-ja/strings.xml`** — added `float_over_settings`,
  `overlay_permission_needed`, `overlay_close` (keep en/ja in sync — Android lint `MissingTranslation` gates `just ci`).
- **`ui/detail/PresetDetailScreen.kt`** — added `FloatOverEditorButton(preset, title)` (next to
  `OpenApnEditorButton`) + `overlayRows(context, preset)` (maps `copyableFields`/`checklistFields` →
  `ApnOverlay.Row`); threaded `title = state.title` into `PresetDetailBody`.

Not yet done: unit tests for the new code, lint/`just ci` pass, docs in `AGENTS.md`/`plan_coding_steps.md`,
Compose-vs-Views final decision, collapse-to-bubble, proper permission-return handling (currently
"grant then tap again").

## Device test findings (2026-06-28, Xiaomi 2304FPN6DC, Android 13 / MIUI, SDK 33)
- ✅ "Float over the APN editor" button renders on the preset detail screen.
- ✅ Permission request flow works: deep-links to "Display over other apps" + shows the toast.
- ✅ Overlay **draws over the live system APN editor** (`Settings$ApnSettingsActivity`) — verified visually;
  system posts `AlertWindowNotification` for our package (overlay added OK). No crash / no `BadTokenException`.
- ✅ Drag (header) and ✕-dismiss work.
- ❌ **COPY buttons don't write the clipboard** — tapped COPY, pasted into the editor's APN field →
  got the device's *previous* clipboard text, not the preset value. `setPrimaryClip` silently no-ops.
- ❌ Toasts suppressed by MIUI.

## Plan — two tracks (#2 then #1)
### Option #2 — salvage Copy via focusable-toggle ✅ DONE (works on MIUI)
On COPY tap: clear `FLAG_NOT_FOCUSABLE` (`wm.updateViewLayout`) so our window gains focus, write the clip
**on `onWindowFocusChanged(true)`** (NOT a fixed delay — that raced the focus grant), then restore
`FLAG_NOT_FOCUSABLE`. Feedback = the button flips to "✓" (toast is MIUI-suppressed). Implemented in
`ApnOverlay.copy()`. **Verified on the Xiaomi:** copied MCC `440` → pasted `440` into the editor.
- Focus-steal cost is tiny (~34ms in/out, per logcat) and not noticeable.
- Still-open: confirm on non-MIUI; behaviour if a field dialog is already open when COPY is tapped.

### Option #1 — read-only floating panel (DO AFTER, regardless of #2)
Make the panel a clean, reliable **cheat-sheet** that works on every device incl. MIUI:
- Large, legible values; clear field labels; the dropdown "→ value" hints stay.
- **Collapse-to-bubble** affordance so it doesn't cover the field being edited; draggable; easy dismiss.
- If #2 succeeded, keep Copy as a bonus on top of the display; if not, display-only (no misleading dead buttons).
- Decide Compose-vs-Views for the final panel (Views = robust/no lifecycle plumbing; Compose = theme parity
  but needs `setViewTree*Owner` on the `ComposeView`).

### Cross-cutting polish (either track)
- Permission-return UX: use an `ActivityResultLauncher` to re-check `canDrawOverlays` on return instead of
  "grant then tap again".
- The panel currently relies on toasts for "Copied" feedback — **MIUI suppresses them**; use in-overlay
  visual feedback (e.g. button flips to "Copied ✓") instead.
- Honest labeling: don't imply auto-fill.

## How to build / install / test on device (devShell)
**Toolchain lives in the Nix devShell** — `adb`, Gradle, SDK are NOT on the host PATH. Prefix every command:
```sh
source /nix/var/nix/profiles/default/etc/profile.d/nix-daemon.sh \
  && cd /Users/fj/temp/prj/others/apn_settings_helper \
  && nix develop --command bash -c '<cmd>'
```
- **Build APK:** `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
  (assembleDebug compiles only — does NOT run ktlint/detekt/lint; run `just ci` before declaring done.)
- **Install gotcha:** the device may already have the **release-signed** app → `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
  Fix: `adb uninstall io.github.ln.apnsettingshelper` then `adb install app/build/outputs/apk/debug/app-debug.apk`.
  (User chose uninstall+install over a debug applicationId suffix; losing favorites/last-applied is acceptable test data.)
- **Launch:** `adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity`.
- **Grant overlay permission without UI** (MIUI's toggle is fiddly to automate): 
  `adb shell appops set io.github.ln.apnsettingshelper SYSTEM_ALERT_WINDOW allow` (verify: `appops get …`).
- **Screenshot-driven UI testing** (no human needed for taps): `adb exec-out screencap -p > shot.png`, then
  Read the PNG. Drive taps with `adb shell input tap X Y`. **Screenshots are often captured mid-transition —
  re-capture if you see an animation.** `adb shell dumpsys window | grep mCurrentFocus` tells you the focused
  activity. Long-press = `adb shell input swipe X Y X Y 700`. Paste into focused field = `adb shell input keyevent 279`.
- **Coordinate mapping:** the Read tool shows screenshots downscaled and prints the multiplier
  (e.g. "displayed 900x2000, original 1440x3200, multiply by 1.60"). `adb` taps use **original device** coords.

### Device under test
- Xiaomi **2304FPN6DC**, **Android 13** (SDK 33), MIUI. transport_id may change between sessions — re-run
  `adb devices -l`. Connect = user plugs in USB + enables USB debugging + authorizes the host key.

## Key references (code)
- `domain/apply/ApplyStrategy.kt`, `OverlayStrategy.kt` (the old v1 stub), `ApplyStrategyResolver.kt` — the
  apply seam. NOTE: the float overlay is wired as a **UI-layer helper** (like `ui/common/ApnEditor.openApnEditor`),
  NOT routed through the resolver (it assists manual copy, it's not a programmatic apply tier).
- `ui/detail/DetailFields.kt` — `copyableFields(preset)` / `checklistFields(preset)` (the field model reused by the overlay).
- `ui/common/Fields.kt` — `CopyableField` / `ChecklistItem` (in-app copy UI, for reference/parity).

## Open questions / risks
- Does the clipboard write fail on **stock Android / Samsung / Pixel** too, or only MIUI? (untested)
- Does the focusable-toggle (#2) actually unblock the write on MIUI, and is the focus-steal acceptable?
- Is `SYSTEM_ALERT_WINDOW` worth it for a *display-only* panel (the scary permission may deter users)?
- Play-compatibility: `SYSTEM_ALERT_WINDOW` is allowed but policy-sensitive (distribution is F-Droid + GitHub first).
- Bigger-picture: is the overlay the right bet vs. smoothing in-app copy / root one-tap? (user chose to pursue overlay.)

## Progress log
- **2026-06-28** — Investigated original report ("release APK can't show the layer"): the Overlay tier was a
  deliberate v1 stub (no perm, resolver off, no UI). User clarified the want: **floating value hints + per-field
  Copy buttons** over the system editor. Built the spike (files above). Tested on Xiaomi/MIUI: overlay draws over
  the editor ✅, but Copy fails (clipboard write dropped) ❌. User directive: **do Option #2 then Option #1** to
  make it useful/easy. Wrote this doc. **Next: implement Option #2 (focusable-toggle).**
- **2026-06-28 (cont.)** — Implemented Option #2. First cut (fixed `postDelayed(120ms)`) FAILED on MIUI —
  paste still showed stale clipboard (timing race vs the focus grant). Reworked to write on
  `ViewTreeObserver.OnWindowFocusChangeListener` (write the instant our window gains focus) → **WORKS**:
  copied MCC `440` → pasted `440` into the system APN editor. Copy buttons are now functional on MIUI, with
  in-overlay "✓" feedback. **Next: Option #1 — polish the panel (legibility, collapse/relocate to not cover
  the field, permission auto-continue) + clean up lint/tests, then `just ci`.**
- **2026-06-28 (cont. 2)** — Implemented Option #1 + polish, all **verified on the Xiaomi/MIUI device**:
  collapse/expand toggle (shrinks the panel to its title bar so you can reach the editor field), a hint line,
  dimmed dropdown-hint rows, and permission **auto-continue** (`rememberLauncherForActivityResult` re-checks
  `canDrawOverlays` on return — no "tap again"). Refactored the overlay into `ApnOverlay` (controller) +
  `OverlayPanel` (view builder) to satisfy detekt (function/param counts) and for clean separation.
  **`just ci` passes** (tests + ktlint + detekt + Android lint). Feature is functionally complete & on-device.
  Observed MIUI quirks (documented above): revokes overlay perm on app update; suppresses our toasts; resets
  the appop shortly after install (re-grant raced the install). **Next: docs + non-MIUI check (see Remaining work).**
- **2026-06-28 (cont. 3)** — **Docs reclassification done.** Updated `AGENTS.md`, `docs/plan_coding_steps.md`,
  and `README.md` to move the overlay from "deferred v1 stub" to an **implemented float-over-editor
  manual-assist helper** — keeping the key distinction that it's a **UI-layer helper** (invoked like
  `openApnEditor`, *not* routed through `ApplyStrategy`) while the `OverlayStrategy` seam slot stays a stub
  for a future *programmatic* tier. No code change. **Next: non-MIUI device verification, then the commit
  decision (both await the user).**
- **2026-06-28 (cont. 4)** — **Multi-field Copy bug found & fixed (user report: "other fields' Copy keeps the
  old 440").** Root-caused on-device with read-back logging: the earlier focusable-toggle/poll/proxy approaches
  all wrote *while focused* yet only the **first** clipboard change after the app was foreground stuck — MIUI
  gates writes on being the *foreground app*, not focus (6-field timing sweep: only the 1st stuck; +4s already
  fails). Per the user's "#3 (silent) then #1 (foreground flash)" directive: tried the fully-silent
  fresh-proxy-window approach (#3) — **failed** for repeat copies. Implemented the **hybrid** (#3 + #1 per
  copy): silent proxy write → read-back verify → fall back to the foreground `ClipboardWriteActivity` (new;
  invisible/translucent/no-anim). **Verified on the Xiaomi:** stress test of 6 fields with 7–12s gaps all paste
  correctly (1 silent + 5 foreground), plus spot-pastes of `10`, `default,supl,ia`, `mineo@k-opti.com`. Removed
  debug logging; **`just ci` passes**. Also fixed the README status blurb (v1 in-dev → code-complete) and
  updated AGENTS.md/plan_coding_steps clipboard-mechanism wording. **Next: non-MIUI check + flash-UX assessment
  + commit decision (await the user).**
- **2026-06-28 (cont. 5)** — **Flash-UX assessment + edge case done (verified on the Xiaomi/MIUI), then
  committed.** User had no non-MIUI device, so skipped that check and did the flash-UX + commit. Measured the
  `ClipboardWriteActivity` flash via the `events` logcat buffer: 1st Copy after foregrounding is **silent** (no
  `ClipboardWriteActivity`; focus stayed on Settings); later Copies launch it but the WM logs
  `translucent=true visible=false visibleRequested=false` (no visible frame), focus held ~13ms, returns cleanly
  to the editor → **imperceptible**. Edge case (copy while the MMSC field-dialog is open): the **dialog
  survives** (IME stays up, copied value lands, paste inserts it) — the flash does not dismiss it. `just ci`
  green. Committed the whole work stream to branch **`feat/float-over-editor-overlay`** (off `main`; not pushed).
  **Next: optional non-MIUI verification when a device is available; possible PR/merge of the branch.**
