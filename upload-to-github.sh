#!/bin/bash
# 小白聊天助手 - 一键上传到 GitHub 并编译

echo "🤖 小白聊天助手 - GitHub 编译工具"
echo "=================================="
echo ""

# 检查 git
if ! command -v git &> /dev/null; then
    echo "❌ git 未安装，请先安装：pkg install git"
    exit 1
fi

# 获取 GitHub 用户名
echo -n "📝 输入你的 GitHub 用户名："
read GITHUB_USER

if [ -z "$GITHUB_USER" ]; then
    echo "❌ 用户名不能为空"
    exit 1
fi

REPO_URL="https://github.com/${GITHUB_USER}/xiaobai-chat-android.git"

echo ""
echo "📦 准备上传到：${REPO_URL}"
echo ""

# 进入项目目录
cd /sdcard/Download/LongXia/projects/android-app

# 初始化 git
echo "🔧 初始化 git..."
git init

# 添加文件
echo "📁 添加文件..."
git add .

# 提交
echo "💾 提交代码..."
git commit -m "小白聊天助手 v1.0 - 自动编译版"

# 修改分支名
git branch -M main

# 关联远程仓库
echo "🔗 关联 GitHub 仓库..."
git remote add origin ${REPO_URL}

# 推送
echo "🚀 推送到 GitHub..."
echo "   输入 GitHub 用户名和密码（或 Token）"
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 推送成功！"
    echo ""
    echo "📱 下一步："
    echo "   1. 打开 https://github.com/${GITHUB_USER}/xiaobai-chat-android"
    echo "   2. 点击 'Actions' 标签"
    echo "   3. 点击 'Run workflow'"
    echo "   4. 等待编译完成"
    echo "   5. 下载 APK"
    echo ""
else
    echo ""
    echo "❌ 推送失败，请检查："
    echo "   1. GitHub 用户名是否正确"
    echo "   2. 仓库是否已创建"
    echo "   3. 网络是否正常"
    echo ""
fi
