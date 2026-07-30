#!/bin/sh
# Self-bootstrapping Gradle wrapper for Unix/Linux
DIRNAME=$(dirname "$0")
WRAPPER_JAR="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Gradle wrapper jar not found. Bootstrapping..."
    mkdir -p "$DIRNAME/gradle/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -sSLo "$WRAPPER_JAR" https://raw.githubusercontent.com/gradle/gradle/release/gradle/wrapper/gradle-wrapper.jar
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$WRAPPER_JAR" https://raw.githubusercontent.com/gradle/gradle/release/gradle/wrapper/gradle-wrapper.jar
    else
        echo "Error: Neither curl nor wget found. Cannot bootstrap Gradle wrapper."
        exit 1
    fi
fi

exec java -jar "$WRAPPER_JAR" "$@"
