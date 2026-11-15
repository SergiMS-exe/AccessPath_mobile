{
  description = "AccessPath mobile: Kotlin Multiplatform / Android";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs =
    { self, nixpkgs }:
    let
      system = "x86_64-linux";

      pkgs = import nixpkgs {
        inherit system;
        config = {
          # La SDK de Android exige aceptar la licencia a nivel de Nix y sus
          # cmdline-tools estan marcadas como unfree.
          android_sdk.accept_license = true;
          allowUnfree = true;
        };
      };

      # SDK base: platform-tools (adb), build-tools 35, platform 35 y
      # cmdline-tools (sdkmanager/avdmanager). Sin emulador ni imagenes.
      androidBase = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "35" ];
        buildToolsVersions = [ "35.0.0" ];
        includeEmulator = false;
        includeSystemImages = false;
        includeNDK = false;
        includeCmake = false;
        includeSources = false;
        extraLicenses = [
          "android-sdk-license"
          "android-sdk-preview-license"
          "android-googletv-license"
          "android-sdk-arm-dbt-license"
        ];
      };

      # El emulador y su imagen pesan varios GB: shell aparte.
      androidEmulator = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "35" ];
        buildToolsVersions = [ "35.0.0" ];
        includeEmulator = true;
        includeSystemImages = true;
        systemImageTypes = [ "google_apis" ];
        abiVersions = [ "x86_64" ];
        useGoogleAPIs = true;
        includeNDK = false;
        includeCmake = false;
        includeSources = false;
        extraLicenses = [
          "android-sdk-license"
          "android-sdk-preview-license"
          "android-googletv-license"
          "android-sdk-arm-dbt-license"
        ];
      };

      androidHook = sdk: ''
        export ANDROID_HOME="${sdk.androidsdk}/libexec/android-sdk"
        export ANDROID_SDK_ROOT="$ANDROID_HOME"
        export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
      '';

      common = with pkgs; [
        git
        curl
        jq
        unzip
        gnumake
      ];
    in
    {
      devShells.${system} = {
        default = pkgs.mkShell {
          name = "accesspath-mobile";
          packages = common ++ [
            pkgs.jdk17
            pkgs.gradle
            pkgs.scrcpy
            androidBase.androidsdk
          ];
          shellHook = ''
            ${androidHook androidBase}
            export JAVA_HOME="${pkgs.jdk17.home}"
            echo "accesspath-mobile: JDK $(java -version 2>&1 | head -1)"
            echo "  crea local.properties con:"
            echo "    API_BASE_URL=https://tu-backend"
            echo "    MAPS_API_KEY=tu_clave"
            echo "  ./gradlew :composeApp:assembleDebug"
            echo "  adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk"
            echo "  sin cable: adb pair IP:PUERTO && adb connect IP:PUERTO"
          '';
        };

        emulator = pkgs.mkShell {
          name = "accesspath-emulator";
          packages = common ++ [
            pkgs.jdk17
            pkgs.gradle
            androidEmulator.androidsdk
          ];
          shellHook = ''
            ${androidHook androidEmulator}
            export JAVA_HOME="${pkgs.jdk17.home}"
            # El emulador usa Qt: en Wayland funciona por XWayland.
            export QT_QPA_PLATFORM="''${QT_QPA_PLATFORM:-xcb}"
            echo "accesspath-emulator (android-35 google_apis x86_64)"
            echo "  avdmanager create avd -n ap35 -k \"system-images;android-35;google_apis;x86_64\" -d pixel_6"
            echo "  emulator -avd ap35 -no-snapshot -gpu host"
            echo "  /dev/kvm debe existir y ser accesible"
          '';
        };
      };

      formatter.${system} = pkgs.nixfmt;
    };
}
