# APN Settings Helper — Dev Environment Reference

Reproducible, CLI-only Android toolchain via a **pure-Nix flake** (Apple Silicon / `aarch64-darwin`).
No Android Studio. Everything is pinned by `flake.lock`.

## What the devShell provides
JDK 17 · Android SDK (platform 35, build-tools 35.0.0, platform-tools) · native arm64 emulator +
`apnhelper` AVD (`system-images;android-35;google_apis;arm64-v8a`) · Gradle 8.14.4 · ktlint · detekt ·
kotlin-language-server · just. Sets `ANDROID_HOME`/`JAVA_HOME`. Footprint ≈ 12 GB (`/nix` ~11 GB on its
own APFS volume + the AVD ~0.8 GB).

## Entering it
- **Interactive:** `cd` into the repo (direnv auto-loads via `.envrc`) or run `nix develop`.
- ⚠️ **Non-interactive / one-shot tool calls:** direnv does not auto-activate and `nix` isn't on PATH —
  prefix every command:
  ```sh
  source /nix/var/nix/profiles/default/etc/profile.d/nix-daemon.sh \
    && cd <repo> && nix develop --command bash -c '<cmd>'
  ```
  (flake eval is cached, ~2 s/call).

## Commands (`just`)
- `just ci` — strict gate: JVM tests + ktlint + detekt + Android lint (fatal). **Run before pushing.**
- `just test` — same checks, linters non-fatal (fast local iteration). `just fmt` — ktlint autofix.
- `just emu` — boot `apnhelper` (windowed). `just emu-test` — boot headless → `connectedAndroidTest` →
  teardown. `just emu-kill`.
- Build / install / launch:
  ```sh
  ./gradlew :app:assembleDebug
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  adb shell am start -n io.github.ln.apnsettingshelper/.MainActivity
  ```
- Headless boot + wait:
  ```sh
  emulator -avd apnhelper -no-window -no-audio -no-snapshot -gpu swiftshader_indirect &
  adb wait-for-device
  until [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; do sleep 2; done
  ```

## Decisions & gotchas
- **`google_apis` image ⇒ `adb root` works** (needed to verify the root-apply path) — but it gives
  `adb root`, **not** app-level `su`, so the in-app "Apply now" button doesn't appear on this AVD
  (libsu `isRoot` = false). Verify root writes via an `adb root` shell (`plan_review_M-E.md`), or use a
  Magisk image for the in-app button.
- **Keep `apnhelper` at `en-US`.** Instrumented tests assert English strings; if its system locale is
  switched (e.g. to capture ja screenshots), reset it (`setprop persist.sys.locale en-US` + reboot) or
  `just emu-test` fails.
- **SDK provider:** `tadfisher/android-nixpkgs` (in-tree `androidenv` has an open aarch64-darwin gap).
- **API-35 pin:** the devShell ships only platform/build-tools 35 and v1 locks compileSdk 35 — to move
  to newer AndroidX/Compose, bump `flake.nix` to platform/build-tools 36 first.
- **Flake targets `aarch64-darwin` only** — that's why CI runs on a `macos-14` (arm64) runner with Nix
  (`.github/workflows/ci.yml`). Porting CI to `ubuntu` needs `flake.nix` to add `x86_64-linux`.

## Teardown
The whole devShell toolchain lives in `/nix/store`: `sudo /nix/nix-installer uninstall` removes Nix and
all of it. Out of store: the AVD (`avdmanager delete avd -n apnhelper`; `rm -rf ~/.android`) and the
direnv allow (`direnv deny`). Nix was installed via Determinate `nix-installer`; pre-existing broken Nix
remnants were removed during setup and are not restored.
