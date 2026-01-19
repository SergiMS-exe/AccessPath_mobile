# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AccessPath is a Kotlin Multiplatform (KMP) mobile application targeting Android and iOS using Compose Multiplatform for shared UI.

## Build Commands

### Android
```bash
# Windows
.\gradlew.bat :composeApp:assembleDebug

# macOS/Linux
./gradlew :composeApp:assembleDebug
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and build from there. Requires configuring `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.

### Testing
```bash
./gradlew test                    # Run all tests
./gradlew :composeApp:testDebugUnitTest   # Run Android unit tests only
```

### Other Commands
```bash
./gradlew build                   # Build all modules
./gradlew clean                   # Clean build artifacts
```

## Architecture

### Platform Abstraction Pattern
The project uses Kotlin's `expect`/`actual` mechanism:
- `commonMain/` - Shared code with `expect` declarations
- `androidMain/` - Android `actual` implementations
- `iosMain/` - iOS `actual` implementations

Example: `Platform.kt` (expect) → `Platform.android.kt` / `Platform.ios.kt` (actual)

### Key Directories
- `composeApp/src/commonMain/` - Shared Kotlin code and Compose UI
- `composeApp/src/androidMain/` - Android-specific implementations and resources
- `composeApp/src/iosMain/` - iOS-specific implementations
- `iosApp/` - Native iOS app wrapper (SwiftUI bridge to Compose)

### iOS Bridge Pattern
SwiftUI wraps Compose via `MainViewControllerKt.MainViewController()` in `ContentView.swift`.

## Configuration

- **Package namespace**: `org.s3m4su.accesspath`
- **Min Android SDK**: 24
- **Target/Compile SDK**: 36
- **Kotlin**: 2.3.0
- **Compose Multiplatform**: 1.10.0
- **Dependency versions**: Managed in `gradle/libs.versions.toml`
