# bgclock task runner. Run `just` (no args) to list recipes.

# JDK 17+ is required by AGP 9 / Gradle 9. System default on Arch is JDK 8.
export JAVA_HOME := "/usr/lib/jvm/java-17-openjdk"

# Show available recipes
default:
    @just --list --unsorted

# Resolve Gradle scripts and dependencies (closest CLI analog of Studio's "Sync Project")
sync:
    ./gradlew help

# Force re-download of all dependencies (use after major version bumps)
sync-fresh:
    ./gradlew --refresh-dependencies help

# Run JVM unit tests (no device required)
test:
    ./gradlew :app:testDebugUnitTest

# Run Android Lint (AGP's bundled static analysis)
lint:
    ./gradlew :app:lintDebug

# Run lint + unit tests
check: lint test

# Build debug APK
build:
    ./gradlew :app:assembleDebug

# Install debug APK on connected device / running emulator
install:
    ./gradlew :app:installDebug

# Install, then launch the app on the connected device
run: install
    adb shell am start -n com.bgclock.debug/com.bgclock.MainActivity

# Build the signed release APK. Keystore at ./release.p12 (committed); password fetched from `pass Dev/bgclock`.
release:
    ./gradlew :app:assembleRelease
    @echo "APK at app/build/outputs/apk/release/app-release.apk"

# Build + install the release APK on the connected device
release-install: release
    adb install -r app/build/outputs/apk/release/app-release.apk

# Delete all build outputs
clean:
    ./gradlew clean
