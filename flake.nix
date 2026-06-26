{
  description = "APN Settings Helper — reproducible Android dev environment (Apple Silicon, pure Nix, CLI-only)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    android-nixpkgs = {
      url = "github:tadfisher/android-nixpkgs";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, flake-utils, android-nixpkgs }:
    flake-utils.lib.eachSystem [ "aarch64-darwin" ] (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true; # Android SDK components are unfree
        };

        # Declarative Android SDK. Package names verified against android-nixpkgs
        # for aarch64-darwin. Adding `androidSdk` to the shell sets
        # ANDROID_HOME / ANDROID_SDK_ROOT automatically (read-only /nix/store).
        androidSdk = android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
          cmdline-tools-latest
          platform-tools                                    # adb
          build-tools-35-0-0
          platforms-android-35                              # compileSdk 35
          emulator
          system-images-android-35-google-apis-arm64-v8a    # native ABI; google_apis ⇒ adb root
        ]);

        jdk = pkgs.jdk17;
      in {
        devShells.default = pkgs.mkShell {
          packages = [
            androidSdk
            jdk
            pkgs.gradle
            pkgs.kotlin-language-server
            pkgs.ktlint
            pkgs.detekt
            pkgs.just
          ];

          JAVA_HOME = jdk.home;

          shellHook = ''
            export JAVA_HOME="${jdk.home}"
            echo "── apn-settings-helper devShell ──────────────────────────────"
            echo " JDK:          $(java -version 2>&1 | head -1)"
            echo " ANDROID_HOME: $ANDROID_HOME"
            echo " emulator:     $(command -v emulator)"
            echo " tasks:        run 'just'"
            echo "──────────────────────────────────────────────────────────────"
          '';
        };
      });
}
