#!/usr/bin/env sh
set -e

APP_HOME=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION="8.10.2"
DIST_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/dist"
DIST_ZIP="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
DIST_UNZIP="$DIST_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$DIST_UNZIP/bin/gradle"

if [ -f "$WRAPPER_JAR" ]; then
    exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

if [ ! -f "$GRADLE_BIN" ]; then
    mkdir -p "$DIST_DIR"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$DIST_ZIP"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -O "$DIST_ZIP"
    else
        echo "gradle not found and the distribution could not be downloaded" >&2
        exit 1
    fi
    if command -v unzip >/dev/null 2>&1; then
        unzip -q "$DIST_ZIP" -d "$DIST_DIR"
    elif command -v python3 >/dev/null 2>&1; then
        python3 -m zipfile -e "$DIST_ZIP" "$DIST_DIR"
    else
        echo "unzip or python3 is required to extract gradle" >&2
        exit 1
    fi
fi

exec "$GRADLE_BIN" "$@"