#!/bin/bash
# 小白聊天助手 - 快速构建脚本

echo "🤖 小白聊天助手 - APK 构建工具"
echo "================================"
echo ""

# 检查 Gradle
if ! command -v gradle &> /dev/null; then
    echo "⚠️  Gradle 未安装，请使用 Android Studio 构建"
    echo ""
    echo "📱 使用 Android Studio:"
    echo "   1. 打开 Android Studio"
    echo "   2. File → Open → 选择本项目目录"
    echo "   3. Build → Build Bundle(s) / APK(s) → Build APK(s)"
    echo ""
    exit 1
fi

# 构建
echo "🔨 开始构建..."
gradle assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 构建成功！"
    echo ""
    echo "📦 APK 位置："
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📲 安装到手机："
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ 构建失败"
    exit 1
fi
