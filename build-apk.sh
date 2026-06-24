#!/usr/bin/env bash
# ============================================================
#  AuraPlay – One-command local APK builder
#  Requires: JDK 17  (brew install openjdk@17 / apt install openjdk-17-jdk)
#  No Android Studio needed.
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}"
echo "╔══════════════════════════════════════════╗"
echo "║       🎵 AuraPlay APK Builder 🎵        ║"
echo "╚══════════════════════════════════════════╝"
echo -e "${NC}"

# ── 1. Check Java ──────────────────────────────────────────
if ! command -v java &>/dev/null; then
    echo -e "${RED}✗ Java not found.${NC}"
    echo ""
    echo "Install JDK 17 first:"
    echo ""
    echo "  macOS   →  brew install openjdk@17"
    echo "  Ubuntu  →  sudo apt install openjdk-17-jdk"
    echo "  Fedora  →  sudo dnf install java-17-openjdk-devel"
    echo "  Windows →  winget install EclipseAdoptium.Temurin.17.JDK"
    echo ""
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo -e "${GREEN}✓ Java ${JAVA_VER} found${NC}"

# ── 2. Android SDK ─────────────────────────────────────────
if [ -z "${ANDROID_HOME:-}" ] && [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    # Try common default locations
    for candidate in \
        "$HOME/Android/Sdk" \
        "$HOME/Library/Android/sdk" \
        "/usr/local/lib/android/sdk" \
        "$HOME/android-sdk"; do
        if [ -d "$candidate" ]; then
            export ANDROID_HOME="$candidate"
            break
        fi
    done
fi

if [ -z "${ANDROID_HOME:-}" ]; then
    echo -e "${YELLOW}⚠ Android SDK not found. Downloading command-line tools…${NC}"
    echo ""

    SDK_ROOT="$HOME/android-sdk"
    mkdir -p "$SDK_ROOT/cmdline-tools"

    if [[ "$OSTYPE" == "darwin"* ]]; then
        SDK_URL="https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip"
    else
        SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    fi

    echo "  Downloading SDK command-line tools…"
    curl -L -o /tmp/cmdline-tools.zip "$SDK_URL"
    unzip -qo /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-tmp
    mv /tmp/cmdline-tools-tmp/cmdline-tools "$SDK_ROOT/cmdline-tools/latest" 2>/dev/null || true
    rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools-tmp

    export ANDROID_HOME="$SDK_ROOT"
    export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

    echo "  Accepting licenses…"
    yes | sdkmanager --licenses >/dev/null 2>&1 || true

    echo "  Installing SDK packages…"
    sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

    echo -e "${GREEN}✓ Android SDK installed at $SDK_ROOT${NC}"
else
    echo -e "${GREEN}✓ Android SDK at $ANDROID_HOME${NC}"
fi

# ── 3. Ensure Gradle Wrapper exists ────────────────────────
if [ ! -f "gradlew" ]; then
    echo -e "${YELLOW}Setting up Gradle wrapper…${NC}"
    if command -v gradle &>/dev/null; then
        gradle wrapper --gradle-version 8.4
    else
        # Manually bootstrap the wrapper
        WRAPPER_DIR="gradle/wrapper"
        mkdir -p "$WRAPPER_DIR"

        cat > "$WRAPPER_DIR/gradle-wrapper.properties" << 'PROPS'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
PROPS

        WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
        if [ ! -f "$WRAPPER_JAR" ]; then
            echo "  Downloading gradle-wrapper.jar…"
            curl -L -o "$WRAPPER_JAR" \
                "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" \
                2>/dev/null || \
            curl -L -o "$WRAPPER_JAR" \
                "https://services.gradle.org/distributions/gradle-8.4-bin.zip" 2>/dev/null || true
        fi

        # Create gradlew script
        cat > gradlew << 'GRADLEW'
#!/bin/sh
# Gradle wrapper stub – downloads & runs Gradle
GRADLE_VERSION="8.4"
GRADLE_DIR="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
if [ ! -d "$GRADLE_DIR" ]; then
    echo "Downloading Gradle $GRADLE_VERSION…"
    mkdir -p "$GRADLE_DIR"
    curl -L "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip
    unzip -qo /tmp/gradle.zip -d "$GRADLE_DIR"
    rm -f /tmp/gradle.zip
fi
GRADLE_HOME=$(find "$GRADLE_DIR" -maxdepth 1 -name "gradle-*" -type d | head -1)
exec "$GRADLE_HOME/bin/gradle" "$@"
GRADLEW
        chmod +x gradlew
    fi
fi

chmod +x gradlew 2>/dev/null || true

# ── 4. Build ───────────────────────────────────────────────
echo ""
echo -e "${CYAN}━━━ Building Debug APK ━━━${NC}"
echo ""

./gradlew assembleDebug --no-daemon --stacktrace

# ── 5. Locate APK ─────────────────────────────────────────
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)

if [ -n "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    cp "$APK_PATH" "AuraPlay-debug.apk" 2>/dev/null || true

    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║        ✓ BUILD SUCCESSFUL! ✓            ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  APK: ${CYAN}$(pwd)/AuraPlay-debug.apk${NC}"
    echo -e "  Size: ${CYAN}${APK_SIZE}${NC}"
    echo ""
    echo "  Transfer to your phone and install!"
    echo ""
else
    echo -e "${RED}✗ APK not found. Check build errors above.${NC}"
    exit 1
fi
