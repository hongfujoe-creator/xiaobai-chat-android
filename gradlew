#!/bin/bash
# Gradle Wrapper 脚本
# 用于在线构建服务

GRADLE_VERSION=8.0
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"
GRADLE_BIN="$GRADLE_HOME/gradle-$GRADLE_VERSION/bin/gradle"

if [ ! -f "$GRADLE_BIN" ]; then
    echo "下载 Gradle $GRADLE_VERSION..."
    mkdir -p "$GRADLE_HOME"
    cd "$GRADLE_HOME"
    wget -q "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    unzip -q "gradle-$GRADLE_VERSION-bin.zip"
    cd -
fi

exec "$GRADLE_BIN" "$@"
