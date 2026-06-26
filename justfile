# APN Settings Helper — dev task runner (run inside the Nix devShell)
set shell := ["bash", "-cu"]

# list recipes
default:
    @just --list

# Fast checks: JVM unit tests + static analysis (no emulator). Needs app code (gradlew).
test:
    ./gradlew test
    @just lint

# Static analysis / linters (non-fatal individually)
lint:
    -ktlint "**/*.kt"
    -detekt
    -./gradlew lint

# Show installed SDK components
sdk-list:
    @sdkmanager --list_installed 2>/dev/null || ls "$ANDROID_HOME"

# Create the project AVD (arm64-v8a, google_apis ⇒ adb root works)
emu-create:
    avdmanager create avd -n apnhelper -k 'system-images;android-35;google_apis;arm64-v8a' --force

# Boot the emulator WITH a window (manual UI / i18n checks)
emu:
    nohup emulator -avd apnhelper -no-snapshot -gpu auto >/tmp/apnhelper-emu.log 2>&1 &
    adb wait-for-device
    until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
    @echo "emulator booted:"
    adb devices

# Kill running emulator(s)
emu-kill:
    -adb emu kill
    -pkill -f "emulator -avd apnhelper"

# Boot headless, run instrumented tests, tear down. Needs app code (gradlew).
emu-test:
    nohup emulator -avd apnhelper -no-window -no-audio -no-snapshot -gpu swiftshader_indirect >/tmp/apnhelper-emu.log 2>&1 &
    adb wait-for-device
    until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
    ./gradlew connectedAndroidTest
    -adb emu kill
